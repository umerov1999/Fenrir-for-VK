/*
 * @author : Paul Taylor
 * <p>
 * Version @version:$Id$
 * <p>
 * MusicTag Copyright (C)2003,2004
 * <p>
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public  License as
 * published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * <p>
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, you can get a copy from
 * http://www.opensource.org/licenses/lgpl-license.php or write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA 02110-1301 USA
 */
package ealvatag.audio.mp3;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static ealvatag.logging.EalvaTagLog.LogLevel.DEBUG;
import static ealvatag.logging.EalvaTagLog.LogLevel.ERROR;
import static ealvatag.logging.EalvaTagLog.LogLevel.TRACE;
import static ealvatag.logging.EalvaTagLog.LogLevel.WARN;

import com.google.common.base.MoreObjects;

import java.io.EOFException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import ealvatag.audio.AudioHeader;
import ealvatag.audio.exceptions.InvalidAudioFrameException;
import ealvatag.audio.io.FileOperator;
import ealvatag.logging.EalvaTagLog;
import ealvatag.logging.EalvaTagLog.JLogger;
import ealvatag.logging.EalvaTagLog.JLoggers;
import ealvatag.logging.ErrorMessage;
import ealvatag.utils.TimeUnits;
import okio.Buffer;

/**
 * Represents the audio header of an MP3 File
 * <p>
 * <p>The audio header consists of a number of
 * audio frames. Because we are not trying to play the audio but only extract some information
 * regarding the audio we only need to read the first  audio frames to ensure that we have correctly
 * identified them as audio frames and extracted the metadata we require.
 * <p>
 * <p>Start of Audio id 0xFF (11111111) and then second byte and with 0xE0(11100000).
 * For example 2nd byte doesn't have to be 0xE0 is just has to have the top 3 significant
 * bits set. For example 0xFB (11111011) is a common occurrence of the second match. The 2nd byte
 * defines flags to indicate various mp3 values.
 * <p>
 * <p>Having found these two values we then read the header which comprises these two bytes plus a further
 * two to ensure this really is a MP3Header, sometimes the first frame is actually a dummy frame with summary
 * information
 * held within about the whole file, typically using a Xing Header or LAme Header. This is most useful when the file
 * is variable bit rate, if the file is variable bit rate but does not use a summary header it will not be correctly
 * identified as a VBR frame and the track length will be incorrectly calculated. Strictly speaking MP3 means
 * Layer III file but MP2 Layer II), MP1 Layer I) and MPEG-2 files are sometimes used and named with
 * the .mp3 suffix so this library attempts to supports all these formats.
 */
@SuppressWarnings("unused")
public class MP3AudioHeader implements AudioHeader {
    private static final double NANOSECONDS_IN_A_SECOND = 1000000000;
    private static final int CONVERT_TO_KILOBITS = 1000;
    private static final String TYPE_MP3 = "mp3";
    private static final int CONVERTS_BYTE_TO_BITS = 8;
    /**
     * After testing the average location of the first MP3Header bit was at 5000 bytes so this is
     * why chosen as a default.
     */
    private final static int FILE_BUFFER_SIZE = 5000;
    private final static int MIN_BUFFER_REMAINING_REQUIRED =
            MPEGFrameHeader.HEADER_SIZE + XingFrame.MAX_BUFFER_SIZE_NEEDED_TO_READ_XING;
    private static final int NO_SECONDS_IN_HOUR = 3600;
    //Logger
    public static JLogger LOG = JLoggers.get(MP3AudioHeader.class, EalvaTagLog.MARKER);
    MPEGFrameHeader mp3FrameHeader;
    private XingFrame mp3XingFrame;
    private VbriFrame mp3VbriFrame;
    private Long audioDataStartPosition;
    private Long audioDataEndPosition;
    private long fileSize;
    private long startByte;
    private double timePerFrame;
    private double trackLength;
    private long numberOfFrames;
    private long numberOfFramesEstimate;
    private int bitrate;
    private String encoder = "";

