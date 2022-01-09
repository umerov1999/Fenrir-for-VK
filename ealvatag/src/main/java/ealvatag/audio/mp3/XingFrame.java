package ealvatag.audio.mp3;

import static ealvatag.logging.EalvaTagLog.LogLevel.INFO;
import static ealvatag.logging.EalvaTagLog.LogLevel.TRACE;

import java.io.EOFException;
import java.util.Arrays;

import ealvatag.audio.exceptions.InvalidAudioFrameException;
import ealvatag.logging.EalvaTagLog;
import ealvatag.logging.EalvaTagLog.JLogger;
import ealvatag.logging.EalvaTagLog.JLoggers;
import ealvatag.utils.ArrayUtil;
import okio.Buffer;

/**
 * Xing Frame
 * <p>
 * In some MP3s which variable bit rate the first frame in the file contains a special frame called a Xing Frame,
 * instead of audio data. This is used to store additional information about the file. The most important aspect for
 * this library is details allowing us to determine the bitrate of a Variable Bit Rate VBR file without having
 * to process the whole file.
 * <p>
 * Xing VBR Tag data format is 120 bytes long
 * 4 bytes for Header Tag
 * 4 bytes for Header Flags
 * 4 bytes for FRAME SIZE
 * 4 bytes for AUDIO_SIZE
 * 100 bytes for entry (NUMTOCENTRIES)
 * 4 bytes for VBR SCALE. a VBR quality indicator: 0=best 100=worst
 * <p>
 * It my then contain a Lame Frame ( a Lame frame is in essence an extended Xing Frame
 */
public class XingFrame {
    private static final JLogger LOG = JLoggers.get(XingFrame.class, EalvaTagLog.MARKER);

    //The offset into first frame varies based on the MPEG frame properties
    private static final int MPEG_VERSION_1_MODE_MONO_OFFSET = 21;
    private static final int MPEG_VERSION_1_MODE_STEREO_OFFSET = 36;
    private static final int MPEG_VERSION_2_MODE_MONO_OFFSET = 13;
    private static final int MPEG_VERSION_2_MODE_STEREO_OFFSET = 21;

    private static final int XING_HEADER_BUFFER_SIZE = 120;
    static final int MAX_BUFFER_SIZE_NEEDED_TO_READ_XING =
            MPEG_VERSION_1_MODE_STEREO_OFFSET + XING_HEADER_BUFFER_SIZE + LameFrame.LAME_HEADER_BUFFER_SIZE;
    private static final int XING_IDENTIFIER_BUFFER_SIZE = 4;
    private static final int XING_FLAG_BUFFER_SIZE = 4;
    private static final int XING_FRAMECOUNT_BUFFER_SIZE = 4;
    private static final int XING_AUDIOSIZE_BUFFER_SIZE = 4;
    private static final int TEMP_BUF_SIZE = Math.max(Math.max(Math.max(Math.max(XING_IDENTIFIER_BUFFER_SIZE,
            XING_FLAG_BUFFER_SIZE),
            XING_FRAMECOUNT_BUFFER_SIZE),
            XING_AUDIOSIZE_BUFFER_SIZE),
            LameFrame.ENCODER_SIZE);
    private static final int BYTE_1 = 0;
    private static final int BYTE_2 = 1;
    private static final int BYTE_3 = 2;
    private static final int BYTE_4 = 3;

    /**
     * Use when it is a VBR (Variable Bitrate) file
     */
    private static final byte[] XING_VBR_ID = {'X', 'i', 'n', 'g'};

    /**
     * Use when it is a CBR (Constant Bitrate) file
     */
    private static final byte[] XING_CBR_ID = {'I', 'n', 'f', 'o'};


    private boolean vbr;
    private boolean isFrameCountEnabled;
    private int frameCount = -1;
    private boolean isAudioSizeEnabled;
    private int audioSize = -1;
    private LameFrame lameFrame;

    private XingFrame(Buffer buffer) throws EOFException {
        byte[] tempBuf = new byte[TEMP_BUF_SIZE];

        int skipLess = setVbr(buffer, tempBuf);

        Arrays.fill(tempBuf, ArrayUtil.ZERO);
        buffer.read(tempBuf, 0, XING_FLAG_BUFFER_SIZE);
        skipLess += XING_FLAG_BUFFER_SIZE;

        boolean hasFrameCount = (tempBuf[BYTE_4] & (byte) (1)) != 0;
        boolean readSize = (tempBuf[BYTE_4] & (byte) (1 << 1)) != 0;

        if (hasFrameCount) {
            skipLess += setFrameCount(buffer, tempBuf);
        }

        if (readSize) {
            skipLess += setAudioSize(buffer, tempBuf);
        }

        //TODO TOC
        //TODO VBR Quality

        //Look for LAME Header as long as we have enough bytes to do it properly
        if (buffer.size() >= XING_HEADER_BUFFER_SIZE + LameFrame.LAME_HEADER_BUFFER_SIZE) {
            try {
                buffer.skip(XING_HEADER_BUFFER_SIZE - skipLess);
                lameFrame = LameFrame.parseLameFrame(buffer, tempBuf);
            } catch (EOFException e) {
                LOG.log(INFO, "Not enough room for Lame header", e);
            }
        }
    }

