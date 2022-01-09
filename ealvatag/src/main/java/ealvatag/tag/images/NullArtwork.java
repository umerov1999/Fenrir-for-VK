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

package ealvatag.tag.images;

import android.graphics.Bitmap;

import com.google.common.base.Optional;

import java.io.File;
import java.io.IOException;

/**
 * A no-op implementation of {@link Artwork}. Works very well with {@link Optional<Artwork>} if caller is unconcerned with results
 * <p>
 * Created by Eric A. Snell on 1/21/17.
 */
public final class NullArtwork implements Artwork {
    public static final Artwork INSTANCE = new NullArtwork();

    private NullArtwork() {
    }

    @Override
    public byte[] getBinaryData() {
        return new byte[0];
    }

    @Override
    public Artwork setBinaryData(byte[] binaryData) {
        return this;
    }

    @Override
    public String getMimeType() {
        return "";
    }

    @Override
    public Artwork setMimeType(String mimeType) {
        return this;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Artwork setDescription(String description) {
        return this;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public Artwork setHeight(int height) {
        return this;
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public Artwork setWidth(int width) {
        return this;
    }

    @Override
    public boolean setImageFromData() {
        return false;
    }

    @Override
    public Bitmap getImage() throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isLinked() {
        return false;
    }

    @Override
    public Artwork setLinked(boolean linked) {
        return this;
    }

    @Override
    public String getImageUrl() {
        return "";
    }

    @Override
    public Artwork setImageUrl(String imageUrl) {
        return this;
    }

    @Override
    public int getPictureType() {
        return 0;
    }

    @Override
    public Artwork setPictureType(int pictureType) {
        return this;
    }

    @Override
    public Artwork setFromFile(File file) throws IOException {
        return this;
    }
}
