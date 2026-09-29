#ifndef sha1_h
#define sha1_h

//#define HAS_SHA1_FSTREAM

#include <cstdint>
#include <iomanip>
#include <iostream>
#include <sstream>
#include <cstring>
#include <string>

#ifdef HAS_SHA1_FSTREAM
#include <fstream>
#endif

#if defined(__x86_64__) || defined(__i386__) || defined(_M_X64) || defined(_M_IX86)
#if defined(_MSC_VER)
#include <immintrin.h>
#else
#include <x86intrin.h>
#include <cpuid.h>
#endif
#elif defined(__aarch64__)

#include <arm_neon.h>
#include <asm/hwcap.h>
#include <linux/auxvec.h>
#include <sys/auxv.h>

#endif

class SHA1 {
public:
    SHA1() {
        reset();
    }

    void update(const std::string &s) {
        update(s.c_str(), s.size());
    }

#ifdef HAS_SHA1_FSTREAM
    void update(std::istream& is) {
        char sbuf[MAX_STREAM_SIZE];
        while (true) {
            is.read(sbuf, MAX_STREAM_SIZE);
            update(sbuf, (size_t)is.gcount());

            if ((size_t)is.gcount() != BLOCK_BYTES) {
                return;
            }
        }
    }
#endif

    void update(const void *buf, size_t size) {
        const auto *p = static_cast<const uint8_t *>(buf);

        total_size += size;

        if (buffer_size > 0) {
            size_t n = BLOCK_BYTES - buffer_size;
            if (n > size) {
                n = size;
            }

            memcpy(buffer + buffer_size, p, n);

            buffer_size += n;
            p += n;
            size -= n;

            if (buffer_size == BLOCK_BYTES) {
                sha1_process(state, buffer, BLOCK_BYTES);
                buffer_size = 0;
            }
        }

        if (size >= BLOCK_BYTES) {
            size_t blocks_size = size & ~size_t(BLOCK_BYTES - 1);

            sha1_process(state, p, blocks_size);

            p += blocks_size;
            size -= blocks_size;
        }

        if (size > 0) {
            memcpy(buffer, p, size);
            buffer_size = size;
        }
    }

    std::string final() {
        const uint64_t bit_length = total_size * 8;

        // SHA-1 padding.
        buffer[buffer_size++] = 0x80;

        if (buffer_size > BLOCK_BYTES - 8) {
            memset(buffer + buffer_size, 0, BLOCK_BYTES - buffer_size);
            sha1_process(state, buffer, BLOCK_BYTES);
            buffer_size = 0;
        }

        memset(buffer + buffer_size, 0, (BLOCK_BYTES - 8) - buffer_size);

        for (int i = 0; i < 8; ++i) {
            buffer[(BLOCK_BYTES - 8) + i] =
                    static_cast<uint8_t>(bit_length >> ((BLOCK_BYTES - 8) - i * 8));
        }

        sha1_process(state, buffer, BLOCK_BYTES);

        std::string result = to_hex_bytes(5, state);
        reset();
        return result;
    }

#ifdef HAS_SHA1_FSTREAM
    static std::string from_file(const std::string& filename) {
        std::ifstream stream(filename, std::ios::binary);
        SHA1 checksum;
        checksum.update(stream);
        return checksum.final();
    }
#endif

    static std::string from_string(const std::string &str) {
        SHA1 checksum;
        checksum.update(str);
        return checksum.final();
    }

private:
    static const size_t BLOCK_INTS = 16;  /* number of 32bit integers per SHA1 block */
    static const size_t BLOCK_BYTES = BLOCK_INTS * 4;
    static const size_t MAX_STREAM_SIZE = 8192;

