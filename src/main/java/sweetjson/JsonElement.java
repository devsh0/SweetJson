package sweetjson;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unchecked")
public class JsonElement {
    public enum Type {
        UNKNOWN,
        STRING,
        NUMBER,
        BOOL,
        OBJECT,
        ARRAY,
        NULL
    }

    private final Object m_value;
    private final Type m_type;

    private Map<String, JsonElement> m_as_map = null;
    private List<JsonElement> m_as_list = null;

    JsonElement (final Object value) {
        m_value = value;
        if (value instanceof String)
            m_type = Type.STRING;
        else if (value instanceof Double)
            m_type = Type.NUMBER;
        else if (value instanceof Boolean)
            m_type = Type.BOOL;
        else if (value == null)
            m_type = Type.NULL;
        else if (value instanceof Map) {
            m_as_map = (Map<String, JsonElement>) value;
            m_type = Type.OBJECT;
        } else if (value instanceof List) {
            m_as_list = (List<JsonElement>) value;
            m_type = Type.ARRAY;
        } else throw new RuntimeException("Incompatible value type `" + value.getClass().getName() + "`");
    }

    private void verify_type_or_throw (final Type type, final String type_str) {
        if (m_type != type)
            throw new RuntimeException("Cannot convert value to " + type_str + "!");
    }

    final List<JsonElement> arraylist () {
        verify_type_or_throw(Type.ARRAY, "ArrayList");
        return m_as_list;
    }

    final Map<String, JsonElement> map () {
        verify_type_or_throw(Type.OBJECT, "Map");
        return m_as_map;
    }

    final String string () {
        verify_type_or_throw(Type.STRING, "String");
        return (String) m_value;
    }

    final double number () {
        verify_type_or_throw(Type.NUMBER, "double");
        return (Double) m_value;
    }

    final boolean bool () {
        verify_type_or_throw(Type.BOOL, "boolean");
        return (Boolean) m_value;
    }

    final Object object () {
        return m_value;
    }

    final Type get_type () {
        return m_type;
    }

    final boolean is_primitive () {
        return m_type != Type.ARRAY && m_type != Type.OBJECT && m_type != Type.UNKNOWN;
    }

    final Object bind_to (final Class<?> prototype) {
        var definition = Typedef.wrap(prototype);
        return SweetJson.get_binder(definition).construct(this, definition, Bag.empty());
    }

    final Object bind_to (final Class<?> prototype, final Bag bag) {
        var definition = Typedef.wrap(prototype);
        return SweetJson.get_binder(definition).construct(this, definition, Objects.requireNonNull(bag));
    }
}
