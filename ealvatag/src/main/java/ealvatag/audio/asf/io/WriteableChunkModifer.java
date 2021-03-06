package ealvatag.audio.asf.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ealvatag.audio.asf.data.GUID;
import ealvatag.audio.asf.util.Utils;

/**
 * A chunk modifier which works with information provided by
 * {@link WriteableChunk} objects.<br>
 *
 * @author Christian Laireiter
 */
public class WriteableChunkModifer implements ChunkModifier {

    /**
     * The chunk to write.
     */
    private final WriteableChunk writableChunk;

    /**
     * Creates an instance.<br>
     *
     * @param chunk chunk to write
     */
    public WriteableChunkModifer(WriteableChunk chunk) {
        writableChunk = chunk;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isApplicable(GUID guid) {
        return guid.equals(writableChunk.getGuid());
    }

    /**
     * {@inheritDoc}
     */
    public ModificationResult modify(GUID guid, InputStream chunk, OutputStream destination) throws IOException { // NOPMD by Christian Laireiter on 5/9/09 5:03 PM
        int chunkDiff = 0;
        long newSize = 0;
        long oldSize = 0;
        /*
         * Replace the outputstream with the counting one, only if assert's are
         * evaluated.
         */
        assert (destination = new CountingOutputstream(destination)) != null;
        if (!writableChunk.isEmpty()) {
            newSize = writableChunk.writeInto(destination);
            assert newSize == writableChunk.getCurrentAsfChunkSize();
            /*
             * If assert's are evaluated, we have replaced destination by a
             * CountingOutpustream and can now verify if
             * getCurrentAsfChunkSize() really works correctly.
             */
            assert ((CountingOutputstream) destination).getCount() == newSize;
            if (guid == null) {
                chunkDiff++;
            }

        }
        if (guid != null) {
            assert isApplicable(guid);
            if (writableChunk.isEmpty()) {
                chunkDiff--;
            }
            oldSize = Utils.readUINT64(chunk);
            chunk.skip(oldSize - 24);
        }
        return new ModificationResult(chunkDiff, (newSize - oldSize), guid);
    }

}