    static bool cpu_has_sha1() {
        static bool checked = false;
        static bool last_check = false;
        if (checked) {
            return last_check;
        }
        uint32_t max_leaf;
        uint32_t ebx;

#if defined(_MSC_VER)
        int32_t r[4];

        __cpuid(r, 0);
        max_leaf = (uint32_t)r[0];
        if (max_leaf < 7) {
            checked = true;
            last_check = false;
            return last_check;
        }
        __cpuidex(r, 7, 0);
        ebx = (uint32_t)r[1];
#elif !defined(__aarch64__) && !defined(__ARM_NEON__) && (defined(__GNUC__) || defined(__clang__))
        uint32_t eax, ebx_, ecx, edx;

        if (!__get_cpuid(0, &eax, &ebx_, &ecx, &edx)) {
            checked = true;
            last_check = false;
            return last_check;
        }
        max_leaf = eax;
        if (max_leaf < 7) {
            checked = true;
            last_check = false;
            return last_check;
        }
        if (!__get_cpuid_count(7, 0, &eax, &ebx_, &ecx, &edx)) {
            checked = true;
            last_check = false;
            return last_check;
        }
        ebx = ebx_;
#elif defined(__aarch64__)
        checked = true;
        last_check = (getauxval(AT_HWCAP) & HWCAP_SHA1) != 0;
        return last_check;
#else
        checked = true;
        last_check = false;
        return last_check;
#endif
        checked = true;
        last_check = (ebx & (1u << 29)) != 0;
        return last_check;
    }

#if defined(__x86_64__) || defined(__i386__) || defined(_M_X64) || defined(_M_IX86)
    static void sha1_process_intrinsic(uint32_t state[5], const uint8_t data[], size_t length) {
        __m128i ABCD, ABCD_SAVE, E0, E0_SAVE, E1;
        __m128i MSG0, MSG1, MSG2, MSG3;
        const __m128i MASK = _mm_set_epi64x(0x0001020304050607ULL, 0x08090a0b0c0d0e0fULL);

        /* Load initial values */
        ABCD = _mm_loadu_si128((const __m128i*) state);
        E0 = _mm_set_epi32(state[4], 0, 0, 0);
        ABCD = _mm_shuffle_epi32(ABCD, 0x1B);

        while (length >= 64) {
            /* Save current state  */
            ABCD_SAVE = ABCD;
            E0_SAVE = E0;

            /* Rounds 0-3 */
            MSG0 = _mm_loadu_si128((const __m128i*)(data + 0));
            MSG0 = _mm_shuffle_epi8(MSG0, MASK);
            E0 = _mm_add_epi32(E0, MSG0);
            E1 = ABCD;
            ABCD = _mm_sha1rnds4_epu32(ABCD, E0, 0);

            /* Rounds 4-7 */
            MSG1 = _mm_loadu_si128((const __m128i*)(data + 16));
            MSG1 = _mm_shuffle_epi8(MSG1, MASK);
            E1 = _mm_sha1nexte_epu32(E1, MSG1);
            E0 = ABCD;
            ABCD = _mm_sha1rnds4_epu32(ABCD, E1, 0);
            MSG0 = _mm_sha1msg1_epu32(MSG0, MSG1);

            /* Rounds 8-11 */
            MSG2 = _mm_loadu_si128((const __m128i*)(data + 32));
            MSG2 = _mm_shuffle_epi8(MSG2, MASK);
            E0 = _mm_sha1nexte_epu32(E0, MSG2);
            E1 = ABCD;
            ABCD = _mm_sha1rnds4_epu32(ABCD, E0, 0);
            MSG1 = _mm_sha1msg1_epu32(MSG1, MSG2);
            MSG0 = _mm_xor_si128(MSG0, MSG2);

            /* Rounds 12-15 */
            MSG3 = _mm_loadu_si128((const __m128i*)(data + 48));
            MSG3 = _mm_shuffle_epi8(MSG3, MASK);
            E1 = _mm_sha1nexte_epu32(E1, MSG3);
            E0 = ABCD;
            MSG0 = _mm_sha1msg2_epu32(MSG0, MSG3);
            ABCD = _mm_sha1rnds4_epu32(ABCD, E1, 0);
            MSG2 = _mm_sha1msg1_epu32(MSG2, MSG3);
            MSG1 = _mm_xor_si128(MSG1, MSG3);

            /* Rounds 16-19 */
            E0 = _mm_sha1nexte_epu32(E0, MSG0);
            E1 = ABCD;
            MSG1 = _mm_sha1msg2_epu32(MSG1, MSG0);
            ABCD = _mm_sha1rnds4_epu32(ABCD, E0, 0);
            MSG3 = _mm_sha1msg1_epu32(MSG3, MSG0);
            MSG2 = _mm_xor_si128(MSG2, MSG0);

            /* Rounds 20-23 */
            E1 = _mm_sha1nexte_epu32(E1, MSG1);
            E0 = ABCD;
            MSG2 = _mm_sha1msg2_epu32(MSG2, MSG1);
            ABCD = _mm_sha1rnds4_epu32(ABCD, E1, 1);
            MSG0 = _mm_sha1msg1_epu32(MSG0, MSG1);
            MSG3 = _mm_xor_si128(MSG3, MSG1);

            /* Rounds 24-27 */
            E0 = _mm_sha1nexte_epu32(E0, MSG2);
            E1 = ABCD;
            MSG3 = _mm_sha1msg2_epu32(MSG3, MSG2);
            ABCD = _mm_sha1rnds4_epu32(ABCD, E0, 1);
            MSG1 = _mm_sha1msg1_epu32(MSG1, MSG2);
            MSG0 = _mm_xor_si128(MSG0, MSG2);

            /* Rounds 28-31 */
            E1 = _mm_sha1nexte_epu32(E1, MSG3);
            E0 = ABCD;
            MSG0 = _mm_sha1msg2_epu32(MSG0, MSG3);
            ABCD = _mm_sha1rnds4_epu32(ABCD, E1, 1);
            MSG2 = _mm_sha1msg1_epu32(MSG2, MSG3);
            MSG1 = _mm_xor_si128(MSG1, MSG3);

            /* Rounds 32-35 */
            E0 = _mm_sha1nexte_epu32(E0, MSG0);
            E1 = ABCD;
            MSG1 = _mm_sha1msg2_epu32(MSG1, MSG0);
            ABCD = _mm_sha1rnds4_epu32(ABCD, E0, 1);
            MSG3 = _mm_sha1msg1_epu32(MSG3, MSG0);
            MSG2 = _mm_xor_si128(MSG2, MSG0);

            /* Rounds 36-39 */
            E1 = _mm_sha1nexte_epu32(E1, MSG1);
            E0 = ABCD;
            MSG2 = _mm_sha1msg2_epu32(MSG2, MSG1);
            ABCD = _mm_sha1rnds4_epu32(ABCD, E1, 1);
            MSG0 = _mm_sha1msg1_epu32(MSG0, MSG1);
            MSG3 = _mm_xor_si128(MSG3, MSG1);

            /* Rounds 40-43 */
            E0 = _mm_sha1nexte_epu32(E0, MSG2);
            E1 = ABCD;
            MSG3 = _mm_sha1msg2_epu32(MSG3, MSG2);
            ABCD = _mm_sha1rnds4_epu32(ABCD, E0, 2);
            MSG1 = _mm_sha1msg1_epu32(MSG1, MSG2);
            MSG0 = _mm_xor_si128(MSG0, MSG2);

            /* Rounds 44-47 */
            E1 = _mm_sha1nexte_epu32(E1, MSG3);
            E0 = ABCD;
            MSG0 = _mm_sha1msg2_epu32(MSG0, MSG3);
            ABCD = _mm_sha1rnds4_epu32(ABCD, E1, 2);
            MSG2 = _mm_sha1msg1_epu32(MSG2, MSG3);
            MSG1 = _mm_xor_si128(MSG1, MSG3);

            /* Rounds 48-51 */
            E0 = _mm_sha1nexte_epu32(E0, MSG0);
            E1 = ABCD;
            MSG1 = _mm_sha1msg2_epu32(MSG1, MSG0);
            ABCD = _mm_sha1rnds4_epu32(ABCD, E0, 2);
            MSG3 = _mm_sha1msg1_epu32(MSG3, MSG0);
            MSG2 = _mm_xor_si128(MSG2, MSG0);

            /* Rounds 52-55 */
            E1 = _mm_sha1nexte_epu32(E1, MSG1);
            E0 = ABCD;
            MSG2 = _mm_sha1msg2_epu32(MSG2, MSG1);
            ABCD = _mm_sha1rnds4_epu32(ABCD, E1, 2);
            MSG0 = _mm_sha1msg1_epu32(MSG0, MSG1);
            MSG3 = _mm_xor_si128(MSG3, MSG1);

            /* Rounds 56-59 */
            E0 = _mm_sha1nexte_epu32(E0, MSG2);
            E1 = ABCD;
            MSG3 = _mm_sha1msg2_epu32(MSG3, MSG2);
            ABCD = _mm_sha1rnds4_epu32(ABCD, E0, 2);
            MSG1 = _mm_sha1msg1_epu32(MSG1, MSG2);
            MSG0 = _mm_xor_si128(MSG0, MSG2);

            /* Rounds 60-63 */
            E1 = _mm_sha1nexte_epu32(E1, MSG3);
            E0 = ABCD;
            MSG0 = _mm_sha1msg2_epu32(MSG0, MSG3);
            ABCD = _mm_sha1rnds4_epu32(ABCD, E1, 3);
            MSG2 = _mm_sha1msg1_epu32(MSG2, MSG3);
            MSG1 = _mm_xor_si128(MSG1, MSG3);

            /* Rounds 64-67 */
            E0 = _mm_sha1nexte_epu32(E0, MSG0);
            E1 = ABCD;
            MSG1 = _mm_sha1msg2_epu32(MSG1, MSG0);
            ABCD = _mm_sha1rnds4_epu32(ABCD, E0, 3);
            MSG3 = _mm_sha1msg1_epu32(MSG3, MSG0);
            MSG2 = _mm_xor_si128(MSG2, MSG0);

            /* Rounds 68-71 */
            E1 = _mm_sha1nexte_epu32(E1, MSG1);
            E0 = ABCD;
            MSG2 = _mm_sha1msg2_epu32(MSG2, MSG1);
            ABCD = _mm_sha1rnds4_epu32(ABCD, E1, 3);
            MSG3 = _mm_xor_si128(MSG3, MSG1);

            /* Rounds 72-75 */
            E0 = _mm_sha1nexte_epu32(E0, MSG2);
            E1 = ABCD;
            MSG3 = _mm_sha1msg2_epu32(MSG3, MSG2);
            ABCD = _mm_sha1rnds4_epu32(ABCD, E0, 3);

            /* Rounds 76-79 */
            E1 = _mm_sha1nexte_epu32(E1, MSG3);
            E0 = ABCD;
            ABCD = _mm_sha1rnds4_epu32(ABCD, E1, 3);

            /* Combine state */
            E0 = _mm_sha1nexte_epu32(E0, E0_SAVE);
            ABCD = _mm_add_epi32(ABCD, ABCD_SAVE);

            data += 64;
            length -= 64;
        }

        /* Save state */
        ABCD = _mm_shuffle_epi32(ABCD, 0x1B);
        _mm_storeu_si128((__m128i*) state, ABCD);
        state[4] = _mm_extract_epi32(E0, 3);
    }
#elif defined(__aarch64__)

