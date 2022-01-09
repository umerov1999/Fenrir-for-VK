package ealvatag.audio.mp3;

import static ealvatag.logging.EalvaTagLog.LogLevel.TRACE;

import com.google.common.base.MoreObjects;

import java.io.EOFException;
import java.util.Arrays;

import ealvatag.logging.EalvaTagLog;
import ealvatag.logging.EalvaTagLog.JLogger;
import ealvatag.logging.EalvaTagLog.JLoggers;
import okio.Buffer;

/**
 * Vrbi Frame
 * <p>
 * <p>In MP3s encoded using the franhofer encoder which variable bit rate the first frame in the file contains a
 * special frame called a Vrbi Frame, instead of audio data (Other Vbr encoders use the more common Xing Frame).
 * This is used to store additional information about the file. The most important aspect for
 * this library is details allowing us to determine the bitrate of a Variable Bit Rate VBR file without having
 * to process the whole file.
 * <p>
 * From http://www.codeproject.com/KB/audio-video/mpegaudioinfo.aspx#SideInfo
 * <p>
 * This header is only used by MPEG audio files encoded with the Fraunhofer Encoder as far as I know. It is different
 * from the XING header. You find it exactly
 * 32 bytes after the end of the first MPEG audio header in the file. (Note that the position is zero-based;
 * position, length and example are each in byte-format.)
 * Position 	Length 	Meaning 	Example
 * 0    		    4 	VBR header ID in 4 ASCII chars, always 'VBRI', not NULL-terminated 	'VBRI'
 * 4	  			2 	Version ID as Big-Endian WORD 	1
 * 6 				2 	Delay as Big-Endian float 	7344
 * 8 				2 	Quality indicator 	75
 * 10 				4 	Number of Bytes of Audio as Big-Endian DWORD 	45000
 * 14 				4 	Number of Frames as Big-Endian DWORD 	7344
 * 18 				2 	Number of entries within TOC table as Big-Endian WORD 	100
 * 20 				2 	Scale factor of TOC table entries as Big-Endian DWORD 	1
 * 22 				2 	Size per table entry in bytes (max 4) as Big-Endian WORD 	2
 * 24 				2 	Frames per table entry as Big-Endian WORD 	845
 * 26 						TOC entries for seeking as Big-Endian integral. From size per table entry and number of
 * entries, you can calculate the length of this field.
 */
public class VbriFrame {
    private static final JLogger LOG = JLoggers.get(VbriFrame.class, EalvaTagLog.MARKER);

    //The offset into frame
    private static final int VBRI_OFFSET = MPEGFrameHeader.HEADER_SIZE + 32;

    //    private static final int VBRI_HEADER_BUFFER_SIZE = 120; //TODO this is just a guess, not right
    private static final int VBRI_IDENTIFIER_BUFFER_SIZE = 4;
    //    private static final int VBRI_DELAY_BUFFER_SIZE = 2;
//    private static final int VBRI_QUALITY_BUFFER_SIZE = 2;
    private static final int VBRI_AUDIOSIZE_BUFFER_SIZE = 4;
    private static final int VBRI_FRAMECOUNT_BUFFER_SIZE = 4;
//    private static final int VBRI_TOC_ENTRY_BUFFER_SIZE = 2;

//    public static final int MAX_BUFFER_SIZE_NEEDED_TO_READ_VBRI = VBRI_OFFSET + VBRI_HEADER_BUFFER_SIZE;

    private static final int BYTE_1 = 0;
    private static final int BYTE_2 = 1;
    private static final int BYTE_3 = 2;
    private static final int BYTE_4 = 3;

    /**
     * Identifier
     */
    private static final byte[] VBRI_VBR_ID = {'V', 'B', 'R', 'I'};

    private final boolean vbr;
    private int frameCount = -1;
    private int audioSize = -1;

    private VbriFrame(Buffer header) throws EOFException {
        vbr = true;
        header.skip(10);
        setAudioSize(header);
        setFrameCount(header);
    }

    static VbriFrame parseVBRIFrame(Buffer header) throws EOFException {
        return new VbriFrame(header);
    }

    static Buffer isVbriFrame(Buffer buffer) throws EOFException {
        buffer.skip(VBRI_OFFSET);

        //Check Identifier
        byte[] identifier = new byte[VBRI_IDENTIFIER_BUFFER_SIZE];
        for (int i = 0; i < identifier.length; i++) {
            identifier[i] = buffer.getByte(i);
        }
        if ((!Arrays.equals(identifier, VBRI_VBR_ID))) {
            return null;
        }
        LOG.log(TRACE, "Found VBRI Frame");
        return buffer;
    }

    /**
     * @return count of frames
     */
    final int getFrameCount() {
        return frameCount;
    }

    private void setFrameCount(Buffer header) throws EOFException {
        byte[] frameCountBuffer = new byte[VBRI_FRAMECOUNT_BUFFER_SIZE];
        for (int i = 0; i < frameCountBuffer.length; i++) {
            frameCountBuffer[i] = header.readByte();
        }
        frameCount = (frameCountBuffer[BYTE_1] << 24) & 0xFF000000 | (frameCountBuffer[BYTE_2] << 16) & 0x00FF0000 |
                (frameCountBuffer[BYTE_3] << 8) & 0x0000FF00 | frameCountBuffer[BYTE_4] & 0x000000FF;
    }

    /**
     * @return size of audio data in bytes
     */
    final int getAudioSize() {
        return audioSize;
    }

    private void setAudioSize(Buffer header) throws EOFException {
        byte[] frameSizeBuffer = new byte[VBRI_AUDIOSIZE_BUFFER_SIZE];
        for (int i = 0; i < frameSizeBuffer.length; i++) {
            frameSizeBuffer[i] = header.readByte();
        }
        audioSize = (frameSizeBuffer[BYTE_1] << 24) & 0xFF000000 | (frameSizeBuffer[BYTE_2] << 16) & 0x00FF0000 |
                (frameSizeBuffer[BYTE_3] << 8) & 0x0000FF00 | frameSizeBuffer[BYTE_4] & 0x000000FF;
    }

    final boolean isVbr() {
        return true;
    }

    public String getEncoder() {
        return "Fraunhofer";
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("vbr", vbr)
                .add("frameCount", frameCount)
                .add("audioSize", audioSize)
                .toString();
    }
}
