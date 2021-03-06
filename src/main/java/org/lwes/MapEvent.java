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
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lwes.db.EventTemplateDB;
import org.lwes.serializer.Deserializer;
import org.lwes.serializer.DeserializerState;
import org.lwes.serializer.Serializer;

public class MapEvent extends DefaultEvent {
    private static transient Log log = LogFactory.getLog(MapEvent.class);

    /**
     * Event data
     */
    private final ConcurrentHashMap<String, BaseType> attributes = new ConcurrentHashMap<String, BaseType>();
    private String name = null;
    private EventTemplateDB eventTemplateDB = null;

    /**
     * If this is set to true, types and attributes are validated against the EventTemplateDB
     */
    private boolean validating = true;

    /**
     * Internal object for deserialization state
     */
    private DeserializerState state = null;

    /**
     * the size of the event in bytes
     */
    private int bytesStoreSize = 0;

    /**
     * Create an event with no name and no validation
     */
    public MapEvent() throws EventSystemException {
        this("");
    }

    /**
     * Create an event called <tt>eventName</tt> with no validation
     */
    public MapEvent(String eventName) throws EventSystemException {
        this(eventName, false, null);
    }

    /**
     * Create an event called <tt>eventName</tt>
     *
     * @param eventName       the name of the event
     * @param eventTemplateDB the EventTemplateDB to use for validation
     * @throws NoSuchEventException         if the Event does not exist in the EventTemplateDB
     * @throws NoSuchAttributeException     if an attribute does not exist in the EventTemplateDB
     * @throws NoSuchAttributeTypeException if an attribute type does not exist in the EventTemplateDB
     */
    public MapEvent(String eventName, EventTemplateDB eventTemplateDB)
            throws EventSystemException {
        this(eventName, true, eventTemplateDB);
    }

    /**
     * Create an event called <tt>eventName</tt>
     *
     * @param eventName       the name of the event
     * @param validate        true if the EventTemplateDB should be checked for types before all mutations
     * @param eventTemplateDB the EventTemplateDB to use for validation
     * @throws NoSuchEventException         if the Event does not exist in the EventTemplateDB
     * @throws NoSuchAttributeException     if an attribute does not exist in the EventTemplateDB
     * @throws NoSuchAttributeTypeException if an attribute type does not exist in the EventTemplateDB
     */
    public MapEvent(String eventName, boolean validate, EventTemplateDB eventTemplateDB)
            throws EventSystemException {
        checkShortStringLength(eventName, MAX_EVENT_NAME_SIZE);
        setEventTemplateDB(eventTemplateDB);
        validating = validate;
        setEventName(eventName);
        setEncoding();
        setDefaultValues(eventTemplateDB);
    }

    /**
     * Creates an event by deserializing a raw byte array.
     *
     * @param bytes           the raw bytes to convert
     * @param eventTemplateDB the EventTemplateDB to use to validate the event
     * @throws NoSuchEventException
     * @throws NoSuchAttributeException
     * @throws NoSuchAttributeTypeException
     */
    public MapEvent(byte[] bytes, EventTemplateDB eventTemplateDB)
            throws EventSystemException {
        this(bytes, true, eventTemplateDB);
    }


    /**
     * Creates an event by deserializing a raw byte array.
     *
     * @param bytes           the raw bytes to convert
     * @param validate        whether or not to validate the event
     * @param eventTemplateDB the EventTemplateDB to use to validate the event
     * @throws NoSuchEventException
     * @throws NoSuchAttributeException
     * @throws NoSuchAttributeTypeException
     */
    public MapEvent(byte[] bytes, boolean validate, EventTemplateDB eventTemplateDB)
            throws EventSystemException {
        setEventTemplateDB(eventTemplateDB);
        validating = validate;
        deserialize(bytes);
        setDefaultValues(eventTemplateDB);
    }

    public MapEvent(Event event) throws NoSuchAttributeException, EventSystemException {
        this();
        copyFrom(event);
    }

    @Override
    public void reset() {
        name = "";
        validating = false;
        eventTemplateDB = null;
        attributes.clear();
        if (state != null) {
            state.reset();
        }
        bytesStoreSize = 3;
    }

