#include <jni.h>
#include <ogg/ogg.h>
#include <cstdio>
#include <cstring>
#include <opus.h>
#include <cstdint>
#include <cstdlib>
#include <ctime>
#include <functional>
#include <vector>
#include <opusfile.h>
#include <cmath>
#include <rnnoise.h>
extern "C" {
#include "libswresample/swresample.h"
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libavutil/opt.h"
}
#include "fenrir_native.h"

struct OpusHeader {
    int version;
    int channels; /* Number of channels: 1..255 */
    int preskip;
    ogg_uint32_t input_sample_rate;
    int gain; /* in dB S7.8 should be zero whenever possible */
    int channel_mapping;
    /* The rest is only used if channel_mapping != 0 */
    int nb_streams;
    int nb_coupled;
    unsigned char stream_map[255];
};

struct Packet {
    unsigned char *data;
    int maxlen;
    int pos;
};

struct oe_enc_opt {
    int channels;
    long rate;
    int gain;
    int samplesize;
    int endianness;
    int skip;
    char *comments;
    int comments_length;
};

static int write_uint32(Packet *p, ogg_uint32_t val) {
    if (p->pos > p->maxlen - 4) {
        return 0;
    }
    p->data[p->pos  ] = (val    ) & 0xFF;
    p->data[p->pos+1] = (val>> 8) & 0xFF;
    p->data[p->pos+2] = (val>>16) & 0xFF;
    p->data[p->pos+3] = (val>>24) & 0xFF;
    p->pos += 4;
    return 1;
}

static int write_uint16(Packet *p, ogg_uint16_t val) {
    if (p->pos > p->maxlen-2) {
        return 0;
    }
    p->data[p->pos  ] = (val    ) & 0xFF;
    p->data[p->pos+1] = (val>> 8) & 0xFF;
    p->pos += 2;
    return 1;
}

static int write_chars(Packet *p, const unsigned char *str, int nb_chars)
{
    int i;
    if (p->pos>p->maxlen-nb_chars)
        return 0;
    for (i=0;i<nb_chars;i++)
        p->data[p->pos++] = str[i];
    return 1;
}

static int opus_header_to_packet(const OpusHeader *h, unsigned char *packet, int len) {
    int i;
    Packet p;
    unsigned char ch;

    p.data = packet;
    p.maxlen = len;
    p.pos = 0;
    if (len < 19) {
        return 0;
    }
    if (!write_chars(&p, (const unsigned char *)"OpusHead", 8)) {
        return 0;
    }

    ch = 1;
    if (!write_chars(&p, &ch, 1)) {
        return 0;
    }

    ch = h->channels;
    if (!write_chars(&p, &ch, 1)) {
        return 0;
    }

    if (!write_uint16(&p, h->preskip)) {
        return 0;
    }

    if (!write_uint32(&p, h->input_sample_rate)) {
        return 0;
    }

    if (!write_uint16(&p, h->gain)) {
        return 0;
    }

    ch = h->channel_mapping;
    if (!write_chars(&p, &ch, 1)) {
        return 0;
    }

    if (h->channel_mapping != 0) {
        ch = h->nb_streams;
        if (!write_chars(&p, &ch, 1)) {
            return 0;
        }

        ch = h->nb_coupled;
        if (!write_chars(&p, &ch, 1)) {
            return 0;
        }

        /* Multi-stream support */
        for (i = 0; i < h->channels; i++) {
            if (!write_chars(&p, &h->stream_map[i], 1)) {
                return 0;
            }
        }
    }

    return p.pos;
}

#define writeint(buf, base, val) do { buf[base + 3] = ((val) >> 24) & 0xff; \
buf[base + 2]=((val) >> 16) & 0xff; \
buf[base + 1]=((val) >> 8) & 0xff; \
buf[base] = (val) & 0xff; \
} while(0)

static void comment_init(char **comments, int *length, const char *vendor_string) {
    // The 'vendor' field should be the actual encoding library used
    size_t vendor_length = strlen(vendor_string);
    int user_comment_list_length = 0;
    size_t len = 8 + 4 + vendor_length + 4;
    char *p = new char[len];
    memcpy(p, "OpusTags", 8);
    writeint(p, 8, vendor_length);
    memcpy(p + 12, vendor_string, vendor_length);
    writeint(p, 12 + vendor_length, user_comment_list_length);
    *length = len;
    *comments = p;
}

static void comment_pad(char **comments, int* length, size_t amount) {
    if (amount > 0) {
        char *p = *comments;
        // Make sure there is at least amount worth of padding free, and round up to the maximum that fits in the current ogg segments
        size_t newlen = (*length + amount + 255) / 255 * 255 - 1;
        auto o = new char[newlen];
        memcpy(o, p, *length);
        delete[] p;
        p = o;
        for (int32_t i = *length; i < newlen; i++) {
            p[i] = 0;
        }
        *comments = p;
        *length = newlen;
    }
}

