package ealvatag.tag.datatype;

import static ealvatag.logging.EalvaTagLog.LogLevel.DEBUG;
import static ealvatag.logging.EalvaTagLog.LogLevel.ERROR;
import static ealvatag.logging.EalvaTagLog.LogLevel.TRACE;
import static ealvatag.logging.EalvaTagLog.LogLevel.WARN;

import com.google.common.annotations.VisibleForTesting;

import java.io.EOFException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

import ealvatag.tag.InvalidDataTypeException;
import ealvatag.tag.TagOptionSingleton;
import ealvatag.tag.exceptions.IllegalCharsetException;
import ealvatag.tag.id3.AbstractTagFrameBody;
import ealvatag.utils.StandardCharsets;
import okio.Buffer;

/**
 * Represents a String whose size is determined by finding of a null character at the end of the String.
 * <p>
 * The String itself might be of length zero (i.e just consist of the null character). The String will be encoded based
 * upon the text encoding of the frame that it belongs to.
 */
public class TextEncodedStringNullTerminated extends AbstractString {
    private static final byte NULL_BYTE = (byte) 0x00;

    /**
     * Creates a new TextEncodedStringNullTerminated datatype.
     *
     * @param identifier identifies the frame type
     */
    public TextEncodedStringNullTerminated(String identifier, AbstractTagFrameBody frameBody) {
        super(identifier, frameBody);
    }

    /**
     * Creates a new TextEncodedStringNullTerminated datatype, with value
     */
    public TextEncodedStringNullTerminated(String identifier, AbstractTagFrameBody frameBody, String value) {
        super(identifier, frameBody, value);
    }

    public TextEncodedStringNullTerminated(TextEncodedStringNullTerminated object) {
        super(object);
    }

    /**
     * Finds the index of the first null byte in {@code buffer}. If null may be multi-byte, it's the index of the second null byte.
     *
     * @param buffer        buffer to search, position is not moved
     * @param nullIsOneByte indicates if null is a single byte or 2 bytes
     * @return the index of the null character (if multi-byte, the index of the second null byte)
     */
    @VisibleForTesting
    static int getNullIndex(Buffer buffer, boolean nullIsOneByte) {
        try {
            if (nullIsOneByte) {
                return (int) buffer.indexOf(NULL_BYTE);
            }
            long indexSecondByte = -1;
            long indexFirstByte = getNullEvenIndex(buffer, 0);
            while (-1 == indexSecondByte && -1 != indexFirstByte) {
                if (buffer.getByte(indexFirstByte + 1) == NULL_BYTE) {
                    indexSecondByte = indexFirstByte + 1;
                } else {
                    indexFirstByte = getNullEvenIndex(buffer, indexFirstByte + 2);
                }
            }
            return (int) indexSecondByte;
        } catch (ArrayIndexOutOfBoundsException e) {
            // Let's assume most data is properly null terminated, so this will be rare
            return -1;
        }
    }

    private static long getNullEvenIndex(Buffer buffer, long fromIndex) {
        long index = buffer.indexOf(NULL_BYTE, fromIndex);
        while (-1 != index && !isEven(index)) {
            index = buffer.indexOf(NULL_BYTE, index + 1);
        }
        return index;
    }

    private static boolean isEven(long num) {
        return ((num % 2) == 0);
    }

    public boolean equals(Object obj) {
        return obj instanceof TextEncodedStringNullTerminated && super.equals(obj);
    }

