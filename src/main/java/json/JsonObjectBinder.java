package json;

import java.util.Map;

import static json.JsonElement.Type.OBJECT;

public class JsonObjectBinder extends AbstractBinder {
    private final Map<String, JsonElement> m_map;
    private final Object m_model;

    public JsonObjectBinder (final Map<String, JsonElement> map, final Class<?> prototype) {
        super(prototype);
        m_map = map;
        m_model = JsonSerializationUtils.create_instance(prototype);
    }

    private void bind_object (final String key, final JsonElement element) throws IllegalAccessException {
        var field = super.m_serializable_fields.get(key);
        if (field != null) {
            field.setAccessible(true);
            var field_type = field.getType();
            var is_object = element.get_type() == OBJECT;
            var klass = is_object ? field_type : field_type.getComponentType();
            field.set(m_model, is_object ? element.to_object(klass) : element.to_array_of(klass));
        }
    }

    private void bind_primitive (final String key, final JsonElement element) throws IllegalAccessException {
        var field = super.m_serializable_fields.get(key);
        if (field != null) {
            field.setAccessible(true);
            field.set(m_model, get_primitive(element, field.getType()));
        }
    }

    public Object build_model () {
        try {
            for (var entry : m_map.entrySet()) {
                var key = entry.getKey();
                var value = entry.getValue();
                if (value.is_primitive())
                    bind_primitive(key, value);
                else bind_object(key, value);
            }
        } catch (IllegalAccessException exception) {
            throw new RuntimeException(exception);
        }
        return m_model;
    }
}