    static void sha1_process_intrinsic(uint32_t state[5], const uint8_t data[], size_t length) {
        uint32x4_t ABCD, ABCD_SAVED;
        uint32x4_t TMP0, TMP1;
        uint32x4_t MSG0, MSG1, MSG2, MSG3;
        uint32_t E0, E0_SAVED, E1;

        /* Load state */
        ABCD = vld1q_u32(&state[0]);
        E0 = state[4];

        while (length >= 64) {
            /* Save state */
            ABCD_SAVED = ABCD;
            E0_SAVED = E0;

            /* Load message */
            MSG0 = vld1q_u32((const uint32_t *) (data));
            MSG1 = vld1q_u32((const uint32_t *) (data + 16));
            MSG2 = vld1q_u32((const uint32_t *) (data + 32));
            MSG3 = vld1q_u32((const uint32_t *) (data + 48));

            /* Reverse for little endian */
            MSG0 = vreinterpretq_u32_u8(vrev32q_u8(vreinterpretq_u8_u32(MSG0)));
            MSG1 = vreinterpretq_u32_u8(vrev32q_u8(vreinterpretq_u8_u32(MSG1)));
            MSG2 = vreinterpretq_u32_u8(vrev32q_u8(vreinterpretq_u8_u32(MSG2)));
            MSG3 = vreinterpretq_u32_u8(vrev32q_u8(vreinterpretq_u8_u32(MSG3)));

            TMP0 = vaddq_u32(MSG0, vdupq_n_u32(0x5A827999));
            TMP1 = vaddq_u32(MSG1, vdupq_n_u32(0x5A827999));

            /* Rounds 0-3 */
            E1 = vsha1h_u32(vgetq_lane_u32(ABCD, 0));
            ABCD = vsha1cq_u32(ABCD, E0, TMP0);
            TMP0 = vaddq_u32(MSG2, vdupq_n_u32(0x5A827999));
            MSG0 = vsha1su0q_u32(MSG0, MSG1, MSG2);

            /* Rounds 4-7 */
            E0 = vsha1h_u32(vgetq_lane_u32(ABCD, 0));
            ABCD = vsha1cq_u32(ABCD, E1, TMP1);
            TMP1 = vaddq_u32(MSG3, vdupq_n_u32(0x5A827999));
            MSG0 = vsha1su1q_u32(MSG0, MSG3);
            MSG1 = vsha1su0q_u32(MSG1, MSG2, MSG3);

            /* Rounds 8-11 */
            E1 = vsha1h_u32(vgetq_lane_u32(ABCD, 0));
            ABCD = vsha1cq_u32(ABCD, E0, TMP0);
            TMP0 = vaddq_u32(MSG0, vdupq_n_u32(0x5A827999));
            MSG1 = vsha1su1q_u32(MSG1, MSG0);
            MSG2 = vsha1su0q_u32(MSG2, MSG3, MSG0);

            /* Rounds 12-15 */
            E0 = vsha1h_u32(vgetq_lane_u32(ABCD, 0));
            ABCD = vsha1cq_u32(ABCD, E1, TMP1);
            TMP1 = vaddq_u32(MSG1, vdupq_n_u32(0x6ED9EBA1));
            MSG2 = vsha1su1q_u32(MSG2, MSG1);
            MSG3 = vsha1su0q_u32(MSG3, MSG0, MSG1);

            /* Rounds 16-19 */
            E1 = vsha1h_u32(vgetq_lane_u32(ABCD, 0));
            ABCD = vsha1cq_u32(ABCD, E0, TMP0);
            TMP0 = vaddq_u32(MSG2, vdupq_n_u32(0x6ED9EBA1));
            MSG3 = vsha1su1q_u32(MSG3, MSG2);
            MSG0 = vsha1su0q_u32(MSG0, MSG1, MSG2);

            /* Rounds 20-23 */
            E0 = vsha1h_u32(vgetq_lane_u32(ABCD, 0));
            ABCD = vsha1pq_u32(ABCD, E1, TMP1);
            TMP1 = vaddq_u32(MSG3, vdupq_n_u32(0x6ED9EBA1));
            MSG0 = vsha1su1q_u32(MSG0, MSG3);
            MSG1 = vsha1su0q_u32(MSG1, MSG2, MSG3);

            /* Rounds 24-27 */
            E1 = vsha1h_u32(vgetq_lane_u32(ABCD, 0));
            ABCD = vsha1pq_u32(ABCD, E0, TMP0);
            TMP0 = vaddq_u32(MSG0, vdupq_n_u32(0x6ED9EBA1));
            MSG1 = vsha1su1q_u32(MSG1, MSG0);
            MSG2 = vsha1su0q_u32(MSG2, MSG3, MSG0);

            /* Rounds 28-31 */
            E0 = vsha1h_u32(vgetq_lane_u32(ABCD, 0));
            ABCD = vsha1pq_u32(ABCD, E1, TMP1);
            TMP1 = vaddq_u32(MSG1, vdupq_n_u32(0x6ED9EBA1));
            MSG2 = vsha1su1q_u32(MSG2, MSG1);
            MSG3 = vsha1su0q_u32(MSG3, MSG0, MSG1);

            /* Rounds 32-35 */
            E1 = vsha1h_u32(vgetq_lane_u32(ABCD, 0));
            ABCD = vsha1pq_u32(ABCD, E0, TMP0);
            TMP0 = vaddq_u32(MSG2, vdupq_n_u32(0x8F1BBCDC));
            MSG3 = vsha1su1q_u32(MSG3, MSG2);
            MSG0 = vsha1su0q_u32(MSG0, MSG1, MSG2);

            /* Rounds 36-39 */
            E0 = vsha1h_u32(vgetq_lane_u32(ABCD, 0));
            ABCD = vsha1pq_u32(ABCD, E1, TMP1);
            TMP1 = vaddq_u32(MSG3, vdupq_n_u32(0x8F1BBCDC));
            MSG0 = vsha1su1q_u32(MSG0, MSG3);
            MSG1 = vsha1su0q_u32(MSG1, MSG2, MSG3);

            /* Rounds 40-43 */
            E1 = vsha1h_u32(vgetq_lane_u32(ABCD, 0));
            ABCD = vsha1mq_u32(ABCD, E0, TMP0);
            TMP0 = vaddq_u32(MSG0, vdupq_n_u32(0x8F1BBCDC));
            MSG1 = vsha1su1q_u32(MSG1, MSG0);
            MSG2 = vsha1su0q_u32(MSG2, MSG3, MSG0);

            /* Rounds 44-47 */
            E0 = vsha1h_u32(vgetq_lane_u32(ABCD, 0));
            ABCD = vsha1mq_u32(ABCD, E1, TMP1);
            TMP1 = vaddq_u32(MSG1, vdupq_n_u32(0x8F1BBCDC));
            MSG2 = vsha1su1q_u32(MSG2, MSG1);
            MSG3 = vsha1su0q_u32(MSG3, MSG0, MSG1);

            /* Rounds 48-51 */
            E1 = vsha1h_u32(vgetq_lane_u32(ABCD, 0));
            ABCD = vsha1mq_u32(ABCD, E0, TMP0);
            TMP0 = vaddq_u32(MSG2, vdupq_n_u32(0x8F1BBCDC));
            MSG3 = vsha1su1q_u32(MSG3, MSG2);
            MSG0 = vsha1su0q_u32(MSG0, MSG1, MSG2);

            /* Rounds 52-55 */
            E0 = vsha1h_u32(vgetq_lane_u32(ABCD, 0));
            ABCD = vsha1mq_u32(ABCD, E1, TMP1);
            TMP1 = vaddq_u32(MSG3, vdupq_n_u32(0xCA62C1D6));
            MSG0 = vsha1su1q_u32(MSG0, MSG3);
            MSG1 = vsha1su0q_u32(MSG1, MSG2, MSG3);

            /* Rounds 56-59 */
            E1 = vsha1h_u32(vgetq_lane_u32(ABCD, 0));
            ABCD = vsha1mq_u32(ABCD, E0, TMP0);
            TMP0 = vaddq_u32(MSG0, vdupq_n_u32(0xCA62C1D6));
            MSG1 = vsha1su1q_u32(MSG1, MSG0);
            MSG2 = vsha1su0q_u32(MSG2, MSG3, MSG0);

            /* Rounds 60-63 */
            E0 = vsha1h_u32(vgetq_lane_u32(ABCD, 0));
            ABCD = vsha1pq_u32(ABCD, E1, TMP1);
            TMP1 = vaddq_u32(MSG1, vdupq_n_u32(0xCA62C1D6));
            MSG2 = vsha1su1q_u32(MSG2, MSG1);
            MSG3 = vsha1su0q_u32(MSG3, MSG0, MSG1);

            /* Rounds 64-67 */
            E1 = vsha1h_u32(vgetq_lane_u32(ABCD, 0));
            ABCD = vsha1pq_u32(ABCD, E0, TMP0);
            TMP0 = vaddq_u32(MSG2, vdupq_n_u32(0xCA62C1D6));
            MSG3 = vsha1su1q_u32(MSG3, MSG2);
            MSG0 = vsha1su0q_u32(MSG0, MSG1, MSG2);

            /* Rounds 68-71 */
            E0 = vsha1h_u32(vgetq_lane_u32(ABCD, 0));
            ABCD = vsha1pq_u32(ABCD, E1, TMP1);
            TMP1 = vaddq_u32(MSG3, vdupq_n_u32(0xCA62C1D6));
            MSG0 = vsha1su1q_u32(MSG0, MSG3);

            /* Rounds 72-75 */
            E1 = vsha1h_u32(vgetq_lane_u32(ABCD, 0));
            ABCD = vsha1pq_u32(ABCD, E0, TMP0);

            /* Rounds 76-79 */
            E0 = vsha1h_u32(vgetq_lane_u32(ABCD, 0));
            ABCD = vsha1pq_u32(ABCD, E1, TMP1);

            /* Combine state */
            E0 += E0_SAVED;
            ABCD = vaddq_u32(ABCD_SAVED, ABCD);

            data += 64;
            length -= 64;
        }

        /* Save state */
        vst1q_u32(&state[0], ABCD);
        state[4] = E0;
    }

#else
    static void sha1_process_intrinsic(uint32_t state[5], const uint8_t data[], size_t length) {}
#endif

