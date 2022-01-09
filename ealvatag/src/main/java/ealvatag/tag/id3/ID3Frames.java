/*
 *  MusicTag Copyright (C)2003,2004
 *
 *  This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser
 *  General Public  License as published by the Free Software Foundation; either version 2.1 of the License,
 *  or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 *  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License along with this library; if not,
 *  you can get a copy from http://www.opensource.org/licenses/lgpl-license.php or write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package ealvatag.tag.id3;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;

/**
 * Subclasses Defines ID3 frames for their Tag Version
 * <p>
 * Here we specify how frames are mapped between different Tag Versions
 *
 * @author Paul Taylor
 * @version $Id$
 */
public abstract class ID3Frames {
    private volatile ImmutableSet<String> multipleFrames;
    private volatile ImmutableSet<String> discardIfFileAlteredFrames;
    private volatile ImmutableSet<String> supportedFrames;
    private volatile ImmutableSet<String> binaryFrames;
    private volatile ImmutableSet<String> commonFrames;
    private volatile ImmutableSet<String> extensionFrames;

    /**
     * If file changes discard these frames
     *
     * @param frameID frame id to check
     * @return true if can discard
     */
    public boolean isDiscardIfFileAltered(String frameID) {
        return getDiscardIfFileAlteredFrames().contains(frameID);
    }

    private ImmutableSet<String> getDiscardIfFileAlteredFrames() {
        if (discardIfFileAlteredFrames == null) {
            synchronized (this) {
                if (discardIfFileAlteredFrames == null) {
                    discardIfFileAlteredFrames = makeDiscardIfFileAlteredFrames();
                }
            }
        }
        return discardIfFileAlteredFrames;
    }

    protected abstract ImmutableSet<String> makeDiscardIfFileAlteredFrames();

    /**
     * Are multiple occurrences of frame allowed
     *
     * @param frameID frame id to check
     * @return true if multiple allowed
     */
    public boolean isMultipleAllowed(String frameID) {
        return getMultipleFrames().contains(frameID);
    }

    private ImmutableSet<String> getMultipleFrames() {
        if (multipleFrames == null) {
            synchronized (this) {
                if (multipleFrames == null) {
                    multipleFrames = makeMultipleFrames();
                }
            }
        }
        return multipleFrames;
    }

    protected abstract ImmutableSet<String> makeMultipleFrames();

    /**
     * @param frameID is this frame id supported
     * @return true if frames with this id are part of the specification
     */
    public boolean isSupportedFrames(String frameID) {
        return getSupportedFrames().contains(frameID);
    }

    @VisibleForTesting
    public final ImmutableSet<String> getSupportedFrames() {
        if (supportedFrames == null) {
            synchronized (this) {
                if (supportedFrames == null) {
                    supportedFrames = makeSupportedFrames();
                }
            }
        }
        return supportedFrames;
    }

    protected abstract ImmutableSet<String> makeSupportedFrames();

    /**
     * @param frameID frame id to check
     * @return true if frames with this id are considered common
     */
    public boolean isCommon(String frameID) {
        return getCommonFrames().contains(frameID);
    }

    private ImmutableSet<String> getCommonFrames() {
        if (commonFrames == null) {
            synchronized (this) {
                if (commonFrames == null) {
                    commonFrames = makeCommonFrames();
                }
            }
        }
        return commonFrames;
    }

    protected abstract ImmutableSet<String> makeCommonFrames();

    /**
     * @param frameID frame id to check
     * @return true if frames with this id are binary (non textual data)
     */
    public boolean isBinary(String frameID) {
        return getBinaryFrames().contains(frameID);
    }

    private ImmutableSet<String> getBinaryFrames() {
        if (binaryFrames == null) {
            synchronized (this) {
                if (binaryFrames == null) {
                    binaryFrames = makeBinaryFrames();
                }
            }
        }
        return binaryFrames;
    }

    protected abstract ImmutableSet<String> makeBinaryFrames();

    /**
     * @param frameID frame id to check
     * @return true if frame is a known extension
     */
    public boolean isExtensionFrames(String frameID) {
        return getExtensionFrames().contains(frameID);
    }

    private ImmutableSet<String> getExtensionFrames() {
        if (extensionFrames == null) {
            synchronized (this) {
                if (extensionFrames == null) {
                    extensionFrames = makeExtensionFrames();
                }
            }
        }
        return extensionFrames;
    }

    protected abstract ImmutableSet<String> makeExtensionFrames();

}
