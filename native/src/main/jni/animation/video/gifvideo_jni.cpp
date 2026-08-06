#include <jni.h>
#include <android/bitmap.h>
#include <cstdint>
#include <limits>
#include <string>
#include <unistd.h>
#include <linux/stat.h>
#include <asm/fcntl.h>
#include <fcntl.h>
#include <libyuv.h>
#include "sws_context_holder.h"
#include "video_frame_reader.h"
#include <cmath>
#include "fenrir_native.h"

extern "C" {
#include <libavformat/avformat.h>
#include <libavutil/eval.h>
#include <libswscale/swscale.h>
#include <libavutil/display.h>
}

static inline std::string av_make_error_str(int errnum) {
    char errbuf[AV_ERROR_MAX_STRING_SIZE];
    av_strerror(errnum, errbuf, AV_ERROR_MAX_STRING_SIZE);
    return (std::string) errbuf;
}

#undef av_err2str
#define av_err2str(errnum) av_make_error_str(errnum).c_str()

/*
static inline void print_ffmpeg_error(int error) {
    LOGE("%s\n", av_err2str(error));
}
*/

struct OffsetIOContext;
struct VideoInfo;
static void freeOffsetIO(VideoInfo *info);

class VideoInfo {
public:
    ~VideoInfo() {
        delete reader;
        reader = nullptr;

        if (video_dec_ctx) {
            // avcodec_close() frees internals but NOT the context allocated by
            // avcodec_alloc_context3(); avcodec_free_context() frees both.
            avcodec_free_context(&video_dec_ctx);
        }
        if (fmt_ctx) {
            avformat_close_input(&fmt_ctx);
            fmt_ctx = nullptr;
        }
        if (src) {
            delete [] src;
            src = nullptr;
        }
        if (ioContext != nullptr) {
            if (ioContext->buffer) {
                av_freep(&ioContext->buffer);
            }
            avio_context_free(&ioContext);
            ioContext = nullptr;
        }
        freeOffsetIO(this);
        if (fd >= 0) {
            close(fd);
            fd = -1;
        }

        video_stream_idx = -1;
        video_stream = nullptr;
        audio_stream = nullptr;
    }

    AVFormatContext *fmt_ctx = nullptr;
    char *src = nullptr;
    int video_stream_idx = -1;
    AVStream *video_stream = nullptr;
    AVStream *audio_stream = nullptr;
    AVCodecContext *video_dec_ctx = nullptr;
    // Borrows fmt_ctx and video_dec_ctx; must be deleted before them (see dtor).
    VideoFrameReader *reader = nullptr;
    bool stopped = false;
    bool seeking = false;
    bool afterEof = false;
    bool isSingleFrame = false;

    struct SwsContextHolder sws_ctx_holder;

    AVIOContext *ioContext = nullptr;
    // Custom AVIO for the fileOffset path in nGetVideoInfo. Owned here because
    // fmt_ctx->pb is not freed by avformat_close_input for a caller-supplied pb.
    AVIOContext *offsetIoContext = nullptr;
    struct OffsetIOContext *offsetIoOpaque = nullptr;
    int fd = -1;
    int64_t file_size = 0;
};

static enum AVPixelFormat get_format(AVCodecContext *ctx,
                                        const enum AVPixelFormat *pix_fmts) {
    const enum AVPixelFormat *p;

    for (p = pix_fmts; *p != -1; p++) {
        LOGE("available format %d", p);
    }

    return pix_fmts[0];
}

static int open_codec_context(int *stream_idx, AVCodecContext **dec_ctx, AVFormatContext *fmt_ctx, enum AVMediaType type) {
    int ret, stream_index;
    AVStream *st;
    const AVCodec *dec = nullptr;
    AVDictionary *opts = nullptr;

    ret = av_find_best_stream(fmt_ctx, type, -1, -1, nullptr, 0);
    if (ret < 0) {
        LOGE("can't find %s stream in input file", av_get_media_type_string(type));
        return ret;
    } else {
        stream_index = ret;
        st = fmt_ctx->streams[stream_index];

        dec = avcodec_find_decoder(st->codecpar->codec_id);
        if (!dec) {
            LOGE("failed to find %d codec", st->codecpar->codec_id);
            return AVERROR(EINVAL);
        }

        *dec_ctx = avcodec_alloc_context3(dec);
        if (!*dec_ctx) {
            LOGE("Failed to allocate the %s codec context", av_get_media_type_string(type));
            return AVERROR(ENOMEM);
        }

        if ((ret = avcodec_parameters_to_context(*dec_ctx, st->codecpar)) < 0) {
            LOGE("Failed to copy %s codec parameters to decoder context", av_get_media_type_string(type));
            return ret;
        }

        av_dict_set(&opts, "refcounted_frames", "1", 0);
        ret = avcodec_open2(*dec_ctx, dec, &opts);
        av_dict_free(&opts);   // frees leftover (unconsumed) options on all paths
        if (ret < 0) {
            LOGE("Failed to open %s codec", av_get_media_type_string(type));
            return ret;
        }
        *stream_idx = stream_index;
    }

    return 0;
}

