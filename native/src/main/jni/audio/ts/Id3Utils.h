#ifndef Id3Utils_h
#define Id3Utils_h

#include <vector>
#include <cstdint>
#include <cstddef>

/**
 * @brief Общие утилиты для работы с заголовками и метаданными ID3v2
 * (DRY-утилита с корректной побитовой маской 0x7F для SyncSafe целых чисел).
 */
namespace Id3Utils {

/**
 * @brief Парсит 4-байтовое SyncSafe целое число в uint32_t (каждый байт содержит 7 значащих бит).
 * @param data Указатель на массив как минимум из 4 байт.
 */
    inline uint32_t ParseSyncSafeSize(const uint8_t *data) noexcept {
        if (!data) return 0;
        return ((static_cast<uint32_t>(data[0]) & 0x7F) << 21) |
               ((static_cast<uint32_t>(data[1]) & 0x7F) << 14) |
               ((static_cast<uint32_t>(data[2]) & 0x7F) << 7) |
               (static_cast<uint32_t>(data[3]) & 0x7F);
    }

/**
 * @brief Проверяет наличие ID3v2 заголовка и возвращает полный размер тега (10 байт заголовка + размер данных).
 * @param data Указатель на входной буфер.
 * @param size Размер входного буфера.
 * @return Полный размер ID3v2 блока в байтах, либо 0, если заголовок отсутствует или некорректен.
 */
    inline size_t ParseHeaderTotalSize(const uint8_t *data, size_t size) noexcept {
        if (!data || size < 10) return 0;
        if (data[0] == 'I' && data[1] == 'D' && data[2] == '3') {
            uint32_t tagSize = ParseSyncSafeSize(data + 6);
            return 10 + static_cast<size_t>(tagSize);
        }
        return 0;
    }

/**
 * @brief Формирует 4-байтовый SyncSafe QByteArray из 32-битного целого числа.
 */
    inline std::vector<uint8_t> MakeSyncSafe(uint32_t size) {
        std::vector<uint8_t> b(4);
        b[0] = static_cast<char>((size >> 21) & 0x7F);
        b[1] = static_cast<char>((size >> 14) & 0x7F);
        b[2] = static_cast<char>((size >> 7) & 0x7F);
        b[3] = static_cast<char>(size & 0x7F);
        return b;
    }

} // namespace Id3Utils
#endif