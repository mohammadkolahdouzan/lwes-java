/*======================================================================*
 * Licensed under the New BSD License (the "License"); you may not use  *
 * this file except in compliance with the License.  Unless required    *
 * by applicable law or agreed to in writing, software distributed      *
 * under the License is distributed on an "AS IS" BASIS, WITHOUT        *
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.     *
 * See the License for the specific language governing permissions and  *
 * limitations under the License. See accompanying LICENSE file.        *
 *======================================================================*/
package org.lwes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.lwes.util.EncodedString;
import org.lwes.util.IPAddress;

public abstract class DefaultEvent implements Event {
    private static final BigInteger UINT64_MASK = new BigInteger("ffffffffffffffff", 16);

    // used to cache length of the encoded event names and keys
    static protected ConcurrentHashMap<String, Integer> encodedStringLengthCache =
        new ConcurrentHashMap<String, Integer>();


    public void setInt16Array(String attributeName, short[] value) throws EventSystemException {
        set(attributeName, FieldType.INT16_ARRAY, value);
    }

    public void setInt32Array(String attributeName, int[] value) throws EventSystemException {
        set(attributeName, FieldType.INT32_ARRAY, value);
    }

    public void setInt64Array(String attributeName, long[] value) throws EventSystemException {
        set(attributeName, FieldType.INT64_ARRAY, value);
    }

    public void setUInt16Array(String attributeName, int[] value) throws EventSystemException {
        set(attributeName, FieldType.UINT16_ARRAY, value);
    }

    public void setUInt32Array(String attributeName, long[] value) throws EventSystemException {
        set(attributeName, FieldType.UINT32_ARRAY, value);
    }

    public void setUInt64Array(String attributeName, long[] value) throws EventSystemException {
        final BigInteger[] value2 = new BigInteger[value.length];
        for (int i = 0; i < value.length; ++i) {
            value2[i] = BigInteger.valueOf(value[i]).and(UINT64_MASK);
        }
        set(attributeName, FieldType.UINT64_ARRAY, value2);
    }

    public void setUInt64Array(String attributeName, BigInteger[] value) throws EventSystemException {
        set(attributeName, FieldType.UINT64_ARRAY, value);
    }

    public void setStringArray(String attributeName, String[] value) throws EventSystemException {
        set(attributeName, FieldType.STRING_ARRAY, value);
    }

    public void setStringObjArray(String attributeName, String[] value) throws EventSystemException {
        set(attributeName, FieldType.NSTRING_ARRAY, value);
    }

    public void setIPAddressArray(String attributeName, IPAddress[] value)
            throws EventSystemException {
        set(attributeName, FieldType.IP_ADDR_ARRAY, value);
    }

    public void setBooleanArray(String attributeName, boolean[] value) throws EventSystemException {
        set(attributeName, FieldType.BOOLEAN_ARRAY, value);
    }

    public void setByteArray(String attributeName, byte[] value) throws EventSystemException {
        set(attributeName, FieldType.BYTE_ARRAY, value);
    }

    public void setDoubleArray(String attributeName, double[] value) throws EventSystemException {
        set(attributeName, FieldType.DOUBLE_ARRAY, value);
    }

    public void setFloatArray(String attributeName, float[] value) throws EventSystemException {
        set(attributeName, FieldType.FLOAT_ARRAY, value);
    }

    public void setShortArray(String attributeName, Short[] value) throws EventSystemException {
        set(attributeName, FieldType.NUINT16_ARRAY, value);
    }

    public void setIntegerArray(String attributeName, Integer[] value) throws EventSystemException {
        set(attributeName, FieldType.NUINT32_ARRAY, value);
    }

    public void setLongArray(String attributeName, Long[] value) throws EventSystemException {
        set(attributeName, FieldType.NINT64_ARRAY, value);
    }

    public void setBooleanArray(String attributeName, Boolean[] value) throws EventSystemException {
        set(attributeName, FieldType.NBOOLEAN_ARRAY, value);
    }

    public void setByteArray(String attributeName, Byte[] value) throws EventSystemException {
        set(attributeName, FieldType.NBYTE_ARRAY, value);
    }

    public void setDoubleArray(String attributeName, Double[] value) throws EventSystemException {
        set(attributeName, FieldType.NDOUBLE_ARRAY, value);
    }

    public void setFloatArray(String attributeName, Float[] value) throws EventSystemException {
        set(attributeName, FieldType.NFLOAT_ARRAY, value);
    }

    public void setDouble(String attributeName, double value) throws EventSystemException {
        set(attributeName, FieldType.DOUBLE, value);
    }

    public void setFloat(String attributeName, float value) throws EventSystemException {
        set(attributeName, FieldType.FLOAT, value);
    }

    public void setByte(String attributeName, byte value) throws EventSystemException {
        set(attributeName, FieldType.BYTE, value);
    }