struct OffsetIOContext {
    int fd;
    int64_t offset;
};

// Frees the custom AVIO built for the fileOffset path (see nGetVideoInfo).
// Safe to call when nothing was allocated: all fields default to null/-1.
static void freeOffsetIO(VideoInfo *info) {
    if (info->offsetIoContext != nullptr) {
        av_freep(&info->offsetIoContext->buffer);
        avio_context_free(&info->offsetIoContext);
        info->offsetIoContext = nullptr;
    }
    if (info->offsetIoOpaque != nullptr) {
        if (info->offsetIoOpaque->fd >= 0) {
            close(info->offsetIoOpaque->fd);
        }
        delete info->offsetIoOpaque;
        info->offsetIoOpaque = nullptr;
    }
}

static int getVideoRotation(const AVStream *stream) {
    const AVPacketSideData *displayMatrix = av_packet_side_data_get(
            stream->codecpar->coded_side_data,
            stream->codecpar->nb_coded_side_data,
            AV_PKT_DATA_DISPLAYMATRIX);
    if (displayMatrix != nullptr && displayMatrix->size >= 9 * sizeof(int32_t)) {
        // av_display_rotation_get() returns the counter-clockwise angle; negate
        // to match the legacy clockwise convention, then normalize.
        double theta = -av_display_rotation_get((const int32_t *) displayMatrix->data);
        if (!std::isnan(theta)) {
            int rotation = ((int) lround(theta / 90.0) * 90) % 360;
            return rotation < 0 ? rotation + 360 : rotation;
        }
        return 0;
    }

    // Fallback for old containers that still carry the legacy metadata tag.
    AVDictionaryEntry *rotate_tag = av_dict_get(stream->metadata, "rotate", nullptr, 0);
    if (rotate_tag && *rotate_tag->value && strcmp(rotate_tag->value, "0") != 0) {
        char *tail;
        int rotation = (int) av_strtod(rotate_tag->value, &tail);
        if (*tail == '\0') {
            rotation = ((rotation / 90) * 90) % 360;
            return rotation < 0 ? rotation + 360 : rotation;
        }
    }
    return 0;
}

