package ealvatag.tag.id3;

import static ealvatag.logging.EalvaTagLog.LogLevel.DEBUG;
import static ealvatag.logging.ErrorMessage.ID3_UNABLE_TO_DECOMPRESS_FRAME;

import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import ealvatag.logging.EalvaTagLog;
import ealvatag.logging.EalvaTagLog.JLogger;
import ealvatag.logging.EalvaTagLog.JLoggers;
import ealvatag.tag.InvalidFrameException;

/**
 * compresses frame data
 * <p>
 * Is currently required for V23Frames and V24Frames
 */
//TODO also need to support compress framedata
@SuppressWarnings("Duplicates")
class ID3Compression {
    private static final JLogger LOG = JLoggers.get(ID3Compression.class, EalvaTagLog.MARKER);

    /**
     * Decompress realFrameSize bytes to decompressedFrameSize bytes and return as ByteBuffer
     */
    static ByteBuffer uncompress(String identifier,
                                 String filename,
                                 ByteBuffer byteBuffer,
                                 int decompressedFrameSize,
                                 int realFrameSize) throws InvalidFrameException {
        LOG.log(DEBUG, "%s:About to decompress %s bytes, expect result to be:%s bytes", filename, realFrameSize, decompressedFrameSize);
        // Decompress the bytes into this buffer, size initialized from header field
        byte[] result = new byte[decompressedFrameSize];
        byte[] input = new byte[realFrameSize];

        //Store position ( just after frame header and any extra bits)
        //Read frame data into array, and then put buffer back to where it was
        int position = byteBuffer.position();
        byteBuffer.get(input, 0, realFrameSize);
        byteBuffer.position(position);

        Inflater decompresser = new Inflater();
        decompresser.setInput(input);
        try {
            int inflatedTo = decompresser.inflate(result);
            LOG.log(DEBUG, "%s:Decompressed to %s bytes", inflatedTo);
        } catch (DataFormatException dfe) {
            LOG.log(DEBUG, "Unable to decompress this frame:%s", identifier, dfe);

            //Update position of main buffer, so no attempt is made to reread these bytes
            byteBuffer.position(byteBuffer.position() + realFrameSize);
            throw new InvalidFrameException(String.format(Locale.getDefault(), ID3_UNABLE_TO_DECOMPRESS_FRAME, identifier, filename),
                    dfe);
        }
        decompresser.end();
        return ByteBuffer.wrap(result);
    }

//  protected static Buffer uncompress(String identifier,
//                                     String filename,
//                                     Buffer byteBuffer,
//                                     int decompressedFrameSize,
//                                     int realFrameSize) throws InvalidFrameException {
//    LOG.log(LogLevel.DEBUG, "%s:About to decompress %s bytes, expect result to be:%s bytes", filename, realFrameSize,
// decompressedFrameSize);
//    // Decompress the bytes into this buffer, size initialized from header field
//    byte[] result = new byte[decompressedFrameSize];
//    byte[] input = new byte[realFrameSize];
//
//    byteBuffer.read(input, 0, realFrameSize);
//
//    Inflater decompresser = new Inflater();
//    decompresser.setInput(input);
//    try {
//      int inflatedTo = decompresser.inflate(result);
//      LOG.log(LogLevel.DEBUG, filename + ":Decompressed to " + inflatedTo + " bytes");
//    } catch (DataFormatException dfe) {
//      LOG.log(LogLevel.DEBUG, "Unable to decompress this frame:" + identifier, dfe);
//
//      //Update position of main buffer, so no attempt is made to reread these bytes
////            byteBuffer.position(byteBuffer.position() + realFrameSize);
//      throw new InvalidFrameException(exceptionMsg(ID3_UNABLE_TO_DECOMPRESS_FRAME, identifier, filename),
//                                      dfe);
//    }
//    decompresser.end();
//    final Buffer decompressedBuffer = new Buffer();
//    decompressedBuffer.write(result);
//    return decompressedBuffer;
//  }
}
