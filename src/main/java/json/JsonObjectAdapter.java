package json;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("ALL")
public class JsonObjectAdapter {
    private final Map<String, JsonElement> m_map;
    private final Class<?> m_prototype;
    private final Object m_model;
    private final Map<String, Field> m_serializable_fields;

    public JsonObjectAdapter (final Map<String, JsonElement> map, final Class<?> prototype) {
        m_map = map;
        m_prototype = prototype;
        m_model = JsonSerializationUtils.create_instance(prototype);
        m_serializable_fields = JsonSerializationUtils.get_serializable_fields(m_prototype);
    }

    protected void write_primitive (final String key, final JsonElement element) throws IllegalAccessException {
        var field = m_serializable_fields.get(key);
        if (field != null) {
            field.setAccessible(true);
            switch (element.get_type()) {
                case STRING -> field.set(m_model, element.string());
                case NUMBER -> JsonSerializationUtils.set_number_field(m_model, field, element);
                case BOOL -> field.set(m_model, element.bool());
                case NULL -> field.set(m_model, null);
                default -> throw new RuntimeException("Attempted to construct primitive from non-primitive value!");
            }
        }

    }

    protected void write_object (final String key, final JsonElement element)
            throws IllegalAccessException {
        var field = m_serializable_fields.get(key);
        if (field != null) {
            field.setAccessible(true);

            if (Map.class.isAssignableFrom(field.getType())) {
                var json_map = element.map();
                var key_type = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                if (!key_type.equals(String.class))
                    throw new RuntimeException("Only String keys allowed in maps!");
                var map = new HashMap<String, Object>();
                for (var entry : json_map.entrySet())
                    map.put(entry.getKey(), entry.getValue().object());
                field.set(m_model, map);
            } else {
                var klass = field.getType();
                field.set(m_model, klass.cast(element.to_object(klass)));
            }
        }
    }

    public Object build_model () {
        try {
            for (var entry : m_map.entrySet()) {
                var key = entry.getKey();
                var value = entry.getValue();

                if (value.is_primitive())
                    write_primitive(key, value);
                var type = value.get_type();
                if (type == JsonElement.Type.OBJECT)
                    write_object(key, value);
            }
        } catch (IllegalAccessException exception) {
            new RuntimeException(exception);
        }
        return m_model;
    }
}
