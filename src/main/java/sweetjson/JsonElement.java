package sweetjson;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unchecked")
public class JsonElement {
    public enum JsonType {
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

    private Map<String, JsonElement> m_as_map = null;
    private List<JsonElement> m_as_list = null;

    JsonElement (final Object value) {
        m_value = value;
        if (value instanceof String)
            m_type = JsonType.STRING;
        else if (value instanceof Double)
            m_type = JsonType.NUMBER;
        else if (value instanceof Boolean)
            m_type = JsonType.BOOL;
        else if (value == null)
            m_type = JsonType.NULL;
        else if (value instanceof Map) {
            m_as_map = (Map<String, JsonElement>) value;
            m_type = JsonType.OBJECT;
        } else if (value instanceof List) {
            m_as_list = (List<JsonElement>) value;
            m_type = JsonType.ARRAY;
        } else throw new RuntimeException("Incompatible value type `" + value.getClass().getName() + "`");
    }

    private void verify_type_or_throw (final JsonType type, final String type_str) {
        if (m_type != type)
            throw new RuntimeException("Cannot convert " + m_type + " to " + type_str + "!");
    }

    final List<JsonElement> arraylist () {
        verify_type_or_throw(JsonType.ARRAY, "ArrayList");
        return m_as_list;
    }

    final Map<String, JsonElement> map () {
        verify_type_or_throw(JsonType.OBJECT, "Map");
        return m_as_map;
    }

    final String string () {
        verify_type_or_throw(JsonType.STRING, "String");
        return (String) m_value;
    }

    final double number () {
        verify_type_or_throw(JsonType.NUMBER, "double");
        return (Double) m_value;
    }

    final boolean bool () {
        verify_type_or_throw(JsonType.BOOL, "boolean");
        return (Boolean) m_value;
    }

    final Object object () {
        return m_value;
    }

    final JsonType get_type () {
        return m_type;
    }

    final boolean is_primitive () {
        return m_type != JsonType.ARRAY && m_type != JsonType.OBJECT && m_type != JsonType.UNKNOWN;
    }

    final <T> T bind_to (final Class<T> prototype) {
        var definition = Typedef.wrap(prototype);
        return SweetJson.get_binder(definition).construct(this, definition, Bag.empty());
    }

    final <T> T bind_to (final Class<T> prototype, final Bag bag) {
        var definition = Typedef.wrap(prototype);
        return SweetJson.get_binder(definition).construct(this, definition, Objects.requireNonNull(bag));
    }

    final <T> T bind_to_generic (final Class<T> prototype, Class<?>... type_args) {
        var definition = Typedef.<T>builder()
                .set_klass(prototype)
                .set_type_args(type_args)
                .build();
        return SweetJson.get_binder(definition).construct(this, definition, Bag.empty());
    }
}