static int writeOggPage(ogg_page *page, FILE *os) {
    int written = (int)fwrite(page->header, sizeof(uint8_t), (size_t) page->header_len, os);
    written += (int)fwrite(page->body, sizeof(uint8_t), (size_t) page->body_len, os);
    return written;
}

const opus_int32 bitrate = 30 * 1024;
static const int opus_application = OPUS_APPLICATION_AUDIO;
const opus_int32 frame_size = 960;
const int max_ogg_delay = 0;
const int comment_padding = 512;

static inline void set_bits(uint8_t *bytes, int32_t bitOffset, int32_t value) {
    bytes += bitOffset / 8;
    bitOffset %= 8;

    auto bits = (uint16_t) (value << bitOffset);
    bytes[0] |= (uint8_t) bits;
    bytes[1] |= (uint8_t) (bits >> 8);
}

int16_t *sampleBuffer = nullptr;

extern "C" JNIEXPORT jbyteArray JNICALL Java_dev_ragnarok_fenrir_module_encoder_AudioRecorderNative_nGetWaveform(JNIEnv *env,
                                                                                                                 jobject clazz, jstring path) {
    const char *pathStr = env->GetStringUTFChars(path, nullptr);
    if (pathStr == nullptr) {
        return nullptr;
    }
    jbyteArray result = nullptr;

    int error = OPUS_OK;
    OggOpusFile *opusFile = op_open_file(pathStr, &error);
    if (opusFile != nullptr && error == OPUS_OK) {
        int64_t totalSamples = op_pcm_total(opusFile, -1);
        const uint32_t resultSamples = 128;
        int32_t sampleRate = max(1, (int32_t) (totalSamples / resultSamples));

        auto *samples = new uint16_t[resultSamples];

        size_t bufferSize = 1024 * 128;
        if (sampleBuffer == nullptr) {
            sampleBuffer = new int16_t[bufferSize / sizeof(int16_t)];
        }

        if (samples != nullptr && sampleBuffer != nullptr) {
            uint64_t sampleIndex = 0;
            uint16_t peakSample = 0;

            int32_t index = 0;
            int decodeFailed = 0;

            while (true) {
                int readSamples = op_read(opusFile, sampleBuffer, bufferSize / 2, nullptr);
                if (readSamples < 0) {
                    LOGE("getWaveform: op_read failed: %d", readSamples);
                    decodeFailed = 1;
                    break;
                }
                if (readSamples == 0) {
                    break;
                }
                for (int32_t i = 0; i < readSamples; i++) {
                    auto sample = (uint16_t) abs(sampleBuffer[i]);
                    if (sample > peakSample) {
                        peakSample = sample;
                    }
                    if (sampleIndex++ % sampleRate == 0) {
                        if (index < resultSamples) {
                            samples[index++] = peakSample;
                        }
                        peakSample = 0;
                    }
                }
            }

            if (!decodeFailed) {
                int64_t sumSamples = 0;
                for (int32_t i = 0; i < resultSamples; i++) {
                    sumSamples += samples[i];
                }
                auto peak = (uint16_t) (sumSamples * 1.8f / resultSamples);
                if (peak < 2500) {
                    peak = 2500;
                }

                for (int32_t i = 0; i < resultSamples; i++) {
                    auto sample = (uint16_t) ((int64_t) samples[i]);
                    if (sample > peak) {
                        samples[i] = peak;
                    }
                }

                result = env->NewByteArray(resultSamples);
                if (result) {
                    auto *bytes = new uint8_t[resultSamples];
                    if (bytes != nullptr) {
                        for (int32_t i = 0; i < resultSamples; i++) {
                            int32_t value = min(31, abs((int32_t) samples[i]) * 31 / peak);

                            bytes[i] = value & 31;
                        }

                        env->SetByteArrayRegion(result, 0, resultSamples, (jbyte *) bytes);
                        delete[] bytes;
                    } else {
                        result = nullptr;
                    }
                }
            }
        }

        delete[] samples;
        op_free(opusFile);
    }

    env->ReleaseStringUTFChars(path, pathStr);
    return result;
}

struct AudioNoiseSuppressorContext {
    DenoiseState *state;
    float *input;
    float *output;
    int frame_size;
};

static void destroyAudioNoiseSuppressorContext(AudioNoiseSuppressorContext *context) {
    if (!context) {
        return;
    }
    if (context->state) {
        rnnoise_destroy(context->state);
    }
    delete[] context->input;
    delete[] context->output;
    delete context;
}