static jlong createDecoder(JNIEnv *env, jstring src,
                              jintArray data) {
    auto *info = new VideoInfo();

    char const *srcString = SafeGetStringUTFChars(env, src, nullptr);
    size_t len = strlen(srcString);
    info->src = new char[len + 1];
    memcpy(info->src, srcString, len);
    info->src[len] = '\0';
    if (srcString != nullptr) {
        env->ReleaseStringUTFChars(src, srcString);
    }

    //av_dict_set(&info->options, "protocol_whitelist", "file,http,https", 0);

    int ret = 0;
    if ((ret = avformat_open_input(&info->fmt_ctx, info->src, nullptr, nullptr)) < 0) {
        LOGE("can't open source file %s, %s", info->src, av_err2str(ret));
        delete info;
        return 0;
    }

    if ((ret = avformat_find_stream_info(info->fmt_ctx, nullptr)) < 0) {
        LOGE("can't find stream information %s, %s", info->src, av_err2str(ret));
        delete info;
        return 0;
    }

    if (open_codec_context(&info->video_stream_idx, &info->video_dec_ctx, info->fmt_ctx, AVMEDIA_TYPE_VIDEO) >= 0) {
        info->video_stream = info->fmt_ctx->streams[info->video_stream_idx];
    }

    if (info->video_stream == nullptr) {
        LOGE("can't find video stream in the input, aborting %s", info->src);
        delete info;
        return 0;
    }

    info->reader = new VideoFrameReader(info->fmt_ctx, info->video_dec_ctx, info->video_stream_idx);
    VideoInfo *self = info;
    info->reader->shouldAbort = [self]() {
        // Covers nStopDecoder (stopped), nPrepareToSeek (seeking) and stream cancel.
        return self->stopped || self->seeking;
    };

    jint *dataArr = env->GetIntArrayElements(data, nullptr);
    if (dataArr != nullptr) {
        dataArr[0] = info->video_dec_ctx->width;
        dataArr[1] = info->video_dec_ctx->height;
        //float pixelWidthHeightRatio = info->video_dec_ctx->sample_aspect_ratio.num / info->video_dec_ctx->sample_aspect_ratio.den; TODO support
        dataArr[2] = (jint) getVideoRotation(info->video_stream);
        dataArr[4] = (int32_t) (info->fmt_ctx->duration * 1000 / AV_TIME_BASE);
        int video_stream_index = -1;
        double fps = 30.0;
        for (int i = 0; i < info->fmt_ctx->nb_streams; i++) {
            if (info->fmt_ctx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
                video_stream_index = i;
                break;
            }
        }
        if (video_stream_index != -1) {
            AVStream* video_stream = info->fmt_ctx->streams[video_stream_index];
            if (video_stream->avg_frame_rate.den && video_stream->avg_frame_rate.num) {
                fps = av_q2d(video_stream->avg_frame_rate);
            } else if(video_stream->r_frame_rate.den && video_stream->r_frame_rate.num) {
                fps = av_q2d(video_stream->r_frame_rate);
            } else {
                /*
                int ticks = video_stream->codec->ticks_per_frame;
                fps = 1.0 / (ticks * av_q2d(video_stream->time_base));
                 */
            }
        }
        dataArr[5] = (int32_t) fps;
        //(int32_t) (1000 * info->video_stream->duration * av_q2d(info->video_stream->time_base));
        env->ReleaseIntArrayElements(data, dataArr, 0);
    }

    //LOGD("successfully opened file %s", info->src);

    return reinterpret_cast<jlong>(info);
}

extern "C" JNIEXPORT jlong JNICALL
Java_dev_ragnarok_fenrir_module_animation_AnimatedFileDrawable_createDecoder(JNIEnv *env, jobject,
                                                                             jstring src,
                                                                             jintArray data) {
    return createDecoder(env, src, data);
}

extern "C" JNIEXPORT jlong JNICALL
Java_dev_ragnarok_fenrir_module_animation_AnimatedFileFrame_createDecoder(JNIEnv *env, jobject,
                                                                          jstring src,
                                                                          jintArray data) {
    return createDecoder(env, src, data);
}

static void destroyDecoder(jlong ptr) {
    if (!ptr) {
        return;
    }
    delete reinterpret_cast<VideoInfo *>(ptr);
}

extern "C" JNIEXPORT void JNICALL
Java_dev_ragnarok_fenrir_module_animation_AnimatedFileDrawable_destroyDecoder(JNIEnv *, jobject,
                                                                              jlong ptr) {
    destroyDecoder(ptr);
}

extern "C" JNIEXPORT void JNICALL
Java_dev_ragnarok_fenrir_module_animation_AnimatedFileFrame_destroyDecoder(JNIEnv *, jobject,
                                                                           jlong ptr) {
    destroyDecoder(ptr);
}

extern "C" JNIEXPORT void JNICALL Java_dev_ragnarok_fenrir_module_animation_AnimatedFileDrawable_stopDecoder(JNIEnv *env, jobject, jlong ptr) {
    if (!ptr) {
        return;
    }
    auto *info = reinterpret_cast<VideoInfo *>(ptr);
    info->stopped = true;
}

extern "C" JNIEXPORT void JNICALL Java_dev_ragnarok_fenrir_module_animation_AnimatedFileDrawable_prepareToSeek(JNIEnv *env, jobject, jlong ptr) {
    if (!ptr) {
        return;
    }
    auto *info = reinterpret_cast<VideoInfo *>(ptr);
    info->seeking = true;
}

