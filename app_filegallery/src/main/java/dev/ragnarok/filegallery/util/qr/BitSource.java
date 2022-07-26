package dev.ragnarok.filegallery.util.qr;

/**
 * <p>This provides an easy abstraction to read bits at a time from a sequence of bytes, where the
 * number of bits read is not often a multiple of 8.</p>
 *
 * <p>This class is thread-safe but not reentrant -- unless the caller modifies the bytes array
 * it passed in, in which case all bets are off.</p>
 *
 * @author Sean Owen
 */
public final class BitSource {

    private final byte[] bytes;
    private int byteOffset;
    private int bitOffset;

    /**
     * @param bytes bytes from which this will read bits. Bits will be read from the first byte first.
     *              Bits are read within a byte from most-significant to least-significant bit.
     */
    public BitSource(byte[] bytes) {
        this.bytes = bytes;
    }

    /**
     * @return index of next bit in current byte which would be read by the next call to {@link #readBits(int)}.
     */
    public int getBitOffset() {
        return bitOffset;
    }

    /**
     * @return index of next byte in input byte array which would be read by the next call to {@link #readBits(int)}.
     */
    public int getByteOffset() {
        return byteOffset;
    }

    /**
     * @param numBits number of bits to read
     * @return int representing the bits read. The bits will appear as the least-significant
     * bits of the int
     * @throws IllegalArgumentException if numBits isn't in [1,32] or more than is available
     */
    public int readBits(int numBits) {
        if (numBits < 1 || numBits > 32 || numBits > available()) {
            throw new IllegalArgumentException(String.valueOf(numBits));
        }

        int result = 0;

        // First, read remainder from current byte
        if (bitOffset > 0) {
            int bitsLeft = 8 - bitOffset;
            int toRead = Math.min(numBits, bitsLeft);
            int bitsToNotRead = bitsLeft - toRead;
            int mask = (0xFF >> (8 - toRead)) << bitsToNotRead;
            result = (bytes[byteOffset] & mask) >> bitsToNotRead;
            numBits -= toRead;
            bitOffset += toRead;
            if (bitOffset == 8) {
                bitOffset = 0;
                byteOffset++;
            }
        }

        // Next read whole bytes
        if (numBits > 0) {
            while (numBits >= 8) {
                result = (result << 8) | (bytes[byteOffset] & 0xFF);
                byteOffset++;
                numBits -= 8;
            }

            // Finally read a partial byte
            if (numBits > 0) {
                int bitsToNotRead = 8 - numBits;
                int mask = (0xFF >> bitsToNotRead) << bitsToNotRead;
                result = (result << numBits) | ((bytes[byteOffset] & mask) >> bitsToNotRead);
                bitOffset += numBits;
            }
        }

        return result;
    }

    /**
     * @return number of bits that can be read successfully
     */
    public int available() {
        return 8 * (bytes.length - byteOffset) - bitOffset;
    }

}