    MP3AudioHeader(FileOperator fileOperator, long startByte, String fileName) throws IOException,
            InvalidAudioFrameException {
        if (!seek(fileOperator, startByte, fileName)) {
            LOG.log(ERROR, ErrorMessage.NO_AUDIO_HEADER_FOUND, fileName);
            throw new InvalidAudioFrameException(ErrorMessage.NO_AUDIO_HEADER_FOUND, fileName);
        }
    }

    /**
     * Returns true if the first MP3 frame can be found for the MP3 file
     * <p>
     * This is the first byte of  music data and not the ID3 Tag Frame.     *
     *
     * @param fileOperator for reading source file
     * @param startByte    if there is an ID3v2tag we dont want to start reading from the start of the tag
     * @return true if the first MP3 frame can be found
     * @throws IOException on any I/O error
     */
    public boolean seek(FileOperator fileOperator, long startByte, String fileName)
            throws IOException {
        //This is substantially faster than updating the filechannels position
        long fileSize = fileOperator.getFileChannel().size();
        long filePointerCount;

        //Update filePointerCount
        filePointerCount = startByte;

        Buffer buffer = new Buffer();
        //Read from here into the byte buffer , doesn't move location of filepointer
        long byteCount = Math.max(Math.min(FILE_BUFFER_SIZE, fileSize - filePointerCount), 0);
        fileOperator.read(filePointerCount, buffer, byteCount);

        boolean syncFound;
        try {
            while (true) {
                if (buffer.size() <= MIN_BUFFER_REMAINING_REQUIRED) {
                    buffer.clear();
                    byteCount = Math.max(Math.min(FILE_BUFFER_SIZE, fileSize - filePointerCount), 0);
                    fileOperator.read(filePointerCount, buffer, byteCount);
                    if (buffer.size() <= MIN_BUFFER_REMAINING_REQUIRED) {
                        //No mp3 exists
                        return false;
                    }
                }

                if (MPEGFrameHeader.isMPEGFrame(buffer)) {  // doesn't move buffer position
                    try {
                        LOG.log(TRACE, "Found Possible header at:%s", filePointerCount);

                        mp3FrameHeader = MPEGFrameHeader.parseMPEGHeader(buffer);  // doesn't move buffer position
                        syncFound = true;

                        //if(2==1) use this line when you want to test getting the next frame without using xing

                        Buffer xingFrameBuffer = XingFrame.isXingFrame(buffer.clone(), mp3FrameHeader);
                        if (xingFrameBuffer != null) {
                            LOG.log(TRACE, "Found Possible XingHeader");
                            try {
                                mp3XingFrame = XingFrame.parseXingFrame(xingFrameBuffer);
                                xingFrameBuffer.skip(xingFrameBuffer.size());
                            } catch (InvalidAudioFrameException ex) {
                                // We Ignore because even if Xing Header is corrupted
                                //doesn't mean file is corrupted
                            }
                            break;
                        }

                        Buffer vbriFrameBuffer = VbriFrame.isVbriFrame(buffer.clone());
                        if (vbriFrameBuffer != null) {
                            LOG.log(TRACE, "Found Possible VbriHeader");
                            mp3VbriFrame = VbriFrame.parseVBRIFrame(vbriFrameBuffer);
                            vbriFrameBuffer.skip(vbriFrameBuffer.size());
                            break;
                        }

                        // There is a small but real chance that an unsynchronised ID3 Frame could fool the MPEG
                        // Parser into thinking it was an MPEG Header. If this happens the chances of the next bytes
                        // forming a Xing frame header are very remote. On the basis that  most files these days have
                        // Xing headers we do an additional check for when an apparent frame header has been found
                        // but is not followed by a Xing Header:We check the next header this wont impose a large
                        // overhead because wont apply to most Mpegs anyway ( Most likely to occur if audio
                        // has an  APIC frame which should have been unsynchronised but has not been) , or if the frame
                        // has been encoded with as Unicode LE because these have a BOM of 0xFF 0xFE
                        syncFound = isNextFrameValid(filePointerCount, buffer.clone(), fileOperator, fileName);
                        if (syncFound) {
                            break;
                        }

                    } catch (InvalidAudioFrameException ex) {
                        // We Ignore because likely to be incorrect sync bits ,
                        // will just continue in loop
                    }
                }

                buffer.readByte();  // move 1 byte further in
                filePointerCount++;


            }
        } catch (EOFException ex) {
            LOG.log(WARN, "Reached end of file without finding sync match", ex);
            syncFound = false;
        } catch (IOException iox) {
            LOG.log(ERROR, "IOException occurred while trying to find sync", iox);
            throw iox;
        }

        //Return to start of audio header
        LOG.log(TRACE, "Return found matching mp3 header starting at %s", filePointerCount);
        setFileSize(fileOperator.getFileChannel().size());
        setMp3StartByte(filePointerCount);
        setTimePerFrame();
        setNumberOfFrames();
        setTrackLength();
        setBitRate();
        setEncoder();
        return syncFound;
    }

