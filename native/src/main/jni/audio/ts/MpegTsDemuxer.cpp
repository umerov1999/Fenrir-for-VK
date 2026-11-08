#include "MpegTsDemuxer.h"
#include "Id3Utils.h"
#include <algorithm>
#include <utility>
#include "fenrir_native.h"

MpegTsDemuxer::MpegTsDemuxer(PayloadCallback callback) : m_callback(std::move(callback)) {}

void MpegTsDemuxer::Reset(bool preserveFormat) {
    m_buffer.clear();
    m_audioPid = 0x1FFF;
    m_id3BytesToSkip = 0;
    if (!preserveFormat) {
        m_format = AudioFormat::Unknown;
        m_isTsStreamDetermined = false;
        m_isTsStream = false;
    }
}

AudioFormat MpegTsDemuxer::DetectAudioFormat(const uint8_t *data, size_t size) {
    if (!data || size < 2) return AudioFormat::Unknown;
    for (size_t i = 0; i + 1 < size; ++i) {
        if (data[i] == 0xFF) {
            uint8_t b1 = data[i + 1];
            // MP3: check standard sync words (MPEG-1 / MPEG-2 Layer III: bits 2..1 are 01)
            if ((b1 & 0xE0) == 0xE0 && ((b1 >> 1) & 0x03) == 0x01) {
                return AudioFormat::MP3;
            }
            // ADTS (AAC): 12-bit sync (b1 & 0xF6) == 0xF0 (Layer == 00)
            if ((b1 & 0xF6) == 0xF0) {
                return AudioFormat::AAC_ADTS;
            }
        }
    }
    return AudioFormat::Unknown;
}

void MpegTsDemuxer::DetermineStreamType(const uint8_t *data, size_t size) {
    if (!m_isTsStreamDetermined && data && size > 0) {
        size_t id3Size = Id3Utils::ParseHeaderTotalSize(data, size);
        const uint8_t *checkPtr = data + id3Size;
        size_t checkSize = (size > id3Size) ? (size - id3Size) : 0;
        m_isTsStream = (checkSize > 0 && checkPtr[0] == 0x47);
        m_isTsStreamDetermined = true;
    }
}

void MpegTsDemuxer::ProcessBytes(const uint8_t *data, size_t size) {
    if (!data || size == 0) return;

    if (!m_isTsStreamDetermined) {
        DetermineStreamType(data, size);
    }

    if (!m_isTsStream) {
        size_t id3Size = Id3Utils::ParseHeaderTotalSize(data, size);
        if (id3Size > 0) {
            m_id3BytesToSkip = id3Size;
        }

        if (m_id3BytesToSkip > 0) {
            size_t toSkip = min(m_id3BytesToSkip, size);
            data += toSkip;
            size -= toSkip;
            m_id3BytesToSkip -= toSkip;
            if (size == 0) return;
        }

        if (m_format == AudioFormat::Unknown) m_format = DetectAudioFormat(data, size);
        m_callback(data, size, m_format);
        return;
    }

    m_buffer.insert(m_buffer.end(), data, data + size);
    size_t bytesConsumed = 0;

    while (m_buffer.size() - bytesConsumed >= 188) {
        const uint8_t *tsPacket = m_buffer.data() + bytesConsumed;

        if (tsPacket[0] != 0x47) {
            auto startIt = m_buffer.begin() + bytesConsumed;
            while (startIt != m_buffer.end()) {
                auto it = std::find(startIt, m_buffer.end(), 0x47);
                if (it == m_buffer.end()) {
                    bytesConsumed = m_buffer.size();
                    break;
                }
                size_t candOffset = std::distance(m_buffer.begin(), it);
                // Проверяем следующий маркер пакета на расстоянии 188 байт, если данных в буфере достаточно
                if (m_buffer.size() - candOffset >= 188 * 2) {
                    if (m_buffer[candOffset + 188] == 0x47) {
                        bytesConsumed = candOffset;
                        break;
                    } else {
                        startIt = it + 1;
                        continue;
                    }
                } else {
                    bytesConsumed = candOffset;
                    break;
                }
            }
            continue;
        }

        uint16_t pid = ((tsPacket[1] & 0x1F) << 8) | tsPacket[2];
        uint8_t pusi = (tsPacket[1] & 0x40) >> 6;
        uint8_t afc = (tsPacket[3] & 0x30) >> 4;

        size_t payloadOffset = 4;
        if (afc == 2 || afc == 3) payloadOffset += 1 + tsPacket[4];

        if ((afc == 1 || afc == 3) && payloadOffset < 188) {
            size_t payloadSize = 188 - payloadOffset;
            const uint8_t *payload = tsPacket + payloadOffset;

            if (pusi == 1 && payloadSize >= 9 && payload[0] == 0x00 && payload[1] == 0x00 &&
                payload[2] == 0x01) {
                uint8_t streamId = payload[3];
                static int s_pusiCount = 0;
                if (++s_pusiCount <= 10) {
                    LOGI("MpegTsDemuxer: Demuxer PUSI: pid=%d streamId=0x%d", pid, streamId);
                }

                if (m_audioPid == 0x1FFF && streamId >= 0xC0 && streamId <= 0xDF) {
                    m_audioPid = pid;
                    LOGI("MpegTsDemuxer: Demuxer: Locked to AUDIO PID -> %d", pid);
                }

                if (pid == m_audioPid) {
                    uint8_t pesHeaderLen = payload[8];
                    size_t pesTotalOffset = 9 + pesHeaderLen;
                    if (pesTotalOffset < payloadSize) {
                        payload += pesTotalOffset;
                        payloadSize -= pesTotalOffset;
                    } else {
                        payloadSize = 0;
                    }

                    size_t id3Size = Id3Utils::ParseHeaderTotalSize(payload, payloadSize);
                    if (id3Size > 0) {
                        m_id3BytesToSkip = id3Size;
                    }
                }
            }

            if (pid == m_audioPid && m_id3BytesToSkip > 0 && payloadSize > 0) {
                size_t toSkip = min(m_id3BytesToSkip, payloadSize);
                payload += toSkip;
                payloadSize -= toSkip;
                m_id3BytesToSkip -= toSkip;
            }

            if (payloadSize > 0 && pid == m_audioPid) {
                if (m_format == AudioFormat::Unknown)
                    m_format = DetectAudioFormat(payload, payloadSize);
                m_callback(payload, payloadSize, m_format);
            }
        }
        bytesConsumed += 188;
    }

    if (bytesConsumed >= m_buffer.size()) {
        m_buffer.clear();
    } else if (bytesConsumed > 0) {
        m_buffer.erase(m_buffer.begin(), m_buffer.begin() + bytesConsumed);
    }
}