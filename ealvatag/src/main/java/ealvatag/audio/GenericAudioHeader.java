/*
 * Copyright (c) 2017 Eric A. Snell
 *
 * This file is part of eAlvaTag.
 *
 * eAlvaTag is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * eAlvaTag is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with eAlvaTag.  If not,
 * see <http://www.gnu.org/licenses/>.
 */
package ealvatag.audio;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import com.google.common.base.MoreObjects;

import java.util.concurrent.TimeUnit;

import ealvatag.utils.TimeUnits;

/**
 * This class represents a structure for storing and retrieving information
 * about the codec respectively the encoding parameters.<br>
 * Most of the parameters are available for nearly each audio format. Some
 * others would result in standard values.<br>
 * <b>Consider:</b> None of the setter methods will actually affect the audio
 * file. This is just a structure for retrieving information, not manipulating
 * the audio file.<br>
 *
 * @author Raphael Slinckx
 */
@SuppressWarnings("WeakerAccess")
public class GenericAudioHeader implements AudioHeader {
    private long audioDataLength = -1;
    private long audioDataStartPosition = 1;
    private long audioDataEndPosition = -1;
    private int bitRate = -1;
    private int noOfChannels = -1;
    private int samplingRate = -1;
    private int bitsPerSample = -1;
    private String encodingType = "";
    private boolean isVbr;
    private boolean isLossless;
    private double trackLength;
    private long noOfSamples = -1;
    private int byteRate = -1;

    public GenericAudioHeader() {
    }

    /**
     * This method returns the bitRate of the represented audio clip in
     * &quot;Kbps&quot;.<br>
     *
     * @return The bitRate in Kbps.
     */
    public int getBitRate() {
        return bitRate;
    }

    /**
     * This Method sets the bitRate in &quot;Kbps&quot;.<br>
     *
     * @param bitRate bitRate in kbps.
     */
    public void setBitRate(int bitRate) {
        this.bitRate = bitRate;
    }

    public String getEncodingType() {
        return encodingType;
    }

    /**
     * Sets the type of the encoding.<br>
     * This is a bit format specific.<br>
     * eg:Layer I/II/III
     *
     * @param encodingType Encoding type.
     */
    public void setEncodingType(String encodingType) {
        this.encodingType = encodingType;
    }

    /**
     * Returns the format, same as encoding type
     *
     * @return The encoding type
     */
    public String getFormat() {
        return encodingType;
    }

    @Override
    public int getChannelCount() {
        return noOfChannels;
    }

    @Override
    public long getDuration(TimeUnit timeUnit, boolean round) {
        return TimeUnits.convert(Math.round(trackLength * 1000000000), NANOSECONDS, timeUnit, round);
    }

    public double getDurationAsDouble() {
        return trackLength;
    }

    public int getSampleRate() {
        return samplingRate;
    }

    public int getBitsPerSample() {
        return bitsPerSample;
    }

    /*
     * Sets the Bits per Sample <br>
     *
     * @params bitsPerSample Bits Per Sample
     */
    public void setBitsPerSample(int bitsPerSample) {
        this.bitsPerSample = bitsPerSample;
    }

    /**
     * This method returns <code>true</code>, if the audio file is encoded
     * with &quot;Variable Bitrate&quot;.<br>
     *
     * @return <code>true</code> if audio clip is encoded with VBR.
     */
    public boolean isVariableBitRate() {
        return isVbr;
    }

    /**
     * Sets the VBR flag for the represented audio clip.<br>
     *
     * @param isVbr <code>true</code> if VBR.
     */
    public void setVariableBitRate(boolean isVbr) {
        this.isVbr = isVbr;
    }

    /**
     * This method returns <code>true</code>, if the audio file is encoded
     * with &quot;Lossless&quot;.<br>
     *
     * @return <code>true</code> if audio clip is encoded with VBR.
     */
    public boolean isLossless() {
        return isLossless;
    }

    /**
     * Sets the Lossless flag for the represented audio clip.<br>
     *
     * @param isLossless <code>true</code> if Lossless.
     */
    public void setLossless(boolean isLossless) {
        this.isLossless = isLossless;
    }

    /**
     * Sets the number of channels.
     *
     * @param channelMode number of channels (2 for stereo, 1 for mono).
     */
    public void setChannelNumber(int channelMode) {
        noOfChannels = channelMode;
    }

    /**
     * This method sets the audio duration of the represented clip.<br>
     *
     * @param length The duration of the audio in seconds (single-precision).
     */
    public void setPreciseLength(double length) {
        trackLength = length;
    }

    /**
     * Sets the Sampling rate in &quot;Hz&quot;<br>
     *
     * @param samplingRate Sample rate.
     */
    public void setSamplingRate(int samplingRate) {
        this.samplingRate = samplingRate;
    }

    public long getAudioDataLength() {
        return audioDataLength;
    }

    public void setAudioDataLength(long audioDataLength) {
        this.audioDataLength = audioDataLength;
    }

    public int getByteRate() {
        return byteRate;
    }

    /*
     * Sets the ByteRate (per second)
     *
     * @params ByteRate
     */
    public void setByteRate(int byteRate) {
        this.byteRate = byteRate;
    }

    public long getNoOfSamples() {
        return noOfSamples;
    }

    public void setNoOfSamples(Long noOfSamples) {
        this.noOfSamples = noOfSamples;
    }


    @Override
    public long getAudioDataStartPosition() {
        return audioDataStartPosition;
    }

    public void setAudioDataStartPosition(long audioDataStartPosition) {
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
    public final String toString() {
        return toStringHelper().toString();
    }

    protected MoreObjects.ToStringHelper toStringHelper() {
        return MoreObjects.toStringHelper(this)
                .add("audioDataLength", audioDataLength)
                .add("audioDataStartPosition", audioDataStartPosition)
                .add("audioDataEndPosition", audioDataEndPosition)
                .add("bitRate", bitRate)
                .add("noOfChannels", noOfChannels)
                .add("samplingRate", samplingRate)
                .add("bitsPerSample", bitsPerSample)
                .add("encodingType", encodingType)
                .add("isVbr", isVbr)
                .add("isLossless", isLossless)
                .add("trackLength", trackLength)
                .add("noOfSamples", noOfSamples)
                .add("byteRate", byteRate);
    }
}