static void push_time(JNIEnv *env, VideoInfo* info, AVFrame *frame, jintArray data) {
    jint *dataArr = env->GetIntArrayElements(data, nullptr);
    dataArr[3] = (jint) ((double)(1000 * frame->best_effort_timestamp) * av_q2d(info->video_stream->time_base));
    env->ReleaseIntArrayElements(data, dataArr, 0);
}

static void push_single_frame(JNIEnv *env, jintArray data) {
    jint *dataArr = env->GetIntArrayElements(data, nullptr);
    dataArr[7] = 1;
    env->ReleaseIntArrayElements(data, dataArr, 0);
}

extern "C" JNIEXPORT void JNICALL Java_dev_ragnarok_fenrir_module_animation_AnimatedFileDrawable_seekToMs(JNIEnv *env, jobject, jlong ptr, jlong ms, jintArray data, jboolean precise) {
    if (ptr == 0) {
        return;
    }
    auto *info = reinterpret_cast<VideoInfo *>(ptr);
    info->seeking = false;   // clear before decoding so shouldAbort() won't bail
    info->afterEof = false;

    AVRational tb = info->video_stream->time_base;
    auto pts = (int64_t) ((double)ms / av_q2d(tb) / 1000);

    if (!info->reader->seek(pts)) {
        LOGE("can't seek file %s", info->src);
        return;
    }

    if (!precise) {
        // Non-precise: land on the keyframe, don't decode toward the target.
        return;
    }

    double targetSec = (double)ms / 1000.0;
    for (;;) {
        VideoFrameReader::Status st = info->reader->getNextFrame();
        if (st != VideoFrameReader::Status::Ok) {
            // Eof: target lies past the last frame -> rewind to start, as before.
            // Aborted/Error: stop without advancing.
            if (st == VideoFrameReader::Status::Eof) {
                info->reader->seek(0);
            }
            return;
        }

        if (info->reader->frameTimeSeconds() >= targetSec) {
            push_time(env, info, info->reader->frame(), data);
            return;
        }
    }
}