static AudioNoiseSuppressorContext *createAudioNoiseSuppressorContext() {
    auto *context = new AudioNoiseSuppressorContext();
    if (!context) {
        return nullptr;
    }
    context->frame_size = rnnoise_get_frame_size();
    if (context->frame_size <= 0) {
        destroyAudioNoiseSuppressorContext(context);
        return nullptr;
    }
    context->input = new float[context->frame_size];
    context->output = new float[context->frame_size];
    context->state = rnnoise_create(nullptr);
    if (!context->input || !context->output || !context->state) {
        destroyAudioNoiseSuppressorContext(context);
        return nullptr;
    }
    return context;
}

static bool processAudioNoiseSuppressor(
        AudioNoiseSuppressorContext *context,
        int16_t *samples,
        int sampleCount
) {
    if (!context || !samples || sampleCount < 0) {
        return false;
    }
    int sampleOffset = 0;
    while (sampleOffset < sampleCount) {
        int blockSamples = min(context->frame_size, sampleCount - sampleOffset);
        for (int index = 0; index < blockSamples; index++) {
            context->input[index] = samples[sampleOffset + index];
        }
        for (int index = blockSamples; index < context->frame_size; index++) {
            context->input[index] = 0.0f;
        }
        rnnoise_process_frame(context->state, context->output, context->input);
        for (int index = 0; index < blockSamples; index++) {
            if (!isfinite(context->output[index])) {
                return false;
            }
        }
        for (int index = 0; index < blockSamples; index++) {
            float sample = fmaxf(-32768.0f, fminf(32767.0f, context->output[index]));
            samples[sampleOffset + index] = (int16_t) lrintf(sample);
        }
        sampleOffset += blockSamples;
    }
    return true;
}

struct AudioRecorderContext {
    opus_int32 rate;
    opus_int32 coding_rate;
    ogg_int32_t packet_id;
    OpusEncoder *encoder;
    uint8_t *packet;
    ogg_stream_state stream;
    int stream_initialized;
    FILE *file;
    oe_enc_opt options;
    OpusHeader header;
    opus_int32 min_bytes;
    int max_frame_bytes;
    ogg_packet ogg_packet;
    ogg_page page;
    opus_int64 bytes_written;
    opus_int64 pages_out;
    opus_int64 total_samples;
    ogg_int64_t encoded_granule_position;
    ogg_int64_t last_granule_position;
    int last_segments;
    int16_t pending_frame[960];
    int pending_samples;
};

static volatile int32_t recorder_serial = 1;

static void destroyAudioRecorderContext(AudioRecorderContext *recorder) {
    if (!recorder) {
        return;
    }

    if (recorder->stream_initialized) {
        while (ogg_stream_flush(&recorder->stream, &recorder->page)) {
            if (recorder->file) {
                writeOggPage(&recorder->page, recorder->file);
            }
        }
        ogg_stream_clear(&recorder->stream);
    }
    if (recorder->encoder) {
        opus_encoder_destroy(recorder->encoder);
    }
    if (recorder->packet) {
        delete[] recorder->packet;
        recorder->packet = nullptr;
    }
    if (recorder->options.comments) {
        delete[] recorder->options.comments;
        recorder->options.comments = nullptr;
    }
    if (recorder->file) {
        fclose(recorder->file);
    }
    delete recorder;
}