    protected void setDefaultValues(EventTemplateDB template) throws EventSystemException {
        if (template == null) {
            return;
        }
        for (Entry<String, BaseType> entry : template.getBaseTypesForEvent(getEventName()).entrySet()) {
            final String key = entry.getKey();
            final BaseType bt = entry.getValue();
            if (bt.getDefaultValue() != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Setting default value: " + key + "=" + bt.getDefaultValue());
                }
                set(key, bt.getType(), bt.getDefaultValue());
            }
        }
    }

    @Override
    public int getNumEventAttributes() {
        return attributes.size();
    }

    /**
     * Returns an enumeration of all the event attribute names
     *
     * @return an enumeration of attribute strings
     */
    @Override
    public Enumeration<String> getEventAttributeNames() {
        return attributes.keys();
    }

    @Override
    public SortedSet<String> getEventAttributes() {
        return new TreeSet<String>(attributes.keySet());
    }

    /**
     * Returns the number of attributes in the event
     *
     * @return number of attributes in the event
     */
    public int size() {
        return attributes.size();
    }

    /**
     * Returns true if the event validates against the EventTemplateDB before making changes
     *
     * @return the validating state
     */
    public boolean isValidating() {
        return this.validating;
    }

    /**
     * Set to true if the event should validate against the EventTemplateDB before making changes
     *
     * @param validate the validating value
     */
    public void setValidating(boolean validate) {
        this.validating = validate;
    }

    /**
     * Returns the EventTemplateDB for this event, used for validation of types and attributes.
     *
     * @return the EventTemplateDB
     */
    public EventTemplateDB getEventTemplateDB() {
        return this.eventTemplateDB;
    }

    /**
     * Sets the EventTemplateDB for this event, used for validation of types and attributes.
     *
     * @param eventTemplateDB the EventTemplateDB to be used for validation
     */
    public void setEventTemplateDB(EventTemplateDB eventTemplateDB) {
        this.eventTemplateDB = eventTemplateDB;
    }

    /**
     * Returns the name of the event
     *
     * @return the name of the event
     */
    @Override
    public synchronized String getEventName() {
        return this.name;
    }

    /**
     * Sets the name of the Event
     *
     * @param name the name of the event
     * @throws NoSuchEventException if the event is validating and does not exist in the EventTemplateDB
     */
    @Override
    public synchronized void setEventName(String name) {
        checkShortStringLength(name, MAX_EVENT_NAME_SIZE);

        /* determine if we already have the name and are just resetting it */
        if (this.name != null) {
            bytesStoreSize -= (this.name.length() + 1 + 2);
        }

        bytesStoreSize += (name.length() + 1 + 2);

        this.name = name;
    }

    /**
     * Set the character encoding for event strings
     *
     * @throws NoSuchAttributeTypeException if the type for the encoding attribute does not exist
     * @throws NoSuchAttributeException     if the encoding attribute does not exist
     */
    @Override
    public void setEncoding() throws EventSystemException {
        setInt16(ENCODING, UTF_8);
    }

    /**
     * Generic accessor, checks if an attribute exists and returns its value.  The user must do their
     * own type checking.
     *
     * @param attributeName name of the attribute to lookup
     * @return the object poitned to by attributeName
     * @throws NoSuchAttributeException if the attribute does not exist in this event
     */
    @Override
    public Object get(String attributeName) {
        if (attributes.containsKey(attributeName)) {
            return attributes.get(attributeName).getTypeObject();
        }

        return null;
    }

    @Override
    public void clear(String attributeName) {
        final BaseType bt = attributes.remove(attributeName);
        if (bt != null) {
            bytesStoreSize -= (attributeName.length() + 1) + bt.bytesStoreSize();
        }
    }

    /**
     * Set the object's attribute <tt>attributeName</tt> with the Object given
     *
     * @param attributeName  the name of the attribute to set
     * @param attributeValue the object to set the attribute with
     * @throws NoSuchAttributeException     if the attribute does not exist in this event
     * @throws NoSuchAttributeTypeException if there is an attribute with an undefined type
     */
    public void set(String attributeName, Object attributeValue) {
        if (isValidating() && getEventTemplateDB() != null) {
            if (getEventTemplateDB().checkForAttribute(getEventName(), attributeName)) {
                set(attributeName, getEventTemplateDB().getBaseTypeForObjectAttribute(getEventName(),
                                                                                      attributeName, attributeValue));
            }
        }
        else {
            throw new EventSystemException("Must be able to check the EventTemplateDB to use set(String,Object)");
        }
    }

    @Override
    public void set(String attribute, FieldType type, Object value) throws EventSystemException {
        set(attribute, new BaseType(type, value));
    }

    /**
     * @param attribute the name of the attribute to set
     * @param bt        the type of this attribute
     * @throws NoSuchAttributeException     if the attribute does not exist in this event
     * @throws NoSuchAttributeTypeException if there is an attribute with an undefined type
     */
    private void set(String attribute, BaseType bt) {
        checkShortStringLength(attribute, MAX_FIELD_NAME_SIZE);

        if (isValidating() && getEventTemplateDB() != null) {
            if (getEventTemplateDB().checkForAttribute(name, attribute)) {
                if (!getEventTemplateDB().checkTypeForAttribute(name, attribute, bt)) {
                    throw new EventSystemException("Wrong type '" + bt.getType() +
                                                   "' for " + name + "." + attribute);
                }
            }
            else {
                throw new EventSystemException("Attribute " + attribute + " does not exist for event " + name);
            }
            getEventTemplateDB().checkForSize(name, attribute, bt);
        }

        // Remove the existing value, and record the reduction in the serialized size.
        final BaseType oldObject = attributes.remove(attribute);
        if (oldObject != null) {
            bytesStoreSize -= (attribute.length() + 1) + oldObject.bytesStoreSize();
        }

        if (bt.getTypeObject() != null) {
            int newSize = bytesStoreSize + ((attribute.length() + 1) + bt.bytesStoreSize());
            if (newSize > MAX_MESSAGE_SIZE) {
                throw new EventSystemException("Event size limit is " + MAX_MESSAGE_SIZE + " bytes.");
            }

            bytesStoreSize += (attribute.length() + 1) + bt.bytesStoreSize();
            attributes.put(attribute, bt);
        }
    }

    /**
     * Serializes the Event into a byte array
     *
     * @return the serialized byte array
     * @throws EventSystemException if there is a bug in predicting the serialized size
     */
    @Override
    public int serialize(byte[] bytes, int offset) {
        /*
           * Serialization uses the following protocol
           * EVENTWORD,<number of elements>,ATTRIBUTEWORD,TYPETOKEN,
           * (UINT16|INT16|UINT32|INT32|UINT64|INT64|BOOLEAN|STRING)
           * ...ATTRIBUTEWORD,TYPETOKEN(UINT16|INT16|UINT32|INT32|
           * UINT64|INT64|BOOLEAN|STRING)
           *
           * The first attribute will always be the encoding if present.
           */
        int pos = 0;
        int attributeCount = attributes.size();

        pos += Serializer.serializeEVENTWORD(name, bytes, pos);
        pos += Serializer.serializeUINT16((short) (attributeCount), bytes, pos);

        /*
           * Set the encoding attributes in the event
           */
        BaseType encodingBase = attributes.get(ENCODING);
        if (encodingBase != null) {
            Object encodingObj = encodingBase.getTypeObject();
            FieldType encodingType = encodingBase.getType();
            if (encodingObj != null) {
                if (encodingType == FieldType.INT16) {
                    short encoding = UTF_8;   // ignore encodingObj, always set to UTF-8
                    if (log.isTraceEnabled()) {
                        log.trace("Character encoding: " + encoding);
                    }
                    pos += Serializer.serializeATTRIBUTEWORD(ENCODING, bytes, pos);
                    pos += Serializer.serializeBYTE(encodingType.token, bytes, pos);
                    pos += Serializer.serializeUINT16(encoding, bytes, pos);
                }
            }
        }
        else {
            if (log.isWarnEnabled()) {
                log.warn("Character encoding null in event " + name);
            }
        }

        Enumeration<String> e = attributes.keys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            if (key.equals(ENCODING)) {
                continue;
            }

            BaseType value = attributes.get(key);
            Object data = value.getTypeObject();
            FieldType type = value.getType();

            /* don't try to serialize nulls */
            if (data == null) {
                if (log.isWarnEnabled()) {
                    log.warn("Attribute " + key + " was null in event " + name);
                }
                continue;
            }

            pos += Serializer.serializeATTRIBUTEWORD(key, bytes, pos);
            pos += Serializer.serializeBYTE(type.token, bytes, pos);
            pos += Serializer.serializeValue(type, data, bytes, pos);

            if (log.isTraceEnabled()) {
                log.trace("Serialized attribute " + key);
            }
        } // while(e.hasMoreElements())

        final int bytesWritten = pos - offset;
        if (bytesStoreSize != bytesWritten) {
            String formatted = null;
            try {
                formatted = toString();
            } catch(Exception ex) {
                formatted = "<unprintable>";
            }
            throw new IllegalStateException("Expected to write " + bytesStoreSize +
                                            " bytes, but actually wrote " + bytesWritten
                                            +" for "+formatted);
        }

        return bytesWritten;
    }

    @Override
    public int serialize(DataOutput output) throws IOException {
        final byte[] bytes = serialize();
        output.write(bytes);
        return bytes.length;
    }

    /**
     * Deserialize the Event from byte array
     *
     * @param bytes the byte array containing a serialized Event
     * @throws EventSystemException
     */
    @Override
    public void deserialize(byte[] bytes, int offset, int length)
            throws EventSystemException {
        if (bytes == null) {
            return;
        }
        if (state == null) {
            state = new DeserializerState();
        }

        state.reset();
        state.incr(offset);
        setEventName(Deserializer.deserializeEVENTWORD(state, bytes));
        long num = Deserializer.deserializeUINT16(state, bytes);
        if (log.isTraceEnabled()) {
            log.trace("Event name = " + getEventName());
            log.trace("Number of attribute: " + num);
        }
        attributes.clear();
        bytesStoreSize = state.currentIndex() - offset;
        for (int i = 0; i < num; ++i) {
            String attribute = Deserializer.deserializeATTRIBUTEWORD(state, bytes);

            final FieldType type = FieldType.byToken(Deserializer.deserializeBYTE(state, bytes));
            if (log.isTraceEnabled()) {
                log.trace("Attribute: " + attribute);
                log.trace("Type: " + type);
                log.trace("State: " + state);
            }
            if (attribute != null) {
                if (i == 0 && attribute.equals(ENCODING)) {
                    if (type == FieldType.INT16) {
                        Deserializer.deserializeINT16(state, bytes);
                        setEncoding();   // ignore the encoding specified, always set to utf-8
                        continue;
                    }
                    else {
                        log.warn("Found encoding, but type was not int16 while deserializing");
                    }
                }

                set(attribute, type, Deserializer.deserializeValue(state, bytes, type));
            }
            if (bytesStoreSize != state.currentIndex() - offset) {
                throw new EventSystemException("Deserializing " + type + " field " + attribute +
                                               " resulted in incorrect cache of serialized size");
            }
        }

        if (bytesStoreSize != length) {
            throw new EventSystemException(
                    "Expected to deserialize " + length + " bytes, but actually read " + bytesStoreSize);
        }
    }

    @Override
    public void deserialize(DataInput stream, int length) throws IOException, EventSystemException {
        final byte[] bytes = new byte[length];
        stream.readFully(bytes);
        deserialize(bytes);
    }

    /**
     * Returns a mutable copy of the event.  This is a SLOW operation.
     *
     * @return Event the Event object
     * @throws NoSuchEventException         if the Event does not exist in the EventTemplateDB
     * @throws NoSuchAttributeException     if the attribute does not exist in this event
     * @throws NoSuchAttributeTypeException if there is an attribute that does not match a type in the EventTemplateDB
     */
    @Override
    public Event copy() throws EventSystemException {
        /* match the type-checking of the original event */
        MapEvent evt = new MapEvent(name, isValidating(), getEventTemplateDB());
        for (Enumeration<String> e = attributes.keys(); e.hasMoreElements(); ) {
            String key = e.nextElement();
            BaseType value = attributes.get(key);
            evt.set(key, value.cloneBaseType());
        }
        return evt;
    }

    /**
     * This method can be used to validate an event after it has been created.
     *
     * @throws ValidationExceptions A list of validation errors
     *                              use {@link EventTemplateDB#validate(Event)}
     */
    @Deprecated
    public void validate() throws ValidationExceptions {
        EventTemplateDB templ = getEventTemplateDB();
        if (templ == null) {
            ValidationExceptions ve = new ValidationExceptions(name);
            ve.addException(new EventSystemException("No template defined."));
            throw ve;
        }

        templ.validate(this);
    }

    @Override
    public FieldType getType(String field) {
        final BaseType bt = attributes.get(field);
        return bt == null ? null : bt.getType();
    }

    /**
     * This returns the number of bytes necessary for serialization, not the number of attributes.
     */
    @Override
    public int getBytesSize() {
        return bytesStoreSize;
    }
}
