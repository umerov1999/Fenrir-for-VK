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

import java.util.concurrent.TimeUnit;

/**
 * No-op implementation of {@link AudioHeader}
 * <p>
 * Created by Eric A. Snell on 2/2/17.
 */
public final class NullAudioHeader implements AudioHeader {
    public static final AudioHeader INSTANCE = new NullAudioHeader();

    private NullAudioHeader() {
    }

    @Override
    public String getEncodingType() {
        return "";
    }

    @Override
    public int getByteRate() {
        return Integer.MIN_VALUE;
    }

    @Override
    public int getBitRate() {
        return 0;
    }

    @Override
    public long getAudioDataLength() {
        return Long.MIN_VALUE;
    }

    @Override
    public long getAudioDataStartPosition() {
        return Long.MIN_VALUE;
    }

    @Override
    public long getAudioDataEndPosition() {
        return Long.MIN_VALUE;
    }

    @Override
    public int getSampleRate() {
        return 0;
    }

    @Override
    public String getFormat() {
        return "";
    }

    @Override
    public int getChannelCount() {
        return 0;
    }

    @Override
    public boolean isVariableBitRate() {
        return false;
    }

    @Override
    public long getDuration(TimeUnit timeUnit, boolean round) {
        return 0;
    }

    @Override
    public double getDurationAsDouble() {
        return 0;
    }

    @Override
    public int getBitsPerSample() {
        return 0;
    }

    @Override
    public boolean isLossless() {
        return false;
    }

    @Override
    public long getNoOfSamples() {
        return Long.MIN_VALUE;
    }
}
