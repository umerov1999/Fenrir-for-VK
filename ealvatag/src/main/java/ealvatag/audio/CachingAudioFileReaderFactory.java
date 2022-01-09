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

/**
 * Creates the reader once and doles out the same one
 * <p>
 * Created by Eric A. Snell on 1/19/17.
 */
public abstract class CachingAudioFileReaderFactory implements AudioFileReaderFactory {
    private volatile AudioFileReader reader;

    @Override
    public final AudioFileReader make() {
        if (reader == null) {
            synchronized (this) {
                if (reader == null) {
                    reader = doMake();
                }
            }
        }
        return reader;
    }

    protected abstract AudioFileReader doMake();
}
