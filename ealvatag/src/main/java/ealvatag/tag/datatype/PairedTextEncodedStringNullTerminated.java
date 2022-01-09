package ealvatag.tag.datatype;

import static ealvatag.logging.EalvaTagLog.LogLevel.DEBUG;
import static ealvatag.logging.EalvaTagLog.LogLevel.ERROR;
import static ealvatag.logging.EalvaTagLog.LogLevel.WARN;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ealvatag.tag.InvalidDataTypeException;
import ealvatag.tag.id3.AbstractTagFrameBody;
import ealvatag.utils.EqualsUtil;
import okio.Buffer;

/**
 * Represents a data type that allow multiple Strings but they should be paired as key values, i.e should be 2,4,6..
 * But keys are not unique so we don't store as a map, so could have same key pointing to two different values
 * such as two ENGINEER keys
 */
public class PairedTextEncodedStringNullTerminated extends AbstractDataType {
    public PairedTextEncodedStringNullTerminated(String identifier, AbstractTagFrameBody frameBody) {
        super(identifier, frameBody);
        value = new PairedTextEncodedStringNullTerminated.ValuePairs();
    }

    @SuppressWarnings("unused")
    public PairedTextEncodedStringNullTerminated(TextEncodedStringSizeTerminated object) {
        super(object);
        value = new PairedTextEncodedStringNullTerminated.ValuePairs();
    }