    private boolean isNextFrameValid(long filePointerCount, Buffer bb, FileOperator fileOperator, String seekFileName)
            throws IOException {
        LOG.log(TRACE, "Checking next frame %s:fpc:%sskipping to:%s",
                seekFileName,
                filePointerCount,
                (filePointerCount + mp3FrameHeader.getFrameLength()));
        boolean result = false;

        long fileSize = fileOperator.getFileChannel().size();

        //Our buffer is not large enough to fit in the whole of this frame, something must
        //have gone wrong because frames are not this large, so just return false
        //bad frame header
        if (mp3FrameHeader.getFrameLength() > (FILE_BUFFER_SIZE - MIN_BUFFER_REMAINING_REQUIRED)) {
            LOG.log(DEBUG, "Frame size is too large to be a frame:%s", mp3FrameHeader.getFrameLength());
            return false;
        }

        //Check for end of buffer if not enough room get some more
        if (bb.size() <= MIN_BUFFER_REMAINING_REQUIRED + mp3FrameHeader.getFrameLength()) {
            LOG.log(DEBUG, "Buffer too small, need to reload, buffer size:%s", bb.size());
            bb.clear();
            long byteCount = Math.max(Math.min(FILE_BUFFER_SIZE, fileSize - filePointerCount), 0);
            fileOperator.read(filePointerCount, bb, byteCount);
            //Not enough left
            if (bb.size() <= MIN_BUFFER_REMAINING_REQUIRED) {
                //No mp3 exists
                LOG.log(DEBUG, "Nearly at end of file, no header found:");
                return false;
            }
        }

        //Position bb to the start of the alleged next frame
        bb.skip(mp3FrameHeader.getFrameLength());
        if (MPEGFrameHeader.isMPEGFrame(bb)) {
            try {
                MPEGFrameHeader.parseMPEGHeader(bb);
                LOG.log(DEBUG, "Check next frame confirms is an audio header ");
                result = true;
            } catch (InvalidAudioFrameException ex) {
                LOG.log(DEBUG, "Check next frame has identified this is not an audio header");
                result = false;
            }
        } else {
            LOG.log(DEBUG, "isMPEGFrame has identified this is not an audio header");
        }
        return result;
    }

    /**
     * Returns the byte position of the first MP3 Frame that the
     * <code>file</code> arguement refers to. This is the first byte of music
     * data and not the ID3 Tag Frame.
     *
     * @return the byte position of the first MP3 Frame
     */
    public long getMp3StartByte() {
        return startByte;
    }

    /**
     * Set the location of where the Audio file begins in the file
     */
    void setMp3StartByte(long startByte) {
        this.startByte = startByte;
    }

    /**
     * Set number of frames in this file, use Xing if exists otherwise ((File Size - Non Audio Part)/Frame Size)
     */
    private void setNumberOfFrames() {
        numberOfFramesEstimate = (fileSize - startByte) / mp3FrameHeader.getFrameLength();

        if (mp3XingFrame != null && mp3XingFrame.isFrameCountEnabled()) {
            numberOfFrames = mp3XingFrame.getFrameCount();
        } else if (mp3VbriFrame != null) {
            numberOfFrames = mp3VbriFrame.getFrameCount();
        } else {
            numberOfFrames = numberOfFramesEstimate;
        }

    }