static inline void writeFrameToBitmap(JNIEnv *env, VideoInfo *info, AVFrame *frame, jintArray data, jobject bitmap) {
    if (env->IsSameObject(bitmap, nullptr)) {
        push_time(env, info, frame, data);
        return;
    }
    jint *dataArr = env->GetIntArrayElements(data, nullptr);
    int32_t wantedWidth;
    int32_t wantedHeight;

    AndroidBitmapInfo bitmapInfo;
    AndroidBitmap_getInfo(env, bitmap, &bitmapInfo);
    auto bitmapWidth = (int32_t)bitmapInfo.width;
    auto bitmapHeight = (int32_t)bitmapInfo.height;
    auto bitmapStride = (int32_t)bitmapInfo.stride;

    if (dataArr != nullptr) {
        wantedWidth = dataArr[0];
        wantedHeight = dataArr[1];
        dataArr[3] = (jint) ((double)(1000 * frame->best_effort_timestamp) * av_q2d(info->video_stream->time_base));
        if (env->GetArrayLength(data) > 6) {
            bool isOpaque = (
                frame->format == AV_PIX_FMT_YUV420P  ||
                frame->format == AV_PIX_FMT_YUVJ420P ||
                frame->format == AV_PIX_FMT_YUV444P
            );
            dataArr[6] = isOpaque ? 1 : 0;
        }
        env->ReleaseIntArrayElements(data, dataArr, 0);
    } else {
        wantedWidth = bitmapWidth;
        wantedHeight = bitmapHeight;
    }

    if (!(wantedWidth == frame->width && wantedHeight == frame->height || wantedWidth == frame->height && wantedHeight == frame->width)) {
        return;
    }

    void *pixels;
    if (__builtin_expect(AndroidBitmap_lockPixels(env, bitmap, &pixels) != ANDROID_BITMAP_RESULT_SUCCESS, 0)) {
        return;
    }

    SwsContext* sws_ctx = nullptr;
    if (frame->format > AV_PIX_FMT_NONE && frame->format < AV_PIX_FMT_NB && frame->format != AV_PIX_FMT_YUVA420P) {
        sws_ctx = info->sws_ctx_holder.get(
            frame->width,
            frame->height,
            (AVPixelFormat) frame->format,
            bitmapWidth,
            bitmapHeight,
            AV_PIX_FMT_RGBA);
    } else if (info->video_dec_ctx->pix_fmt > AV_PIX_FMT_NONE && info->video_dec_ctx->pix_fmt < AV_PIX_FMT_NB && frame->format != AV_PIX_FMT_YUVA420P) {
        sws_ctx = info->sws_ctx_holder.get(
            info->video_dec_ctx->width,
            info->video_dec_ctx->height,
            info->video_dec_ctx->pix_fmt,
            bitmapWidth,
            bitmapHeight,
            AV_PIX_FMT_RGBA);
    }

    if (sws_ctx != nullptr && ((intptr_t) pixels) % 16 == 0) {
        uint8_t __attribute__ ((aligned (16))) *dst_data[1];
        dst_data[0] = (uint8_t *) pixels;

        int32_t dst_stride[1];
        dst_stride[0] = bitmapStride;
        sws_scale(sws_ctx,
            frame->data,
            frame->linesize,
            0,
            frame->height,
            dst_data,
            dst_stride
        );
    } else if (frame->width == bitmapWidth && frame->height == bitmapHeight) {
        if (frame->format == AV_PIX_FMT_YUVA420P) {
            libyuv::I420AlphaToARGBMatrix(
                frame->data[0], frame->linesize[0],
                frame->data[2], frame->linesize[2],
                frame->data[1], frame->linesize[1],
                frame->data[3], frame->linesize[3],
                (uint8_t *) pixels,
                bitmapStride,
                &libyuv::kYvuI601Constants,
                bitmapWidth,
                bitmapHeight,
                1
            );
        } else if (frame->format == AV_PIX_FMT_YUV444P) {
            libyuv::H444ToARGB(
                frame->data[0], frame->linesize[0],
                frame->data[2], frame->linesize[2],
                frame->data[1], frame->linesize[1],
                (uint8_t *) pixels,
                bitmapStride,
                bitmapWidth,
                bitmapHeight
            );
        } else if (frame->format == AV_PIX_FMT_YUV420P || frame->format == AV_PIX_FMT_YUVJ420P) {
            if (frame->colorspace == AVColorSpace::AVCOL_SPC_BT709) {
                libyuv::H420ToARGB(
                    frame->data[0], frame->linesize[0],
                    frame->data[2], frame->linesize[2],
                    frame->data[1], frame->linesize[1],
                    (uint8_t *) pixels,
                    bitmapStride,
                    bitmapWidth,
                    bitmapHeight
                );
            } else {
                libyuv::I420ToARGB(
                    frame->data[0], frame->linesize[0],
                    frame->data[2], frame->linesize[2],
                    frame->data[1], frame->linesize[1],
                    (uint8_t *) pixels,
                    bitmapStride,
                    bitmapWidth,
                    bitmapHeight
                );
            }
        } else if (frame->format == AV_PIX_FMT_BGRA) {
            libyuv::ABGRToARGB(
                frame->data[0], frame->linesize[0],
                (uint8_t *) pixels,
                bitmapStride,
                bitmapWidth,
                bitmapHeight
            );
        }
    } else if (sws_ctx != nullptr && ((intptr_t) pixels) % 16 != 0) {
        // fallback if pixels not aligned
        int alignedStride = FFALIGN(bitmapWidth * 4, 16);
        int bufSize = alignedStride * bitmapHeight;
        auto *alignedBuf = (uint8_t *) av_malloc(bufSize);
        if (alignedBuf != nullptr) {
            uint8_t *dst_data[1] = { alignedBuf };
            int32_t dst_stride[1] = { alignedStride };
            sws_scale(sws_ctx,
                      frame->data,
                      frame->linesize,
                      0,
                      frame->height,
                      dst_data,
                      dst_stride
            );
            if (alignedStride == bitmapStride) {
                memcpy(pixels, alignedBuf, bufSize);
            } else {
                uint8_t *src = alignedBuf;
                auto *dst = (uint8_t *) pixels;
                int copyStride = bitmapWidth * 4;
                for (int i = 0; i < bitmapHeight; i++) {
                    memcpy(dst, src, copyStride);
                    src += alignedStride;
                    dst += bitmapStride;
                }
            }
            av_free(alignedBuf);
        }
    }

    AndroidBitmap_unlockPixels(env, bitmap);
}

