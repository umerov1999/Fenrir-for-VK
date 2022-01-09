package ealvatag.audio.real;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import ealvatag.audio.AudioFileReader;
import ealvatag.audio.GenericAudioHeader;
import ealvatag.audio.Utils;
import ealvatag.audio.exceptions.CannotReadException;
import ealvatag.tag.FieldDataInvalidException;
import ealvatag.tag.FieldKey;
import ealvatag.tag.TagFieldContainer;

/**
 * Real Media File Format: Major Chunks: .RMF PROP MDPR CONT DATA INDX
 */
public class RealFileReader extends AudioFileReader {
    @Override
    protected GenericAudioHeader getEncodingInfo(RandomAccessFile raf) throws CannotReadException, IOException {
        GenericAudioHeader rv = new GenericAudioHeader();
        RealChunk prop = findPropChunk(raf);
        DataInputStream dis = prop.getDataInputStream();
        int objVersion = Utils.readUint16(dis);
        if (objVersion == 0) {
            long maxBitRate = Utils.readUint32(dis) / 1000;
            long avgBitRate = Utils.readUint32(dis) / 1000;
            long maxPacketSize = Utils.readUint32(dis);
            long avgPacketSize = Utils.readUint32(dis);
            long packetCnt = Utils.readUint32(dis);
            int duration = (int) Utils.readUint32(dis) / 1000;
            long preroll = Utils.readUint32(dis);
            long indexOffset = Utils.readUint32(dis);
            long dataOffset = Utils.readUint32(dis);
            int numStreams = Utils.readUint16(dis);
            int flags = Utils.readUint16(dis);
            rv.setBitRate((int) avgBitRate);
            rv.setPreciseLength(duration);
            rv.setVariableBitRate(maxBitRate != avgBitRate);
        }
        return rv;
    }

    private RealChunk findPropChunk(RandomAccessFile raf) throws IOException, CannotReadException {
        RealChunk rmf = RealChunk.readChunk(raf);
        RealChunk prop = RealChunk.readChunk(raf);
        return prop;
    }

    private RealChunk findContChunk(RandomAccessFile raf) throws IOException, CannotReadException {
        RealChunk rmf = RealChunk.readChunk(raf);
        RealChunk prop = RealChunk.readChunk(raf);
        RealChunk rv = RealChunk.readChunk(raf);
        while (!rv.isCONT()) rv = RealChunk.readChunk(raf);
        return rv;
    }

    @Override
    protected TagFieldContainer getTag(RandomAccessFile raf, boolean ignoreArtwork) throws CannotReadException, IOException {
        RealChunk cont = findContChunk(raf);
        DataInputStream dis = cont.getDataInputStream();
        String title = Utils.readString(dis, Utils.readUint16(dis));
        String author = Utils.readString(dis, Utils.readUint16(dis));
        String copyright = Utils.readString(dis, Utils.readUint16(dis));
        String comment = Utils.readString(dis, Utils.readUint16(dis));
        RealTag rv = new RealTag();
        // NOTE: frequently these fields are off-by-one, thus the crazy
        // logic below...
        try {
            rv.addField(FieldKey.TITLE, (title.length() == 0 ? author : title));
            rv.addField(FieldKey.ARTIST, title.length() == 0 ? copyright : author);
            rv.addField(FieldKey.COMMENT, comment);
        } catch (FieldDataInvalidException fdie) {
            throw new RuntimeException(fdie);
        }
        return rv;
    }

}