    @SuppressWarnings("unused")
    public PairedTextEncodedStringNullTerminated(PairedTextEncodedStringNullTerminated object) {
        super(object);
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof PairedTextEncodedStringNullTerminated)) {
            return false;
        }

        PairedTextEncodedStringNullTerminated that = (PairedTextEncodedStringNullTerminated) obj;

        return EqualsUtil.areEqual(value, that.value);
    }

    /**
     * Returns the size in bytes of this dataType when written to file
     *
     * @return size of this dataType
     */
    public int getSize() {
        return size;
    }

    /**
     * Check the value can be encoded with the specified encoding
     */
    public boolean canBeEncoded() {
        if (null != value) {
            for (Pair entry : ((ValuePairs) value).mapping) {
                TextEncodedStringNullTerminated next =
                        new TextEncodedStringNullTerminated(identifier, frameBody, entry.getValue());
                if (!next.canBeEncoded()) {
                    return false;
                }
            }
            return true;
        } else {
            LOG.log(ERROR, "value is null");
            return false;
        }
    }

    /**
     * Read Null Terminated Strings from the array starting at offset, continue until unable to find any null terminated
     * Strings or until reached the end of the array. The offset should be set to byte after the last null terminated
     * String found.
     *
     * @param arr    to read the Strings from
     * @param offset in the array to start reading from
     * @throws InvalidDataTypeException if unable to find any null terminated Strings
     */
    public void readByteArray(byte[] arr, int offset) throws InvalidDataTypeException {
        LOG.log(DEBUG, "Reading PairTextEncodedStringNullTerminated from array from offset:%s", offset);
        //Continue until unable to read a null terminated String
        while (true) {
            try {
                //Read Key
                TextEncodedStringNullTerminated key = new TextEncodedStringNullTerminated(identifier, frameBody);
                key.readByteArray(arr, offset);
                size += key.getSize();
                offset += key.getSize();
                if (key.getSize() == 0) {
                    break;
                }

                try {
                    //Read Value
                    TextEncodedStringNullTerminated result = new TextEncodedStringNullTerminated(identifier, frameBody);
                    result.readByteArray(arr, offset);
                    size += result.getSize();
                    offset += result.getSize();
                    if (result.getSize() == 0) {
                        break;
                    }
                    //Add to value
                    ((ValuePairs) value).add((String) key.getValue(), (String) result.getValue());
                } catch (InvalidDataTypeException idte) {
                    //Value may not be null terminated if it is the last value
                    //Read Value
                    if (offset >= arr.length) {
                        break;
                    }
                    TextEncodedStringSizeTerminated result = new TextEncodedStringSizeTerminated(identifier, frameBody);
                    result.readByteArray(arr, offset);
                    size += result.getSize();
                    offset += result.getSize();
                    if (result.getSize() == 0) {
                        break;
                    }
                    //Add to value
                    ((ValuePairs) value).add((String) key.getValue(), (String) result.getValue());
                    break;
                }
            } catch (InvalidDataTypeException idte) {
                break;
            }

            if (size == 0) {
                LOG.log(WARN, "No null terminated Strings found");
                throw new InvalidDataTypeException("No null terminated Strings found");
            }
        }
        LOG.log(DEBUG, "Read  PairTextEncodedStringNullTerminated:%s size:%s", value, size);
    }

    @Override
    public void read(Buffer buffer, int size) throws EOFException, InvalidDataTypeException {
        int runningSize = size;
        while (runningSize > 0) {  // loop until no more null terminated strings
            TextEncodedStringNullTerminated key = new TextEncodedStringNullTerminated(identifier, frameBody);
            key.read(buffer, runningSize);
            int keySize = key.getSize();
            if (keySize == 0) {
                break;
            }
            this.size += keySize;
            runningSize -= keySize;
            try {
                // TODO: 1/25/17 do we really need to fall back to non null terminated?? Means we have to clone the buffer.
                TextEncodedStringNullTerminated result = new TextEncodedStringNullTerminated(identifier, frameBody);
                result.read(buffer.clone(), size);  // clone so we can try again if InvalidDataTypeException case: read to end instead of null
                int resultSize = result.getSize();
                buffer.skip(resultSize);  // we cloned, so skip the amount read from the clone.
                this.size += resultSize;
                runningSize -= resultSize;
                if (resultSize == 0) {
                    break;
                }
                //Add to value
                ((ValuePairs) value).add((String) key.getValue(), (String) result.getValue());
            } catch (InvalidDataTypeException e) {
                TextEncodedStringSizeTerminated result = new TextEncodedStringSizeTerminated(identifier, frameBody);
                result.read(buffer, size);
                int resultSize = result.getSize();
                this.size += resultSize;
                runningSize -= resultSize;
                if (resultSize == 0) {
                    break;
                }
                //Add to value
                ((ValuePairs) value).add((String) key.getValue(), (String) result.getValue());
                break;
            }

            if (this.size == 0) {
                LOG.log(WARN, "No null terminated Strings found");
                throw new InvalidDataTypeException("No null terminated Strings found");
            }
        }
        LOG.log(DEBUG, "Read  PairTextEncodedStringNullTerminated:%s size:%s", value, size);
    }

    /**
     * For every String write to byteBuffer
     *
     * @return byteBuffer that should be written to file to persist this dataType.
     */
    public byte[] writeByteArray() {
        LOG.log(DEBUG, "Writing PairTextEncodedStringNullTerminated");

        int localSize = 0;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            for (Pair pair : ((ValuePairs) value).mapping) {
                {
                    TextEncodedStringNullTerminated next =
                            new TextEncodedStringNullTerminated(identifier, frameBody, pair.getKey());
                    buffer.write(next.writeByteArray());
                    localSize += next.getSize();
                }
                {
                    TextEncodedStringNullTerminated next =
                            new TextEncodedStringNullTerminated(identifier, frameBody, pair.getValue());
                    buffer.write(next.writeByteArray());
                    localSize += next.getSize();
                }
            }
        } catch (IOException ioe) {
            //This should never happen because the write is internal with the JVM it is not to a file
            LOG.log(ERROR, "IOException in MultipleTextEncodedStringNullTerminated when writing byte array", ioe);
            throw new RuntimeException(ioe);
        }

        //Update size member variable
        size = localSize;

        LOG.log(DEBUG, "Written PairTextEncodedStringNullTerminated");
        return buffer.toByteArray();
    }

    public String toString() {
        return value.toString();
    }

    public ValuePairs getValue() {
        return (ValuePairs) value;
    }

    /**
     * This holds the values held by this PairedTextEncodedDataType, always held as pairs of values
     */
    public static class ValuePairs {
        private final List<Pair> mapping = new ArrayList<>();

        public ValuePairs() {
        }

        public void add(Pair pair) {
            mapping.add(pair);
        }

        /**
         * Add String Data type to the value list
         *
         * @param value to add to the list
         */
        public void add(String key, String value) {
            mapping.add(new Pair(key, value));
        }


        /**
         * Return the list of values
         *
         * @return the list of values
         */
        public List<Pair> getMapping() {
            return mapping;
        }

        /**
         * @return no of values
         */
        public int getNumberOfValues() {
            return mapping.size();
        }

        /**
         * Return the list of values as a single string separated by a colon,comma
         *
         * @return a string representation of the value
         */
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Pair next : mapping) {
                sb.append(next.getKey())
                        .append(':')
                        .append(next.getValue())
                        .append(',');
            }
            if (sb.length() > 0) {
                sb.setLength(sb.length() - 1);
            }
            return sb.toString();
        }

        /**
         * @return no of values
         */
        public int getNumberOfPairs() {
            return mapping.size();
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }

            if (!(obj instanceof ValuePairs)) {
                return false;
            }

            ValuePairs that = (ValuePairs) obj;

            return EqualsUtil.areEqual(getNumberOfValues(), that.getNumberOfValues());
        }
    }
}