    static XingFrame parseXingFrame(Buffer buffer)
            throws InvalidAudioFrameException, EOFException {
        return new XingFrame(buffer);
    }

    static Buffer isXingFrame(Buffer buffer, MPEGFrameHeader mpegFrameHeader) throws EOFException {

        //Get to Start of where Xing Frame Should be ( we dont know if it is one at this point)
        if (mpegFrameHeader.getVersion() == MPEGFrameHeader.VERSION_1) {
            if (mpegFrameHeader.getChannelMode() == MPEGFrameHeader.MODE_MONO) {
                buffer.skip(MPEG_VERSION_1_MODE_MONO_OFFSET);
            } else {
                buffer.skip(MPEG_VERSION_1_MODE_STEREO_OFFSET);
            }
        }
        //MPEGVersion 2 and 2.5
        else {
            if (mpegFrameHeader.getChannelMode() == MPEGFrameHeader.MODE_MONO) {
                buffer.skip(MPEG_VERSION_2_MODE_MONO_OFFSET);
            } else {
                buffer.skip(MPEG_VERSION_2_MODE_STEREO_OFFSET);
            }
        }

        //Create header from here

        //Check Identifier
        byte[] identifier = new byte[XING_IDENTIFIER_BUFFER_SIZE];
        for (int i = 0; i < identifier.length; i++) {
            identifier[i] = buffer.getByte(i);
        }
        if ((!Arrays.equals(identifier, XING_VBR_ID)) && (!Arrays.equals(identifier, XING_CBR_ID))) {
            return null;
        }
        LOG.log(TRACE, "Found Xing Frame");
        return buffer;
    }

    LameFrame getLameFrame() {
        return lameFrame;
    }

    private int setVbr(Buffer buffer, byte[] tempBuf) {
        //Is it VBR or CBR
        buffer.read(tempBuf, 0, XING_IDENTIFIER_BUFFER_SIZE);
        if (ArrayUtil.equals(tempBuf, XING_VBR_ID, XING_VBR_ID.length)) {
            LOG.log(TRACE, "Is Vbr");
            vbr = true;
        }
        return XING_IDENTIFIER_BUFFER_SIZE;
    }

    private int setFrameCount(Buffer header, byte[] frameCountBuffer)
            throws EOFException {
        frameCount = header.readInt();
        isFrameCountEnabled = true;
//        ArrayUtil.fill(frameCountBuffer, ArrayUtil.ZERO, XING_FRAMECOUNT_BUFFER_SIZE);
//        header.read(frameCountBuffer, 0, XING_FRAMECOUNT_BUFFER_SIZE);
//        isFrameCountEnabled = true;
//        frameCount = (frameCountBuffer[BYTE_1] << 24) & 0xFF000000 | (frameCountBuffer[BYTE_2] << 16) & 0x00FF0000 |
//                (frameCountBuffer[BYTE_3] << 8) & 0x0000FF00 | frameCountBuffer[BYTE_4] & 0x000000FF;
        return XING_FRAMECOUNT_BUFFER_SIZE;
    }

    /**
     * @return true if frameCount has been specified in header
     */
    final boolean isFrameCountEnabled() {
        return isFrameCountEnabled;
    }

    /**
     * @return count of frames
     */
    final int getFrameCount() {
        return frameCount;
    }

    private int setAudioSize(Buffer header, byte[] frameSizeBuffer) throws EOFException {
        audioSize = header.readInt();
        isAudioSizeEnabled = true;
//        ArrayUtil.fill(frameSizeBuffer, ArrayUtil.ZERO, XING_AUDIOSIZE_BUFFER_SIZE);
//        header.read(frameSizeBuffer, 0, XING_AUDIOSIZE_BUFFER_SIZE);
//        isAudioSizeEnabled = true;
//        audioSize = (frameSizeBuffer[BYTE_1] << 24) & 0xFF000000 | (frameSizeBuffer[BYTE_2] << 16) & 0x00FF0000 |
//                (frameSizeBuffer[BYTE_3] << 8) & 0x0000FF00 | frameSizeBuffer[BYTE_4] & 0x000000FF;
        return XING_AUDIOSIZE_BUFFER_SIZE;
    }

    /**
     * @return true if audioSize has been specified in header
     */
    final boolean isAudioSizeEnabled() {
        return isAudioSizeEnabled;
    }

    /**
     * @return size of audio data in bytes
     */
    final int getAudioSize() {
        return audioSize;
    }

    final boolean isVbr() {
        return vbr;
    }

    public String toString() {
        return "xingheader" + " vbr:" + vbr + " frameCountEnabled:" + isFrameCountEnabled + " frameCount:" +
                frameCount + " audioSizeEnabled:" + isAudioSizeEnabled + " audioFileSize:" + audioSize;
    }
}
