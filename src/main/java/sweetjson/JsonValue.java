/*
 * Copyright (C) 2020 Devashish Jaiswal.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sweetjson;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unchecked")
public class JsonValue
{
    public enum JsonType
    {
        UNKNOWN,
        STRING,
        NUMBER,
        BOOL,
        OBJECT,
        ARRAY,
        NULL
    }

    private final Object m_value;
    private final JsonType m_type;

    private Map<String, JsonValue> m_as_map = null;
    private List<JsonValue> m_as_list = null;

    JsonValue (final Object value)
    {
        m_value = value;
        if (value instanceof String)
            m_type = JsonType.STRING;
        else if (value instanceof Double)
            m_type = JsonType.NUMBER;
        else if (value instanceof Boolean)
            m_type = JsonType.BOOL;
        else if (value == null)
            m_type = JsonType.NULL;
        else if (value instanceof Map)
        {
            m_as_map = (Map<String, JsonValue>) value;
            m_type = JsonType.OBJECT;
        } else if (value instanceof List)
        {
            m_as_list = (List<JsonValue>) value;
            m_type = JsonType.ARRAY;
        } else throw new RuntimeException("Incompatible value type `" + value.getClass().getName() + "`");
    }

    public boolean is_null ()
    {
        return as_object() == null;
    }

    private void verify_type_or_throw (final JsonType type, final String type_str)
    {
        if (m_type != type)
            throw new RuntimeException("Cannot convert " + m_type + " to " + type_str + "!");
    }

    public final List<JsonValue> as_list ()
    {
        verify_type_or_throw(JsonType.ARRAY, "List");
        return m_as_list;
    }

    public final Map<String, JsonValue> as_map ()
    {
        verify_type_or_throw(JsonType.OBJECT, "Map");
        return m_as_map;
    }

    public final String as_string ()
    {
        verify_type_or_throw(JsonType.STRING, "String");
        return (String) m_value;
    }

    public final double as_double ()
    {
        verify_type_or_throw(JsonType.NUMBER, "double");
        return (Double) m_value;
    }

    public final float as_float ()
    {
        return (float) as_double();
    }

    public final int as_int ()
    {
        return (int) as_double();
    }

    public final char as_char ()
    {
        return (char) as_int();
    }

    public final byte as_byte ()
    {
        return (byte) as_int();
    }

    public final short as_short ()
    {
        return (short) as_int();
    }

    public final long as_long ()
    {
        return (long) as_double();
    }

    public final boolean is_number ()
    {
        return get_type() == JsonType.NUMBER;
    }

    public final boolean is_boolean ()
    {
        return get_type() == JsonType.BOOL;
    }

    public final boolean is_string ()
    {
        return get_type() == JsonType.STRING;
    }

    public final boolean is_array ()
    {
        return get_type() == JsonType.ARRAY;
    }

    public final boolean is_object ()
    {
        return get_type() == JsonType.OBJECT;
    }

    public final boolean as_bool ()
    {
        verify_type_or_throw(JsonType.BOOL, "boolean");
        return (Boolean) m_value;
    }

    public final Object as_object ()
    {
        return m_value;
    }

    public final JsonType get_type ()
    {
        return m_type;
    }

    public final boolean is_primitive ()
    {
        return m_type != JsonType.ARRAY && m_type != JsonType.OBJECT && m_type != JsonType.UNKNOWN;
    }

    public final <T> T bind_to (final Class<T> prototype)
    {
        var definition = Typedef.wrap(prototype);
        return SweetJson.get_binder(definition).construct(this, definition, Bag.empty());
    }

    public final <T> T bind_to (final Class<T> prototype, final Bag bag)
    {
        var definition = Typedef.wrap(prototype);
        return SweetJson.get_binder(definition).construct(this, definition, Objects.requireNonNull(bag));
    }

    public final <T> T bind_to_generic (final Class<T> prototype, Class<?>... type_args)
    {
        var definition = Typedef.<T>builder()
                .set_klass(prototype)
                .set_type_args(type_args)
                .build();
        return SweetJson.get_binder(definition).construct(this, definition, Bag.empty());
    }
}
