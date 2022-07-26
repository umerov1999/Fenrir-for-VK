package dev.ragnarok.filegallery.util.qr;

import java.util.List;

/**
 * <p>Encapsulates the result of decoding a matrix of bits. This typically
 * applies to 2D barcode formats. For now it contains the raw bytes obtained,
 * as well as a String interpretation of those bytes, if applicable.</p>
 *
 * @author Sean Owen
 */
public final class DecoderResult {

    private final byte[] rawBytes;
    private final String text;
    private final List<byte[]> byteSegments;
    private final String ecLevel;
    private final int structuredAppendParity;
    private final int structuredAppendSequenceNumber;
    private final int symbologyModifier;
    private int numBits;
    private Integer errorsCorrected;
    private Integer erasures;
    private Object other;

    public DecoderResult(byte[] rawBytes,
                         String text,
                         List<byte[]> byteSegments,
                         String ecLevel) {
        this(rawBytes, text, byteSegments, ecLevel, -1, -1, 0);
    }

    public DecoderResult(byte[] rawBytes,
                         String text,
                         List<byte[]> byteSegments,
                         String ecLevel,
                         int symbologyModifier) {
        this(rawBytes, text, byteSegments, ecLevel, -1, -1, symbologyModifier);
    }

    public DecoderResult(byte[] rawBytes,
                         String text,
                         List<byte[]> byteSegments,
                         String ecLevel,
                         int saSequence,
                         int saParity) {
        this(rawBytes, text, byteSegments, ecLevel, saSequence, saParity, 0);
    }

    public DecoderResult(byte[] rawBytes,
                         String text,
                         List<byte[]> byteSegments,
                         String ecLevel,
                         int saSequence,
                         int saParity,
                         int symbologyModifier) {
        this.rawBytes = rawBytes;
        numBits = rawBytes == null ? 0 : 8 * rawBytes.length;
        this.text = text;
        this.byteSegments = byteSegments;
        this.ecLevel = ecLevel;
        structuredAppendParity = saParity;
        structuredAppendSequenceNumber = saSequence;
        this.symbologyModifier = symbologyModifier;
    }

    /**
     * @return raw bytes representing the result, or {@code null} if not applicable
     */
    public byte[] getRawBytes() {
        return rawBytes;
    }

    /**
     * @return how many bits of {@link #getRawBytes()} are valid; typically 8 times its length
     * @since 3.3.0
     */
    public int getNumBits() {
        return numBits;
    }

    /**
     * @param numBits overrides the number of bits that are valid in {@link #getRawBytes()}
     * @since 3.3.0
     */
    public void setNumBits(int numBits) {
        this.numBits = numBits;
    }

    /**
     * @return text representation of the result
     */
    public String getText() {
        return text;
    }

    /**
     * @return list of byte segments in the result, or {@code null} if not applicable
     */
    public List<byte[]> getByteSegments() {
        return byteSegments;
    }

    /**
     * @return name of error correction level used, or {@code null} if not applicable
     */
    public String getECLevel() {
        return ecLevel;
    }

    /**
     * @return number of errors corrected, or {@code null} if not applicable
     */
    public Integer getErrorsCorrected() {
        return errorsCorrected;
    }

    public void setErrorsCorrected(Integer errorsCorrected) {
        this.errorsCorrected = errorsCorrected;
    }

    /**
     * @return number of erasures corrected, or {@code null} if not applicable
     */
    public Integer getErasures() {
        return erasures;
    }

    public void setErasures(Integer erasures) {
        this.erasures = erasures;
    }

    /**
     * @return arbitrary additional metadata
     */
    public Object getOther() {
        return other;
    }

    public void setOther(Object other) {
        this.other = other;
    }

    public boolean hasStructuredAppend() {
        return structuredAppendParity >= 0 && structuredAppendSequenceNumber >= 0;
    }

    public int getStructuredAppendParity() {
        return structuredAppendParity;
    }

    public int getStructuredAppendSequenceNumber() {
        return structuredAppendSequenceNumber;
    }

    public int getSymbologyModifier() {
        return symbologyModifier;
    }

}