static AudioRecorderContext *createAudioRecorderContext(const std::string &path, opus_int32 sampleRate) {
    if (path.empty()) {
        LOGE("path is null");
        return nullptr;
    }

    auto *recorder = new AudioRecorderContext();
    if (!recorder) {
        LOGE("Unable to allocate audio recorder context");
        return nullptr;
    }

    recorder->packet_id = -1;
    recorder->coding_rate = sampleRate;
    recorder->rate = sampleRate;

    recorder->file = fopen(path.c_str(), "w");
    if (!recorder->file) {
        LOGE("error cannot open file: %s", path.c_str());
        destroyAudioRecorderContext(recorder);
        return nullptr;
    }

    recorder->options.rate = recorder->rate;
    recorder->options.gain = 0;
    recorder->options.endianness = 0;
    recorder->options.samplesize = 16;
    recorder->options.channels = 1;
    recorder->options.skip = 0;

    comment_init(
            &recorder->options.comments,
            &recorder->options.comments_length,
            opus_get_version_string()
    );
    if (!recorder->options.comments) {
        LOGE("Unable to allocate Opus comments");
        destroyAudioRecorderContext(recorder);
        return nullptr;
    }

    if (recorder->rate != recorder->coding_rate) {
        LOGE("Invalid rate");
        destroyAudioRecorderContext(recorder);
        return nullptr;
    }

    recorder->header.channels = 1;
    recorder->header.channel_mapping = 0;
    recorder->header.input_sample_rate = recorder->rate;
    recorder->header.gain = recorder->options.gain;
    recorder->header.nb_streams = 1;

    int result = OPUS_OK;
    recorder->encoder = opus_encoder_create(
            recorder->coding_rate,
            1,
            opus_application,
            &result
    );
    if (result != OPUS_OK) {
        LOGE("Error cannot create encoder: %s", opus_strerror(result));
        destroyAudioRecorderContext(recorder);
        return nullptr;
    }

    recorder->min_bytes = recorder->max_frame_bytes =
            (1275 * 3 + 7) * recorder->header.nb_streams;
    recorder->packet = new uint8_t[recorder->max_frame_bytes];
    if (!recorder->packet) {
        LOGE("Unable to allocate Opus packet buffer");
        destroyAudioRecorderContext(recorder);
        return nullptr;
    }

    result = opus_encoder_ctl(recorder->encoder, OPUS_SET_BITRATE(bitrate));
    //result = opus_encoder_ctl(_encoder, OPUS_SET_COMPLEXITY(10));
    if (result != OPUS_OK) {
        LOGE("Error OPUS_SET_BITRATE returned: %s", opus_strerror(result));
        destroyAudioRecorderContext(recorder);
        return nullptr;
    }

#ifdef OPUS_SET_LSB_DEPTH
    result = opus_encoder_ctl(
            recorder->encoder,
            OPUS_SET_LSB_DEPTH(max(8, min(24, recorder->options.samplesize)))
    );
    if (result != OPUS_OK) {
        LOGE("Warning OPUS_SET_LSB_DEPTH returned: %s", opus_strerror(result));
    }
#endif

    opus_int32 lookahead;
    result = opus_encoder_ctl(recorder->encoder, OPUS_GET_LOOKAHEAD(&lookahead));
    if (result != OPUS_OK) {
        LOGE("Error OPUS_GET_LOOKAHEAD returned: %s", opus_strerror(result));
        destroyAudioRecorderContext(recorder);
        return nullptr;
    }

    recorder->options.skip += lookahead;
    recorder->header.preskip = (int) (
            recorder->options.skip * (48000.0 / recorder->coding_rate)
    );

    int serial = __sync_fetch_and_add(&recorder_serial, 1)
            ^ (int) time(nullptr)
            ^ (int) (uintptr_t) recorder;
    if (ogg_stream_init(&recorder->stream, serial) == -1) {
        LOGE("Error: stream init failed");
        destroyAudioRecorderContext(recorder);
        return nullptr;
    }
    recorder->stream_initialized = 1;

    unsigned char header_data[100];
    int packet_size = opus_header_to_packet(&recorder->header, header_data, 100);
    recorder->ogg_packet.packet = header_data;
    recorder->ogg_packet.bytes = packet_size;
    recorder->ogg_packet.b_o_s = 1;
    recorder->ogg_packet.e_o_s = 0;
    recorder->ogg_packet.granulepos = 0;
    recorder->ogg_packet.packetno = 0;
    ogg_stream_packetin(&recorder->stream, &recorder->ogg_packet);

    while ((result = ogg_stream_flush(&recorder->stream, &recorder->page))) {
        if (!result) {
            break;
        }

        int pageBytesWritten = writeOggPage(&recorder->page, recorder->file);
        if (pageBytesWritten != recorder->page.header_len + recorder->page.body_len) {
            LOGE("Error: failed writing header to output stream");
            destroyAudioRecorderContext(recorder);
            return nullptr;
        }
        recorder->bytes_written += pageBytesWritten;
        recorder->pages_out++;
    }

    comment_pad(
            &recorder->options.comments,
            &recorder->options.comments_length,
            comment_padding
    );
    recorder->ogg_packet.packet = (unsigned char *) recorder->options.comments;
    recorder->ogg_packet.bytes = recorder->options.comments_length;
    recorder->ogg_packet.b_o_s = 0;
    recorder->ogg_packet.e_o_s = 0;
    recorder->ogg_packet.granulepos = 0;
    recorder->ogg_packet.packetno = 1;
    ogg_stream_packetin(&recorder->stream, &recorder->ogg_packet);

    while ((result = ogg_stream_flush(&recorder->stream, &recorder->page))) {
        if (result == 0) {
            break;
        }

        int writtenPageBytes = writeOggPage(&recorder->page, recorder->file);
        if (writtenPageBytes != recorder->page.header_len + recorder->page.body_len) {
            LOGE("Error: failed writing header to output stream");
            destroyAudioRecorderContext(recorder);
            return nullptr;
        }

        recorder->bytes_written += writtenPageBytes;
        recorder->pages_out++;
    }

    delete[] recorder->options.comments;
    recorder->options.comments = nullptr;

    return recorder;
}