    public void setBoolean(String attributeName, boolean aBool) throws EventSystemException {
        set(attributeName, FieldType.BOOLEAN, aBool);
    }

    public void setUInt16(String attributeName, int aNumber) throws EventSystemException {
        set(attributeName, FieldType.UINT16, aNumber);
    }

    public void setInt16(String attributeName, short aNumber) throws EventSystemException {
        set(attributeName, FieldType.INT16, aNumber);
    }

    public void setUInt32(String attributeName, long aNumber) throws EventSystemException {
        set(attributeName, FieldType.UINT32, aNumber);
    }

    public void setInt32(String attributeName, Integer aNumber) throws EventSystemException {
        set(attributeName, FieldType.INT32, aNumber);
    }

    public void setInt32(String attributeName, int aNumber) throws EventSystemException {
        set(attributeName, FieldType.INT32, aNumber);
    }

    public void setUInt64(String attributeName, BigInteger aNumber) throws EventSystemException {
        set(attributeName, FieldType.UINT64, aNumber);
    }

    public void setUInt64(String attributeName, long aNumber) throws EventSystemException {
        set(attributeName, FieldType.UINT64, BigInteger.valueOf(aNumber));
    }

    public void setInt64(String attributeName, long aNumber) throws EventSystemException {
        set(attributeName, FieldType.INT64, aNumber);
    }

    public void setString(String attributeName, String aString) throws EventSystemException {
        set(attributeName, FieldType.STRING, aString);
    }

    public void setIPAddress(String attributeName, byte[] address) throws EventSystemException {
        setIPAddress(attributeName, new IPAddress(address));
    }

    public void setIPAddress(String attributeName, InetAddress address) throws EventSystemException {
        setIPAddress(attributeName, new IPAddress(address));
    }

    public void setIPAddress(String attributeName, IPAddress address) throws EventSystemException {
        set(attributeName, FieldType.IPADDR, address);
    }

    public void setNShortArray(String attribute, Short[] values) {
        set(attribute, FieldType.NUINT16_ARRAY, values);
    }

    public void setNLongArray(String attribute, Long[] values) {
        set(attribute, FieldType.NINT64_ARRAY, values);
    }

    public void setNIntegerArray(String attribute, Integer[] values) {
        set(attribute, FieldType.NUINT32_ARRAY, values);
    }

    public void setNFloatArray(String attribute, Float[] values) {
        set(attribute, FieldType.NFLOAT_ARRAY, values);
    }

    public void setNDoubleArray(String attribute, Double[] values) {
        set(attribute, FieldType.NDOUBLE_ARRAY, values);
    }

    public boolean isSet(String attributeName) {
        return (get(attributeName) != null);
    }

    public Short[] getShortObjArray(String attributeName) {
        return (Short[]) get(attributeName);
    }

    public Integer[] getIntegerObjArray(String attributeName) {
        return (Integer[]) get(attributeName);
    }

    public Long[] getLongObjArray(String attributeName) {
        return (Long[]) get(attributeName);
    }

    public Float[] getFloatObjArray(String attributeName) {
        return (Float[]) get(attributeName);
    }

    public Double[] getDoubleObjArray(String attributeName) {
        return (Double[]) get(attributeName);
    }

    public Byte[] getByteObjArray(String attributeName) {
        return (Byte[]) get(attributeName);
    }

    public Boolean[] getBooleanObjArray(String attributeName) {
        return (Boolean[]) get(attributeName);
    }

    public String[] getStringObjArray(String attributeName) {
        return (String[]) get(attributeName);
    }

    public BigInteger[] getBigIntegerObjArray(String attributeName) {
        return (BigInteger[]) get(attributeName);
    }

    public short[] getInt16Array(String attributeName) {
        return (short[]) get(attributeName);
    }

    public int[] getInt32Array(String attributeName) {
        return (int[]) get(attributeName);
    }

    public long[] getInt64Array(String attributeName) {
        return (long[]) get(attributeName);
    }

    public int[] getUInt16Array(String attributeName) {
        return (int[]) get(attributeName);
    }

    public long[] getUInt32Array(String attributeName) {
        return (long[]) get(attributeName);
    }

    public BigInteger[] getUInt64Array(String attributeName) {
        return (BigInteger[]) get(attributeName);
    }

    public String[] getStringArray(String attributeName) {
        return (String[]) get(attributeName);
    }

    public byte[] getByteArray(String attributeName) {
        return (byte[]) get(attributeName);
    }

    public boolean[] getBooleanArray(String attributeName) {
        return (boolean[]) get(attributeName);
    }

    public double[] getDoubleArray(String attributeName) {
        return (double[]) get(attributeName);
    }

    public float[] getFloatArray(String attributeName) {
        return (float[]) get(attributeName);
    }

    public Double getDouble(String attributeName) {
        return (Double) get(attributeName);
    }