    /**
     * @return The number of frames within the Audio File, calculated as accurately as possible
     */
    long getNumberOfFrames() {
        return numberOfFrames;
    }

    @Override
    public long getNoOfSamples() {
        return numberOfFrames;
    }

    /**
     * @return The number of frames within the Audio File, calculated by dividing the filesize by the number of frames, this may not be the
     * most accurate method available.
     */
    public long getNumberOfFramesEstimate() {
        return numberOfFramesEstimate;
    }

    /**
     * Set the time each frame contributes to the audio in fractions of seconds, the higher
     * the sampling rate the shorter the audio segment provided by the frame,
     * the number of samples is fixed by the MPEG Version and Layer
     */
    private void setTimePerFrame() {
        timePerFrame = mp3FrameHeader.getNoOfSamples() / mp3FrameHeader.getSamplingRate().doubleValue();

        //Because when calculating framelength we may have altered the calculation slightly for MPEGVersion2
        //to account for mono/stereo we seem to have to make a corresponding modification to get the correct time
        if ((mp3FrameHeader.getVersion() == MPEGFrameHeader.VERSION_2) ||
                (mp3FrameHeader.getVersion() == MPEGFrameHeader.VERSION_2_5)) {
            if ((mp3FrameHeader.getLayer() == MPEGFrameHeader.LAYER_II) ||
                    (mp3FrameHeader.getLayer() == MPEGFrameHeader.LAYER_III)) {
                if (mp3FrameHeader.getNumberOfChannels() == 1) {
                    timePerFrame = timePerFrame / 2;
                }
            }
        }
    }

    /**
     * @return the the time each frame contributes to the audio in fractions of seconds
     */
    private double getTimePerFrame() {
        return timePerFrame;
    }

    /**
     * Estimate the length of the audio track in seconds
     * Calculation is Number of frames multiplied by the Time Per Frame using the first frame as a prototype
     * Time Per Frame is the number of samples in the frame (which is defined by the MPEGVersion/Layer combination)
     * divided by the sampling rate, i.e the higher the sampling rate the shorter the audio represented by the frame
     * is going
     * to be.
     */
    private void setTrackLength() {
        trackLength = numberOfFrames * getTimePerFrame();
    }


    /**
     * @return Track Length in seconds
     */
    @Override
    public double getDurationAsDouble() {
        return trackLength;
    }

    @Override
    public long getDuration(TimeUnit timeUnit, boolean round) {
        return TimeUnits.convert(Math.round(trackLength * NANOSECONDS_IN_A_SECOND), NANOSECONDS, timeUnit, round);
    }

    /**
     * @return the audio file type
     */
    public String getEncodingType() {
        return TYPE_MP3;
    }

    /**
     * Set bitrate in kbps, if Vbr use Xingheader if possible
     */
    protected void setBitRate() {

        if (mp3XingFrame != null && mp3XingFrame.isVbr()) {
            if (mp3XingFrame.isAudioSizeEnabled() && mp3XingFrame.getAudioSize() > 0) {
                bitrate = (int) ((mp3XingFrame.getAudioSize() * CONVERTS_BYTE_TO_BITS) /
                        (timePerFrame * getNumberOfFrames() * CONVERT_TO_KILOBITS));
            } else {
                bitrate = (int) (((fileSize - startByte) * CONVERTS_BYTE_TO_BITS) /
                        (timePerFrame * getNumberOfFrames() * CONVERT_TO_KILOBITS));
            }
        } else if (mp3VbriFrame != null) {
            if (mp3VbriFrame.getAudioSize() > 0) {
                bitrate = (int) ((mp3VbriFrame.getAudioSize() * CONVERTS_BYTE_TO_BITS) /
                        (timePerFrame * getNumberOfFrames() * CONVERT_TO_KILOBITS));
            } else {
                bitrate = (int) (((fileSize - startByte) * CONVERTS_BYTE_TO_BITS) /
                        (timePerFrame * getNumberOfFrames() * CONVERT_TO_KILOBITS));
            }
        } else {
            bitrate = mp3FrameHeader.getBitRate();
        }
    }