// countedSamples is how many of the encoded samples belong to the stream duration; padding
// frames that only flush the encoder lookahead count zero.
static int writeAudioRecorderContextPacket(
        AudioRecorderContext *recorder,
        uint8_t *framePcmBytes,
        uint32_t frameByteCount,
        opus_int32 countedSamples,
        int end
) {
    if (!recorder || !recorder->encoder || !recorder->file) {
        return 0;
    }
    size_t cur_frame_size = frame_size;
    recorder->packet_id++;

    opus_int32 nb_samples = frameByteCount / 2;
    recorder->total_samples += countedSamples;
    recorder->ogg_packet.e_o_s = end;

    int nbBytes = 0;
    int size_segments = 0;

    if (nb_samples != 0) {
        uint8_t *paddedFrameBytes = framePcmBytes;
        int freePaddedFrameBytes = 0;

        if (nb_samples < cur_frame_size) {
            paddedFrameBytes = new uint8_t[cur_frame_size * 2];
            freePaddedFrameBytes = 1;
            memcpy(paddedFrameBytes, framePcmBytes, frameByteCount);
            memset(paddedFrameBytes + nb_samples * 2, 0, cur_frame_size * 2 - nb_samples * 2);
        }

        nbBytes = opus_encode(
                recorder->encoder,
                (opus_int16 *) paddedFrameBytes,
                cur_frame_size,
                recorder->packet,
                recorder->max_frame_bytes / 10
        );
        if (freePaddedFrameBytes) {
            delete[] paddedFrameBytes;
        }

        if (nbBytes < 0) {
            LOGE("Encoding failed: %s. Aborting.", opus_strerror(nbBytes));
            return 0;
        }

        recorder->encoded_granule_position +=
                cur_frame_size * 48000 / recorder->coding_rate;
        size_segments = (nbBytes + 255) / 255;
        recorder->min_bytes = min(nbBytes, recorder->min_bytes);
    }

    while ((((size_segments <= 255)
            && (recorder->last_segments + size_segments > 255))
            || (recorder->encoded_granule_position
            - recorder->last_granule_position > max_ogg_delay))
            && ogg_stream_flush_fill(
                    &recorder->stream,
                    &recorder->page,
                    255 * 255
            )) {
        if (ogg_page_packets(&recorder->page) != 0) {
            recorder->last_granule_position = ogg_page_granulepos(&recorder->page);
        }

        recorder->last_segments -= recorder->page.header[26];
        int writtenPageBytes = writeOggPage(&recorder->page, recorder->file);
        if (writtenPageBytes != recorder->page.header_len + recorder->page.body_len) {
            LOGE("Error: failed writing data to output stream");
            return 0;
        }
        recorder->bytes_written += writtenPageBytes;
        recorder->pages_out++;
    }

    recorder->ogg_packet.packet = recorder->packet;
    recorder->ogg_packet.bytes = nbBytes;
    recorder->ogg_packet.b_o_s = 0;
    recorder->ogg_packet.granulepos = recorder->encoded_granule_position;
    if (recorder->ogg_packet.e_o_s) {
        recorder->ogg_packet.granulepos = (
                (recorder->total_samples * 48000 + recorder->rate - 1)
                / recorder->rate
        ) + recorder->header.preskip;
    }
    recorder->ogg_packet.packetno = 2 + recorder->packet_id;
    ogg_stream_packetin(&recorder->stream, &recorder->ogg_packet);
    recorder->last_segments += size_segments;

    while ((recorder->ogg_packet.e_o_s
            || (recorder->encoded_granule_position
            + (frame_size * 48000 / recorder->coding_rate)
            - recorder->last_granule_position > max_ogg_delay)
            || (recorder->last_segments >= 255))
            ? ogg_stream_flush_fill(&recorder->stream, &recorder->page, 255 * 255)
            : ogg_stream_pageout_fill(&recorder->stream, &recorder->page, 255 * 255)) {
        if (ogg_page_packets(&recorder->page) != 0) {
            recorder->last_granule_position = ogg_page_granulepos(&recorder->page);
        }
        recorder->last_segments -= recorder->page.header[26];
        int writtenPageBytes = writeOggPage(&recorder->page, recorder->file);
        if (writtenPageBytes != recorder->page.header_len + recorder->page.body_len) {
            LOGE("Error: failed writing data to output stream");
            return 0;
        }
        recorder->bytes_written += writtenPageBytes;
        recorder->pages_out++;
    }

    return 1;
}

