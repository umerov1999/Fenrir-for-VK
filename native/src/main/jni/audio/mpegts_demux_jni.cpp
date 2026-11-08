#include <iostream>
#include <sstream>
#include <cstring>
#include <jni.h>
#include "fenrir_native.h"
#include "ts/MpegTsDemuxer.h"

extern "C" {
#include <libavformat/avformat.h>
#include <libavcodec/avcodec.h>
#include <libavutil/error.h>
}

#define BUFFERSIZE 8196

static bool aacToM4a(const std::string &input, const std::string &output) {
    AVFormatContext *inFmt = nullptr;
    AVFormatContext *outFmt = nullptr;

    int ret = avformat_open_input(
            &inFmt, input.c_str(), nullptr, nullptr
    );

    if (ret < 0)
        return false;

    ret = avformat_find_stream_info(inFmt, nullptr);

    if (ret < 0)
        goto cleanup;

    ret = avformat_alloc_output_context2(
            &outFmt,
            nullptr,
            "ipod",
            output.c_str()
    );

    if (ret < 0 || !outFmt)
        goto cleanup;

    {
        int audioIndex = -1;
        for (unsigned int i = 0;
             i < inFmt->nb_streams; ++i) {

            AVStream *inStream = inFmt->streams[i];

            if (inStream->codecpar->codec_type ==
                AVMEDIA_TYPE_AUDIO) {

                audioIndex = static_cast<int>(i);
                break;
            }
        }
        if (audioIndex < 0) {
            ret = AVERROR_STREAM_NOT_FOUND;
            goto cleanup;
        }
        AVStream *inStream = inFmt->streams[audioIndex];
        AVStream *outStream =
                avformat_new_stream(outFmt, nullptr);

        if (!outStream) {
            ret = AVERROR(ENOMEM);
            goto cleanup;
        }

        ret = avcodec_parameters_copy(
                outStream->codecpar,
                inStream->codecpar
        );

        if (ret < 0)
            goto cleanup;

        outStream->codecpar->codec_tag = 0;
        outStream->time_base = inStream->time_base;

        if (!(outFmt->oformat->flags & AVFMT_NOFILE)) {
            ret = avio_open(
                    &outFmt->pb,
                    output.c_str(),
                    AVIO_FLAG_WRITE
            );

            if (ret < 0)
                goto cleanup;
        }

        AVDictionary *opts = nullptr;

        av_dict_set(
                &opts,
                "movflags",
                "+faststart",
                0
        );

        ret = avformat_write_header(outFmt, &opts);
        av_dict_free(&opts);

        if (ret < 0)
            goto cleanup;

        AVPacket *pkt = av_packet_alloc();

        if (!pkt) {
            ret = AVERROR(ENOMEM);
            goto cleanup;
        }

        while ((ret = av_read_frame(inFmt, pkt)) >= 0) {
            if (pkt->stream_index != audioIndex) {
                av_packet_unref(pkt);
                continue;
            }

            av_packet_rescale_ts(
                    pkt,
                    inStream->time_base,
                    outStream->time_base
            );

            pkt->stream_index = outStream->index;

            ret = av_interleaved_write_frame(
                    outFmt,
                    pkt
            );

            av_packet_unref(pkt);

            if (ret < 0)
                break;
        }

        av_packet_free(&pkt);

        if (ret == AVERROR_EOF)
            ret = 0;

        if (ret >= 0)
            ret = av_write_trailer(outFmt);
    }

    cleanup:

    if (outFmt) {
        if (!(outFmt->oformat->flags & AVFMT_NOFILE)
            && outFmt->pb) {
            avio_closep(&outFmt->pb);
        }

        avformat_free_context(outFmt);
    }

    if (inFmt)
        avformat_close_input(&inFmt);

    return ret >= 0;
}

extern "C" JNIEXPORT jint JNICALL
Java_dev_ragnarok_fenrir_module_hls_TSDemuxer_unpack(JNIEnv *env, jobject, jstring input,
                                                     jstring output) {
    char const *inputString = SafeGetStringUTFChars(env, input, nullptr);
    char const *outputString = SafeGetStringUTFChars(env, output, nullptr);
    std::string inputPath = inputString;
    std::string outputPath = outputString;
    if (inputString != nullptr) {
        env->ReleaseStringUTFChars(input, inputString);
    }
    if (outputString != nullptr) {
        env->ReleaseStringUTFChars(output, outputString);
    }

    FILE *fileIn = fopen(inputPath.c_str(), "rb");
    if (!fileIn) {
        return 0;
    }
    fseek(fileIn, 0, SEEK_END);
    long sz = ftell(fileIn);
    fseek(fileIn, 0, SEEK_SET);
    if (sz <= 0) {
        fclose(fileIn);
        return 0;
    }

    FILE *fileOut = fopen(outputPath.c_str(), "wb");
    if (!fileOut) {
        fclose(fileIn);
        return 0;
    }

    auto isAacFormat = std::make_shared<bool>(false);
    auto isHasWriteError = std::make_shared<bool>(false);
    auto demuxer = std::make_shared<MpegTsDemuxer>(
            [fileOut, isAacFormat, isHasWriteError](const uint8_t *payload, size_t size,
                                                    AudioFormat format) {
                if (format == AudioFormat::AAC_ADTS) {
                    *isAacFormat = true;
                }
                if (fwrite(payload, sizeof(uint8_t), size, fileOut) != size) {
                    *isHasWriteError = true;
                }
            });

    std::string data;
    data.resize(BUFFERSIZE);
    size_t n = 0;
    while ((n = fread(data.data(), sizeof(uint8_t), BUFFERSIZE, fileIn)) >= BUFFERSIZE) {
        demuxer->ProcessBytes(reinterpret_cast<const uint8_t *>(data.data()), n);
        if (*isHasWriteError) {
            fclose(fileIn);
            fclose(fileOut);
            return false;
        }
    }
    if (n > 0) {
        data.resize(n);
        n = fread(data.data(), sizeof(uint8_t), n, fileIn);
        demuxer->ProcessBytes(reinterpret_cast<const uint8_t *>(data.data()), n);
        if (*isHasWriteError) {
            fclose(fileIn);
            fclose(fileOut);
            return false;
        }
    }
    fclose(fileIn);
    fclose(fileOut);

    int ret = 1;
    if (*isAacFormat) {
        ret = 2;
        if (aacToM4a(outputPath, outputPath + ".m4a")) {
            std::remove(outputPath.c_str());
            std::rename((outputPath + ".m4a").c_str(), outputPath.c_str());
            ret = 3;
        }
    }

    return ret;
}