    public Float getFloat(String attributeName) {
        return (Float) get(attributeName);
    }

    public Byte getByte(String attributeName) {
        return (Byte) get(attributeName);
    }

    public Boolean getBoolean(String attributeName) {
        return (Boolean) get(attributeName);
    }

    public Integer getUInt16(String attributeName) {
        return (Integer) get(attributeName);
    }

    public Short getInt16(String attributeName) {
        return (Short) get(attributeName);
    }

    public Long getUInt32(String attributeName) {
        return (Long) get(attributeName);
    }

    public Integer getInt32(String attributeName) {
        return (Integer) get(attributeName);
    }

    public BigInteger getUInt64(String attributeName) {
        return (BigInteger) get(attributeName);
    }


    public Long getInt64(String attributeName) {
        return (Long) get(attributeName);
    }

    public String getString(String attributeName) {
        return (String) get(attributeName);
    }

    public InetAddress getInetAddress(String attributeName) {
        IPAddress a = (IPAddress) get(attributeName);
        if (a != null) {
            return a.toInetAddress();
        }
        else {
            return null;
        }
    }

    public byte[] getIPAddress(String attributeName) {
        return ((IPAddress) get(attributeName)).getInetAddressAsBytes();
    }

    public IPAddress getIPAddressObj(String attributeName) {
        return (IPAddress) get(attributeName);
    }

    public final byte[] serialize() {
        final byte[] bytes = new byte[getBytesSize()];
        final int length = serialize(bytes, 0);
        if (length != bytes.length) {
            throw new IllegalStateException("Expected to write " + bytes.length + " bytes, but wrote " + length);
        }
        return bytes;
    }

    public final void deserialize(byte[] bytes) throws EventSystemException {
        deserialize(bytes, 0, bytes.length);
    }

    public String toOneLineString() {
        return toString().replaceAll("\n", " ");
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Event && toString().equals(o.toString());
    }

    // These are here mainly for @Override to work properly
    public abstract void clear(String key);

    public abstract void reset();

    public abstract void setEventName(String name);

    public abstract String getEventName();

    public abstract void set(String key, FieldType type, Object value);

    public abstract void setEncoding();

    public abstract int getNumEventAttributes();

    public abstract Enumeration<String> getEventAttributeNames();

    public abstract Set<String> getEventAttributes();

    public abstract FieldType getType(String attributeName);

    public abstract Object get(String attributeName);

    public abstract int serialize(byte[] bytes, int offset);

    public abstract int serialize(DataOutput output) throws IOException;

    public abstract void deserialize(byte[] bytes, int offset, int length);

    public abstract void deserialize(DataInput stream, int length) throws IOException;

    public abstract int getBytesSize();

    public abstract Event copy();

    public void copyFrom(Event event) {
        reset();
        setEventName(event.getEventName());
        for (FieldAccessor field : event) {
            set(field.getName(), field.getType(), field.getValue());
        }
    }

    /**
     * Returns a String representation of this event
     *
     * @return a String return of this event.
     */
    @Override
    public String toString() {
        final String eventName = getEventName();
        if (eventName == null || eventName.isEmpty()) {
            return "";
        }

        StringBuffer sb = new StringBuffer();
        sb.append(eventName);
        sb.append("\n{\n");

        for (String field : new TreeSet<String>(getEventAttributes())) {
            final Object value = get(field);
            final String valueString;
            if (value == null) {
                valueString = "";
            }
            else if (value.getClass().isArray()) {
                valueString = Arrays.deepToString(new Object[]{value}).replaceFirst("^\\[(.*)\\]$", "$1");
            }
            else {
                valueString = value.toString();
            }
            sb.append("\t").append(field).append(" = ").append(valueString).append(";\n");
        }

        sb.append("}");
        return sb.toString();
    }

    protected static void checkShortStringLength(String string, int maxLength)
            throws EventSystemException {
        // try to find the length of the encoded string in the cache if it is
        // already calculated before, otherwise calculate it and update the cache.
        Integer serializedLength = encodedStringLengthCache.get(string);
        if(serializedLength == null) {
          serializedLength = EncodedString.getEncodedLength(string);
          encodedStringLengthCache.putIfAbsent(string, serializedLength);
        }
        if (serializedLength > maxLength) {
            throw new EventSystemException(
                    "String " + string + " was longer than maximum length: " + serializedLength + " > " + maxLength);
        }
    }

    /**
     * Please override this in subclasses for increased speed
     */
    public Iterator<FieldAccessor> iterator() {
        final Iterator<String> iterator = getEventAttributes().iterator();

        return new Iterator<FieldAccessor>() {
            public boolean hasNext() {
                return iterator.hasNext();
            }

            public FieldAccessor next() {
                final String name = iterator.next();
                return new DefaultFieldAccessor(name, getType(name), get(name));
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