static int writeAudioRecorderContextFrame(
        AudioRecorderContext *recorder,
        uint8_t *framePcmBytes,
        uint32_t frameByteCount,
        int end
) {
    return writeAudioRecorderContextPacket(
            recorder,
            framePcmBytes,
            frameByteCount,
            (opus_int32) (frameByteCount / 2),
            end
    );
}

// Writes the buffered tail and enough silent frames to push the encoder lookahead out, then
// marks the last packet as end of stream. Its granule position trims the padding, so the
// stream ends exactly after the last captured sample.
static bool finishAudioRecorderStream(AudioRecorderContext *recorder) {
    if (!recorder) {
        return false;
    }
    if (recorder->pending_samples > 0) {
        int pendingSamples = recorder->pending_samples;
        recorder->pending_samples = 0;
        if (!writeAudioRecorderContextFrame(
                recorder,
                (uint8_t *) recorder->pending_frame,
                pendingSamples * sizeof(int16_t),
                0
        )) {
            return false;
        }
    }
    ogg_int64_t endGranulePosition =
            (recorder->total_samples * 48000 + recorder->rate - 1) / recorder->rate
            + recorder->header.preskip;
    int16_t silence[960];
    memset(silence, 0, sizeof(silence));
    while (true) {
        int end = recorder->encoded_granule_position
                + frame_size * 48000 / recorder->coding_rate >= endGranulePosition;
        if (!writeAudioRecorderContextPacket(
                recorder,
                (uint8_t *) silence,
                sizeof(silence),
                0,
                end
        )) {
            return false;
        }
        if (end) {
            return true;
        }
    }
}

static jlong flushAudioRecorderContext(AudioRecorderContext *recorder) {
    if (!recorder || !recorder->file || fflush(recorder->file) != 0) {
        return -1;
    }
    long position = ftell(recorder->file);
    return position < 0 ? -1 : (jlong) position;
}

static bool writeAudioRecorderFrame(
        AudioRecorderContext *recorder,
        uint8_t *pcmBytes,
        uint32_t byteCount
) {
    if (!recorder || (byteCount > 0 && !pcmBytes)) {
        return false;
    }

    auto *samples = (int16_t *) pcmBytes;
    int sampleCount = (int) byteCount / 2;
    int sampleOffset = 0;

    while (sampleOffset < sampleCount) {
        if (recorder->pending_samples == 0
                && sampleCount - sampleOffset >= frame_size) {
            if (!writeAudioRecorderContextFrame(
                    recorder,
                    (uint8_t *) (samples + sampleOffset),
                    frame_size * sizeof(int16_t),
                    0
            )) {
                return false;
            }
            sampleOffset += frame_size;
            continue;
        }

        int copySamples = min(
                frame_size - recorder->pending_samples,
                sampleCount - sampleOffset
        );
        memcpy(
                recorder->pending_frame + recorder->pending_samples,
                samples + sampleOffset,
                copySamples * sizeof(int16_t)
        );
        recorder->pending_samples += copySamples;
        sampleOffset += copySamples;

        if (recorder->pending_samples == frame_size) {
            if (!writeAudioRecorderContextFrame(
                    recorder,
                    (uint8_t *) recorder->pending_frame,
                    frame_size * sizeof(int16_t),
                    0
            )) {
                return false;
            }
            recorder->pending_samples = 0;
        }
    }
    return true;
}

extern "C" JNIEXPORT jlong JNICALL Java_dev_ragnarok_fenrir_module_encoder_AudioRecorderNative_nCreate(
        JNIEnv *env,
        jobject clazz,
        jstring path,
        jint sampleRate
) {
    const char *pathString = env->GetStringUTFChars(path, nullptr);
    if (!pathString) {
        return 0;
    }

    AudioRecorderContext *recorder = createAudioRecorderContext(pathString, sampleRate);
    env->ReleaseStringUTFChars(path, pathString);
    if (!recorder) {
        return 0;
    }

    return reinterpret_cast<jlong>(recorder);
}

extern "C" JNIEXPORT jboolean JNICALL Java_dev_ragnarok_fenrir_module_encoder_AudioRecorderNative_nWriteFrame(
        JNIEnv *env,
        jobject clazz,
        jlong handle,
        jobject frame,
        jint length
) {
    auto *recorder = reinterpret_cast<AudioRecorderContext*>(handle);
    auto *frameBytes = static_cast<jbyte *>(env->GetDirectBufferAddress(frame));
    jlong capacity = env->GetDirectBufferCapacity(frame);
    if (!recorder || !frameBytes || length < 0 || capacity < length) {
        return 0;
    }
    // A short frame is just the tail of the capture; the stream ends only in nFinish.
    return writeAudioRecorderFrame(recorder, (uint8_t *) frameBytes, (uint32_t) length);
}