    static inline uint32_t rol32(uint32_t x, uint32_t n) {
        return (x << n) | (x >> (32 - n));
    }

    static void sha1_process(uint32_t state[5], const uint8_t data[], size_t length) {
        if (cpu_has_sha1()) {
            sha1_process_intrinsic(state, data, length);
            return;
        }
        static constexpr uint32_t K[4] = {
                0x5A827999u, // 0 <= t <= 19
                0x6ED9EBA1u, // 20 <= t <= 39
                0x8F1BBCDCu, // 40 <= t <= 59
                0xCA62C1D6u  // 60 <= t <= 79
        };

        while (length >= 64) {
            uint32_t w[80];

            for (uint32_t i = 0; i < 16; ++i) {
                w[i] =
                        (static_cast<uint32_t>(data[i * 4 + 0]) << 24) |
                        (static_cast<uint32_t>(data[i * 4 + 1]) << 16) |
                        (static_cast<uint32_t>(data[i * 4 + 2]) << 8) |
                        (static_cast<uint32_t>(data[i * 4 + 3]));
            }

            for (uint32_t i = 16; i < 80; ++i) {
                w[i] = rol32(
                        w[i - 3] ^ w[i - 8] ^ w[i - 14] ^ w[i - 16],
                        1
                );
            }

            auto a = state[0];
            auto b = state[1];
            auto c = state[2];
            auto d = state[3];
            auto e = state[4];

            for (uint32_t i = 0; i < 80; ++i) {
                uint32_t f;

                if (i < 20) {
                    f = (b & c) | ((~b) & d);
                } else if (i < 60 && i >= 40) {
                    f = (b & c) | (b & d) | (c & d);
                } else {
                    f = b ^ c ^ d;
                }

                const auto k_index = i / 20;

                const auto temp =
                        rol32(a, 5) +
                        f +
                        e +
                        w[i] +
                        K[k_index];

                e = d;
                d = c;
                c = rol32(b, 30);
                b = a;
                a = temp;
            }

            state[0] += a;
            state[1] += b;
            state[2] += c;
            state[3] += d;
            state[4] += e;

            data += 64;
            length -= 64;
        }
    }

    void reset() {
        state[0] = 0x67452301u;
        state[1] = 0xEFCDAB89u;
        state[2] = 0x98BADCFEu;
        state[3] = 0x10325476u;
        state[4] = 0xC3D2E1F0u;

        buffer_size = 0;
        total_size = 0;
        memset(buffer, 0, sizeof(buffer));
    }

    template<typename U>
    static std::string to_hex_bytes(size_t count, const U *arr) {
        if (count <= 0) {
            return "";
        }

        const char hexChars[16] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c',
                                   'd', 'e', 'f'};
        std::string result;
        result.reserve(count * sizeof(U) * 2);
        for (size_t s = 0; s < count; s++) {
            auto buf = (uint8_t *) arr;
            for (auto i = (int) sizeof(U) - 1; i >= 0; i--) {
                result.push_back(*(hexChars + ((*(buf + i + s * sizeof(U)) & 0xF0) >> 4)));
                result.push_back(*(hexChars + (*(buf + i + s * sizeof(U)) & 0xF)));
            }
        }
        return result;
    }

    uint8_t buffer[BLOCK_BYTES];

    uint32_t state[5];
    uint64_t total_size;
    size_t buffer_size;
};

#endif