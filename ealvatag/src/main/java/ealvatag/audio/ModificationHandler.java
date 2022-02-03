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

import java.io.File;
import java.util.Vector;

import ealvatag.audio.exceptions.ModifyVetoException;

/**
 * This class multicasts the events to multiple listener instances.<br>
 * Additionally the Vetos are handled. (other listeners are notified).
 *
 * @author Christian Laireiter
 */
public class ModificationHandler implements AudioFileModificationListener {

    /**
     * The listeners to wich events are broadcasted are stored here.
     */
    private final Vector<AudioFileModificationListener> listeners = new Vector<>();

    /**
     * This method adds an {@link AudioFileModificationListener}
     *
     * @param l Listener to add.
     */
    public void addAudioFileModificationListener(AudioFileModificationListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    /**
     * (overridden)
     *
     * @see AudioFileModificationListener#fileModified(ealvatag.audio.AudioFile,
     * File)
     */
    public void fileModified(AudioFile original, File temporary) throws ModifyVetoException {
        for (AudioFileModificationListener listener : listeners) {
            AudioFileModificationListener current = listener;
            try {
                current.fileModified(original, temporary);
            } catch (ModifyVetoException e) {
                vetoThrown(current, original, e);
                throw e;
            }
        }
    }

    /**
     * (overridden)
     *
     * @see AudioFileModificationListener#fileOperationFinished(File)
     */
    public void fileOperationFinished(File result) {
        for (AudioFileModificationListener listener : listeners) {
            AudioFileModificationListener current = listener;
            current.fileOperationFinished(result);
        }
    }

    /**
     * (overridden)
     *
     * @see AudioFileModificationListener#fileWillBeModified(ealvatag.audio.AudioFile,
     * boolean)
     */
    public void fileWillBeModified(AudioFile file, boolean delete) throws ModifyVetoException {
        for (AudioFileModificationListener listener : listeners) {
            AudioFileModificationListener current = listener;
            try {
                current.fileWillBeModified(file, delete);
            } catch (ModifyVetoException e) {
                vetoThrown(current, file, e);
                throw e;
            }
        }
    }

    /**
     * This method removes an {@link AudioFileModificationListener}
     *
     * @param l Listener to remove.
     */
    public void removeAudioFileModificationListener(AudioFileModificationListener l) {
        listeners.remove(l);
    }

    /**
     * (overridden)
     *
     * @see AudioFileModificationListener#vetoThrown(AudioFileModificationListener,
     * ealvatag.audio.AudioFile,
     * ealvatag.audio.exceptions.ModifyVetoException)
     */
    public void vetoThrown(AudioFileModificationListener cause, AudioFile original, ModifyVetoException veto) {
        for (AudioFileModificationListener listener : listeners) {
            AudioFileModificationListener current = listener;
            current.vetoThrown(cause, original, veto);
        }
    }
}