extern "C" JNIEXPORT jlong JNICALL Java_dev_ragnarok_fenrir_module_encoder_AudioRecorderNative_nFlush(
        JNIEnv *env,
        jobject clazz,
        jlong handle
) {
    return flushAudioRecorderContext(reinterpret_cast<AudioRecorderContext*>(handle));
}

extern "C" JNIEXPORT jboolean JNICALL Java_dev_ragnarok_fenrir_module_encoder_AudioRecorderNative_nFinish(
        JNIEnv *env,
        jobject clazz,
        jlong handle
) {
    auto *recorder = reinterpret_cast<AudioRecorderContext *>(handle);
    if (!recorder) {
        return false;
    }

    bool result = finishAudioRecorderStream(recorder);
    if (result && flushAudioRecorderContext(recorder) < 0) {
        result = false;
    }
    destroyAudioRecorderContext(recorder);
    return result;
}

extern "C" JNIEXPORT void JNICALL Java_dev_ragnarok_fenrir_module_encoder_AudioRecorderNative_nDestroy(
        JNIEnv *env,
        jobject clazz,
        jlong handle
) {
    destroyAudioRecorderContext(reinterpret_cast<AudioRecorderContext *>(handle));
}

extern "C" JNIEXPORT jlong Java_dev_ragnarok_fenrir_module_encoder_AudioNoiseSuppressor_nCreate(
        JNIEnv *env,
        jobject clazz
) {
    return reinterpret_cast<jlong>(createAudioNoiseSuppressorContext());
}

extern "C" JNIEXPORT jboolean JNICALL Java_dev_ragnarok_fenrir_module_encoder_AudioNoiseSuppressor_nProcess(
        JNIEnv *env,
        jobject clazz,
        jlong handle,
        jobject pcm,
        jint sampleCount
) {
    auto *context = reinterpret_cast<AudioNoiseSuppressorContext *>(handle);
    auto *samples = static_cast<int16_t *>(env->GetDirectBufferAddress(pcm));
    jlong capacity = env->GetDirectBufferCapacity(pcm);
    if (!context || !samples || sampleCount < 0
            || capacity < (jlong) sampleCount * sizeof(int16_t)) {
        return false;
    }
    return processAudioNoiseSuppressor(context, samples, sampleCount);
}

extern "C" JNIEXPORT void JNICALL Java_dev_ragnarok_fenrir_module_encoder_AudioNoiseSuppressor_nDestroy(
        JNIEnv *env,
        jobject clazz,
        jlong handle
) {
    destroyAudioNoiseSuppressorContext(reinterpret_cast<AudioNoiseSuppressorContext *>(handle));
}

using AudioCallback =
        std::function<bool(const int16_t* samples, int count)>;

