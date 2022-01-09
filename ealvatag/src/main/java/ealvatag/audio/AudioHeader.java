package ealvatag.audio;

import java.util.concurrent.TimeUnit;

/**
 * Representation of AudioHeader
 * <p>
 * <p>Contains info about the Audio Header
 */
public interface AudioHeader {
    /**
     * @return the audio file type
     */
    String getEncodingType();

    /**
     * @return the ByteRate of the Audio, this is the total average amount of bytes of data sampled per second
     */
    int getByteRate();


    /**
     * @return bitRate as a number, this is the amount of kilobits of data sampled per second
     */
    int getBitRate();


    /**
     * @return length of the audio data in bytes, exactly what this means depends on the audio format
     * <p>
     * TODO currently only used by Wav/Aiff/Flac/Mp4
     */
    long getAudioDataLength();


    /**
     * @return the location in the file where the audio samples start
     * <p>
     * TODO currently only used by Wav/Aiff/Flac/Mp4
     */
    long getAudioDataStartPosition();


    /**
     * @return the location in the file where the audio samples end
     * <p>
     * TODO currently only used by Wav/Aiff/Flac/Mp4
     */
    long getAudioDataEndPosition();


    /**
     * @return he Sampling rate, the number of samples taken per second
     */
    int getSampleRate();

    /**
     * @return the format
     */
    String getFormat();

    /**
     * Number of channels in the track, ie. mono=1 stereo=2
     *
     * @return number of channels
     */
    int getChannelCount();

    /**
     * @return if the sampling bitRate is variable or constant
     */
    boolean isVariableBitRate();

    /**
     * Return the duration of the audio track in the given {@code timeUnit}, either rounded or truncated.
     * <p>
     * If actual duration precision is more fine-grained than nanoseconds, the least significant nanosecond digit  will be rounded. eg.
     * if nanoseconds is equivalent to 328979581.5 the result will always be the be rounded to 328979582 regardless of the {@code round}
     * parameter.
     *
     * @param timeUnit the unit to return
     * @param round    if true conversion rounded up at the TimeUnit boundary, otherwise truncated (which is the usual
     *                 {@link TimeUnit#convert(long, TimeUnit)} result
     * @return duration in {@code timeUnit}
     */
    long getDuration(TimeUnit timeUnit, boolean round);

    /**
     * Return the track duration in seconds. The {@link #getDuration(TimeUnit, boolean)} method should be preferred as it supports
     * nanoseconds (results are format dependent). This will return seconds in the form of a double, but any precision at or below
     * nanoseconds is generally not needed and probably not precise. Also, comparing doubles is problematic.
     *
     * @return track length as double
     * @see #getDuration(TimeUnit, boolean)
     */
    double getDurationAsDouble();

    /**
     * @return the number of bits in each sample
     */
    int getBitsPerSample();

    /**
     * @return if the audio codec is lossless or lossy
     */
    boolean isLossless();

    /**
     * @return the total number of samples, this can usually be used in conjunction with the sample rate to determine the track duration
     */
    long getNoOfSamples();
}