static jint getFrameAtTime(JNIEnv *env, long ptr, jlong ms, jobject bitmap, jintArray data) {
    if (ptr == 0 || bitmap == nullptr || data == nullptr) {
        return 0;
    }
    auto *info = reinterpret_cast<VideoInfo *>(ptr);
    info->seeking = false;   // clear before decoding so shouldAbort() won't bail
    info->afterEof = false;

    AVRational tb = info->video_stream->time_base;
    auto pts = (int64_t) ((double)ms / av_q2d(tb) / 1000);

    if (!info->reader->seek(pts)) {
        LOGE("can't seek file %s", info->src);
        return 0;
    }

    double targetSec = (double)ms / 1000.0;
    AVFrame *held = av_frame_alloc();   // most recent frame before target (fallback)
    bool haveHeld = false;
    int result = 0;

    for (;;) {
        VideoFrameReader::Status st = info->reader->getNextFrame();
        if (st != VideoFrameReader::Status::Ok) {
            // Eof: target lies at/after the last frame -> emit the last frame we
            // held. Aborted/Error: give up with no frame.
            if (st == VideoFrameReader::Status::Eof && haveHeld) {
                writeFrameToBitmap(env, info, held, data, bitmap);
                result = 1;
            }
            break;
        }

        AVFrame *frame = info->reader->frame();
        if (info->reader->frameTimeSeconds() >= targetSec) {
            writeFrameToBitmap(env, info, frame, data, bitmap);
            result = 1;
            break;
        }

        // Frame is before the target: keep it as the fallback and continue.
        av_frame_unref(held);
        av_frame_ref(held, frame);
        haveHeld = true;
    }

    av_frame_free(&held);
    return result;
}

extern "C" JNIEXPORT jint JNICALL
Java_dev_ragnarok_fenrir_module_animation_AnimatedFileDrawable_getFrameAtTime(JNIEnv *env, jobject,
                                                                              jlong ptr, jlong ms,
                                                                              jobject bitmap,
                                                                              jintArray data) {
    return getFrameAtTime(env, ptr, ms, bitmap, data);
}

extern "C" JNIEXPORT jint JNICALL
Java_dev_ragnarok_fenrir_module_animation_AnimatedFileFrame_getFrameAtTime(JNIEnv *env, jobject,
                                                                           jlong ptr, jlong ms,
                                                                           jobject bitmap,
                                                                           jintArray data) {
    return getFrameAtTime(env, ptr, ms, bitmap, data);
}

extern "C" JNIEXPORT jint JNICALL Java_dev_ragnarok_fenrir_module_animation_AnimatedFileDrawable_getVideoFrame(JNIEnv *env, jobject, jlong ptr, jobject bitmap, jintArray data, jboolean preview, jfloat start_time, jfloat end_time, jboolean loop) {
    if (!ptr) {
        return 0;
    }
    auto *info = reinterpret_cast<VideoInfo *>(ptr);
    if (info->stopped || info->seeking) {
        return 0;
    }

    AVRational tb = info->video_stream->time_base;
    int64_t startPts = start_time > 0 ? (int64_t) (start_time / av_q2d(tb)) : 0;

    VideoFrameReader::Status st = info->reader->getNextFrame();

    // End of stream, or the frame ran past the trim point -> loop back or finish.
    // Note: end_time is compared against the frame's display-order timestamp,
    // not a packet pts, so B-frame reordering can no longer drop the wrong frame.
    bool pastEnd = st == VideoFrameReader::Status::Ok &&
                   end_time > 0 && info->reader->frameTimeSeconds() > end_time;

    if (st == VideoFrameReader::Status::Eof) {
        if (info->afterEof && !info->isSingleFrame) {
            push_single_frame(env, data);
            info->isSingleFrame = true;
        }
        info->afterEof = true;
    } else {
        info->afterEof = false;
    }

    if (st == VideoFrameReader::Status::Eof || pastEnd) {
        if (!loop) {
            return 0;
        }
        if (!info->reader->seek(startPts)) {
            return 0;
        }
        st = info->reader->getNextFrame();
    }

    if (st != VideoFrameReader::Status::Ok) {
        // Aborted (stopped / seeking / canceled), Error, or Eof after looping.
        return 0;
    }

    AVFrame *frame = info->reader->frame();
    if (bitmap != nullptr) {
        writeFrameToBitmap(env, info, frame, data, bitmap);
    }
    push_time(env, info, frame, data);
    return 1;
}