static bool ffmpegDecodeAudioMono(const std::string &filename, int outputSampleRate,
                    const AudioCallback& callback) {
    AVFormatContext* fmt = nullptr;
    AVCodecContext* dec = nullptr;
    SwrContext* swr = nullptr;
    AVPacket* pkt = nullptr;
    AVFrame* frame = nullptr;
    AVChannelLayout outLayout = AV_CHANNEL_LAYOUT_MONO;
    std::vector<int16_t> pcm;
    int ret = 0;

    auto cleanup = [&]() {
        swr_free(&swr);
        av_frame_free(&frame);
        av_packet_free(&pkt);
        avcodec_free_context(&dec);
        avformat_close_input(&fmt);
        av_channel_layout_uninit(&outLayout);
    };

    // 1. Open input.
    ret = avformat_open_input(&fmt, filename.c_str(), nullptr, nullptr);
    if (ret < 0) {
        goto done;
    }

    ret = avformat_find_stream_info(fmt, nullptr);
    if (ret < 0) {
        goto done;
    }

    // 2. Find the audio stream.
    ret = av_find_best_stream(fmt, AVMEDIA_TYPE_AUDIO, -1, -1, nullptr, 0);
    if (ret < 0) {
        goto done;
    }

    {
        int audioStream = ret;
        const AVCodec* codec =
                avcodec_find_decoder(fmt->streams[audioStream]->codecpar->codec_id);

        if (!codec) {
            ret = AVERROR_DECODER_NOT_FOUND;
            goto done;
        }

        dec = avcodec_alloc_context3(codec);
        if (!dec) {
            ret = AVERROR(ENOMEM);
            goto done;
        }

        ret = avcodec_parameters_to_context(dec, fmt->streams[audioStream]->codecpar);
        if (ret < 0) {
            goto done;
        }

        ret = avcodec_open2(dec, codec, nullptr);
        if (ret < 0) {
            goto done;
        }

        // 3. Configure output: mono, S16, chosen sample rate.
        AVChannelLayout inLayout = {};
        if (dec->ch_layout.nb_channels > 0) {
            ret = av_channel_layout_copy(&inLayout, &dec->ch_layout);
        } else {
            av_channel_layout_default(&inLayout, 1);
            ret = 0;
        }

        if (ret < 0) {
            goto done;
        }

        ret = swr_alloc_set_opts2(&swr, &outLayout, AV_SAMPLE_FMT_S16,
                                  outputSampleRate, &inLayout, dec->sample_fmt,
                                  dec->sample_rate, 0, nullptr);

        av_channel_layout_uninit(&inLayout);

        if (ret < 0) {
            goto done;
        }

        ret = swr_init(swr);
        if (ret < 0) {
            goto done;
        }

        pkt = av_packet_alloc();
        frame = av_frame_alloc();

        if (!pkt || !frame) {
            ret = AVERROR(ENOMEM);
            goto done;
        }

        // Decode frames and deliver converted PCM.
        auto receiveFrames = [&]() -> int {
            for (;;) {
                int r = avcodec_receive_frame(dec, frame);

                if (r == AVERROR(EAGAIN) || r == AVERROR_EOF) return 0;
                if (r < 0) return r;

                int capacity = static_cast<int>(av_rescale_rnd(
                        swr_get_delay(swr, dec->sample_rate) + frame->nb_samples,
                        outputSampleRate, dec->sample_rate, AV_ROUND_UP));

                pcm.resize(max(capacity, 1));

                uint8_t* out[] = {reinterpret_cast<uint8_t*>(pcm.data())};

                r = swr_convert(swr, out, capacity,
                                const_cast<const uint8_t**>(frame->extended_data),
                                frame->nb_samples);

                if (r < 0) return r;

                if (r > 0 && !callback(pcm.data(), r)) {
                    av_frame_unref(frame);
                    cleanup();
                    return false;
                }

                av_frame_unref(frame);
            }
        };

        // 4. Read packets and decode audio.
        while ((ret = av_read_frame(fmt, pkt)) >= 0) {
            if (pkt->stream_index == audioStream) {
                ret = avcodec_send_packet(dec, pkt);
                av_packet_unref(pkt);

                if (ret < 0) goto done;

                ret = receiveFrames();
                if (ret < 0) goto done;
            } else {
                av_packet_unref(pkt);
            }
        }

        if (ret != AVERROR_EOF) {
            goto done;
        }

        // 5. Flush the decoder.
        ret = avcodec_send_packet(dec, nullptr);
        if (ret < 0) {
            goto done;
        }

        ret = receiveFrames();
        if (ret < 0) {
            goto done;
        }

        // 6. Flush any delayed resampler samples.
        for (;;) {
            int capacity = static_cast<int>(
                    av_rescale_rnd(swr_get_delay(swr, dec->sample_rate), outputSampleRate,
                                   dec->sample_rate, AV_ROUND_UP));

            if (capacity <= 0) break;

            pcm.resize(capacity);

            uint8_t* out[] = {reinterpret_cast<uint8_t*>(pcm.data())};

            int n = swr_convert(swr, out, capacity, nullptr, 0);

            if (n < 0) {
                ret = n;
                goto done;
            }

            if (n == 0) {
                break;
            }

            if (!callback(pcm.data(), n)) {
                ret = -1;
                goto done;
            }
        }
        ret = 0;
    }

    done:
    cleanup();
    return !ret;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_dev_ragnarok_fenrir_module_encoder_AudioRecorderNative_encodeToOpus(JNIEnv *env, jobject, jstring input, jstring output, jint sampleRate) {
    char const *inputString = SafeGetStringUTFChars(env, input, nullptr);
    char const *outputString = SafeGetStringUTFChars(env, output, nullptr);

    std::string inputFile = inputString;
    std::string outputFile = outputString;

    if (inputString != nullptr) {
        env->ReleaseStringUTFChars(input, inputString);
    }
    if (outputString != nullptr) {
        env->ReleaseStringUTFChars(output, outputString);
    }

    auto recorder = createAudioRecorderContext(outputFile, sampleRate);
    if (!recorder) {
        return false;
    }

    auto callback = [recorder](const int16_t* samples, int count) -> bool {
        return writeAudioRecorderFrame(recorder, (uint8_t *) samples, (uint32_t) count * sizeof(int16_t));
    };

    bool ret = ffmpegDecodeAudioMono(inputFile, sampleRate, callback);
    if (ret) {
        ret = finishAudioRecorderStream(recorder);
        if (ret && flushAudioRecorderContext(recorder) < 0) {
            ret = false;
        }
    }
    destroyAudioRecorderContext(recorder);

    return ret;
}