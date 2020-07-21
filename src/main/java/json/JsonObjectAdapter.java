package json;

import java.util.Map;

public class JsonObjectAdapter {
    private final Map<String, JsonElement> m_map;
    private final Class<?> m_prototype;
    private final Object m_model;

    public JsonObjectAdapter (final Map<String, JsonElement> map, final Class<?> prototype) {
        m_map = map;
        m_prototype = prototype;
        m_model = JsonSerializationUtils.create_instance(prototype);
    }

    protected void write_primitive (final String key, final JsonElement element) {
        var fields = JsonSerializationUtils.get_serializable_fields(m_prototype);
        try {
            for (var field : fields) {
                if (field.getName().equalsIgnoreCase(key)) {
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
        } catch (IllegalAccessException exception) {
            throw new RuntimeException(exception);
        }
    }

    public Object build_model () {
        for (var entry : m_map.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();

            if (value.is_primitive())
                write_primitive(key, value);
        }
        return m_model;
    }
}