    /**
     * Read a string from buffer upto null character (if exists)
     * <p>
     * Must take into account the text encoding defined in the Encoding Object
     * ID3 Text Frames often allow multiple strings separated by the null char
     * appropriate for the encoding.
     *
     * @param arr    this is the buffer for the frame
     * @param offset this is where to start reading in the buffer for this field
     */
    public void readByteArray(byte[] arr, int offset) throws InvalidDataTypeException {
        if (offset >= arr.length) {
            throw new InvalidDataTypeException("Unable to find null terminated string");
        }
        int bufferSize;

        LOG.log(DEBUG, "Reading from array starting from offset:%s", offset);
        int size;

        //Get the Specified Decoder
        Charset charset = getTextEncodingCharSet();


        //We only want to load up to null terminator, data after this is part of different
        //field and it may not be possible to decode it so do the check before we do
        //do the decoding,encoding dependent.
        ByteBuffer buffer = ByteBuffer.wrap(arr, offset, arr.length - offset);
        int endPosition = 0;

        //Latin-1 and UTF-8 strings are terminated by a single-byte null,
        //while UTF-16 and its variants need two bytes for the null terminator.
        boolean nullIsOneByte = StandardCharsets.ISO_8859_1 == charset || StandardCharsets.UTF_8 == charset;

        boolean isNullTerminatorFound = false;
        while (buffer.hasRemaining()) {
            byte nextByte = buffer.get();
            if (nextByte == 0x00) {
                if (nullIsOneByte) {
                    buffer.mark();
                    buffer.reset();
                    endPosition = buffer.position() - 1;
                    LOG.log(TRACE, "Null terminator found starting at:%s", endPosition);

                    isNullTerminatorFound = true;
                    break;
                } else {
                    // Looking for two-byte null
                    if (buffer.hasRemaining()) {
                        nextByte = buffer.get();
                        if (nextByte == 0x00) {
                            buffer.mark();
                            buffer.reset();
                            endPosition = buffer.position() - 2;
                            LOG.log(TRACE, "UTF16:Null terminator found starting at:%s", endPosition);
                            isNullTerminatorFound = true;
                            break;
                        } //Nothing to do, we have checked 2nd value of pair it was not a null terminator
                        //so will just start looking again in next invocation of loop
                    } else {
                        buffer.mark();
                        buffer.reset();
                        endPosition = buffer.position() - 1;
                        LOG.log(WARN, "UTF16:Should be two null terminator marks but only found one starting at:%s", endPosition);

                        isNullTerminatorFound = true;
                        break;
                    }
                }
            } else {
                //If UTF16, we should only be looking on 2 byte boundaries
                if (!nullIsOneByte) {
                    if (buffer.hasRemaining()) {
                        buffer.get();
                    }
                }
            }
        }

        if (!isNullTerminatorFound) {
            throw new InvalidDataTypeException("Unable to find null terminated string");
        }


        LOG.log(TRACE, "End Position is:%s Offset:%s", endPosition, offset);

        //Set Size so offset is ready for next field (includes the null terminator)
        size = endPosition - offset;
        size++;
        if (!nullIsOneByte) {
            size++;
        }
        setSize(size);

        //Decode buffer if runs into problems should throw exception which we
        //catch and then set value to empty string. (We don't read the null terminator
        //because we dont want to display this)
        bufferSize = endPosition - offset;
        LOG.log(TRACE, "Text size is:%s", bufferSize);
        if (bufferSize == 0) {
            value = "";
        } else {
            //Decode sliced inBuffer
            ByteBuffer inBuffer = ByteBuffer.wrap(arr, offset, bufferSize).slice();
            CharBuffer outBuffer = CharBuffer.allocate(bufferSize);

            CharsetDecoder decoder = getCorrectDecoder(inBuffer);
            CoderResult coderResult = decoder.decode(inBuffer, outBuffer, true);
            if (coderResult.isError()) {
                LOG.log(WARN, "Problem decoding text encoded null terminated string:%s", coderResult.toString());
            }
            decoder.flush(outBuffer);
            outBuffer.flip();
            value = outBuffer.toString();
        }
        //Set Size so offset is ready for next field (includes the null terminator)
        LOG.log(DEBUG, "Read NullTerminatedString:%s size inc terminator:%s", value, size);
    }

    @Override
    public void read(Buffer buffer, int size) throws EOFException, InvalidDataTypeException {
        try {
            Charset charset = getTextEncodingCharSet();
            boolean nullIsOneByte = StandardCharsets.ISO_8859_1 == charset || StandardCharsets.UTF_8 == charset;
            int indexOfNull = getNullIndex(buffer, nullIsOneByte);

            if (indexOfNull < 0) {
                throw new InvalidDataTypeException("Can't find null string terminator");
            }
            setSize(indexOfNull + 1);
            int byteCount = nullIsOneByte ? indexOfNull : indexOfNull - 1;
            value = buffer.readString(byteCount, charset);
            buffer.readByte();
            if (!nullIsOneByte) {
                buffer.readByte();
            }
        } catch (IllegalCharsetException e) {
            throw new InvalidDataTypeException(e, "Bad charset Id");
        }
    }

    /**
     * Write String into byte array, adding a null character to the end of the String
     *
     * @return the data as a byte array in format to write to file
     */
    public byte[] writeByteArray() {
        LOG.log(DEBUG, "Writing NullTerminatedString. %s", value != null ? value : "null");
        byte[] data;
        //Write to buffer using the CharSet defined by getTextEncodingCharSet()
        //Add a null terminator which will be encoded based on encoding.
        Charset charset = getTextEncodingCharSet();
        try {
            if (StandardCharsets.UTF_16.equals(charset)) {
                if (TagOptionSingleton.getInstance().isEncodeUTF16BomAsLittleEndian()) {
                    CharsetEncoder encoder = StandardCharsets.UTF_16LE.newEncoder();
                    encoder.onMalformedInput(CodingErrorAction.IGNORE);
                    encoder.onUnmappableCharacter(CodingErrorAction.IGNORE);

                    //Note remember LE BOM is ff fe but this is handled by encoder Unicode char is fe ff
                    ByteBuffer bb = encoder.encode(CharBuffer.wrap('\ufeff' + (String) value + '\0'));
                    data = new byte[bb.limit()];
                    bb.get(data, 0, bb.limit());
                } else {
                    CharsetEncoder encoder = StandardCharsets.UTF_16BE.newEncoder();
                    encoder.onMalformedInput(CodingErrorAction.IGNORE);
                    encoder.onUnmappableCharacter(CodingErrorAction.IGNORE);

                    //Note  BE BOM will leave as fe ff
                    ByteBuffer bb = encoder.encode(CharBuffer.wrap('\ufeff' + (String) value + '\0'));
                    data = new byte[bb.limit()];
                    bb.get(data, 0, bb.limit());
                }
            } else {
                CharsetEncoder encoder = charset.newEncoder();
                encoder.onMalformedInput(CodingErrorAction.IGNORE);
                encoder.onUnmappableCharacter(CodingErrorAction.IGNORE);

                ByteBuffer bb = encoder.encode(CharBuffer.wrap((String) value + '\0'));
                data = new byte[bb.limit()];
                bb.get(data, 0, bb.limit());
            }
        }
        //https://bitbucket.org/ijabz/jaudiotagger/issue/1/encoding-metadata-to-utf-16-can-fail-if
        catch (CharacterCodingException ce) {
            LOG.log(ERROR, "Character encoding, charset:%s value:%s", charset, value, ce);
            throw new RuntimeException(ce);
        }
        setSize(data.length);
        return data;
    }
}
