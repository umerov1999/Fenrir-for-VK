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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import ealvatag.tag.id3.valuepair.ImageFormats;
import ealvatag.tag.reference.PictureTypes;

/**
 * Base class for Artwork implementations
 * <p>
 * Created by Eric A. Snell on 1/20/17.
 */
public abstract class AbstractArtwork implements Artwork {
    private byte[] binaryData;
    private String mimeType = "";
    private String description = "";
    private boolean isLinked;
    private String imageUrl = "";
    private int pictureType = -1;
    private int width;
    private int height;

    public Artwork setFromFile(File file) throws IOException {
        RandomAccessFile imageFile = new RandomAccessFile(file, "r");
        byte[] imagedata = new byte[(int) imageFile.length()];
        imageFile.read(imagedata);
        imageFile.close();

        setBinaryData(imagedata);
        setMimeType(ImageFormats.getMimeTypeForBinarySignature(imagedata));
        setDescription("");
        setPictureType(PictureTypes.DEFAULT_ID);
        return this;
    }

    public byte[] getBinaryData() {
        return binaryData;
    }

    public Artwork setBinaryData(byte[] binaryData) {
        this.binaryData = binaryData;
        return this;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Artwork setMimeType(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public Artwork setDescription(String description) {
        this.description = description;
        return this;
    }

    public boolean isLinked() {
        return isLinked;
    }

    public Artwork setLinked(boolean linked) {
        isLinked = linked;
        return this;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Artwork setImageUrl(String imageUrl) {
        return setLinkedFromURL(imageUrl);
    }

    public int getPictureType() {
        return pictureType;
    }

    public Artwork setPictureType(int pictureType) {
        this.pictureType = pictureType;
        return this;
    }

    public Artwork setWidth(int width) {
        this.width = width;
        return this;
    }

    public Artwork setHeight(int height) {
        this.height = height;
        return this;
    }

    private Artwork setLinkedFromURL(String url) {
        isLinked = true;
        imageUrl = url;
        return this;
    }
}