    private void setEncoder() {
        if (mp3XingFrame != null) {
            if (mp3XingFrame.getLameFrame() != null) {
                encoder = mp3XingFrame.getLameFrame().getEncoder();
            }
        } else if (mp3VbriFrame != null) {
            encoder = mp3VbriFrame.getEncoder();
        }
    }

    /**
     * @return bitrate in kbps, no indicator is provided as to whether or not it is vbr
     */
    public int getBitRate() {
        return bitrate;
    }


    /**
     * @return the sampling rate in Hz
     */
    public int getSampleRate() {
        return mp3FrameHeader.getSamplingRate();
    }

    /**
     * @return the number of bits per sample
     */
    public int getBitsPerSample() {
        //TODO: can it really be different in such an MP3 ? I think not.
        return 16;
    }

    /**
     * @return MPEG Version (1-3)
     */
    public String getMpegVersion() {
        return mp3FrameHeader.getVersionAsString();
    }

    /**
     * @return MPEG Layer (1-3)
     */
    public String getMpegLayer() {
        return mp3FrameHeader.getLayerAsString();
    }

    /**
     * @return the format of the audio (i.e. MPEG-1 Layer3)
     */
    public String getFormat() {
        return mp3FrameHeader.getVersionAsString() + " " + mp3FrameHeader.getLayerAsString();
    }

    @Override
    public int getChannelCount() {
        return mp3FrameHeader.getNumberOfChannels();
    }

    /**
     * @return Emphasis
     */
    public String getEmphasis() {
        return mp3FrameHeader.getEmphasisAsString();
    }

    /**
     * @return if the bitrate is variable, Xing header takes precedence if we have one
     */
    public boolean isVariableBitRate() {
        if (mp3XingFrame != null) {
            return mp3XingFrame.isVbr();
        } else if (mp3VbriFrame != null) {
            return mp3VbriFrame.isVbr();
        } else {
            return mp3FrameHeader.isVariableBitRate();
        }
    }

    public boolean isProtected() {
        return mp3FrameHeader.isProtected();
    }

    public boolean isPrivate() {
        return mp3FrameHeader.isPrivate();
    }

    public boolean isCopyrighted() {
        return mp3FrameHeader.isCopyrighted();
    }

    public boolean isOriginal() {
        return mp3FrameHeader.isOriginal();
    }

    public boolean isPadding() {
        return mp3FrameHeader.isPadding();
    }

    public boolean isLossless() {
        return false;
    }

    /**
     * @return encoder
     */
    public String getEncoder() {
        return encoder;
    }

    /**
     * Set the size of the file, required in some calculations
     */
    private void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }


    /**
     * TODO (Was originally added for Wavs)
     */
    public int getByteRate() {
        return -1;
    }

    /**
     * TODO (Was origjnally added for Wavs)
     */
    public long getAudioDataLength() {
        return 0;
    }

    @Override
    public long getAudioDataStartPosition() {
        return audioDataStartPosition;
    }

    public void setAudioDataStartPosition(Long audioDataStartPosition) {
        this.audioDataStartPosition = audioDataStartPosition;
    }

    @Override
    public long getAudioDataEndPosition() {
        return audioDataEndPosition;
    }

    public void setAudioDataEndPosition(Long audioDataEndPosition) {
        this.audioDataEndPosition = audioDataEndPosition;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("mp3FrameHeader", mp3FrameHeader)
                .add("mp3XingFrame", mp3XingFrame)
                .add("mp3VbriFrame", mp3VbriFrame)
                .add("audioDataStartPosition", audioDataStartPosition)
                .add("audioDataEndPosition", audioDataEndPosition)
                .add("fileSize", fileSize)
                .add("startByte", startByte)
                .add("timePerFrame", timePerFrame)
                .add("trackLength", trackLength)
                .add("numberOfFrames", numberOfFrames)
                .add("numberOfFramesEstimate", numberOfFramesEstimate)
                .add("bitrate", bitrate)
                .add("encoder", encoder)
                .toString();
    }
}
