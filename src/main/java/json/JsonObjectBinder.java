package json;

import java.lang.reflect.Field;
import java.util.Map;

public class JsonObjectBinder extends AbstractBinder {
    // These need to be reset for each new binding request.
    private Object m_model;
    private Map<String, Field> m_serializable_fields;

    private void bind_object (final String key, final JsonElement element) throws IllegalAccessException {
        var field = m_serializable_fields.get(key);
        if (field != null) {
            field.setAccessible(true);
            var field_type = field.getType();
            var binder = AbstractBinder.get_binder(field_type);
            field.set(m_model, binder.build_model(element, field_type));
        }
    }

    private void bind_primitive (final String key, final JsonElement element) throws IllegalAccessException {
        var field = m_serializable_fields.get(key);
        if (field != null) {
            field.setAccessible(true);
            field.set(m_model, get_primitive(element, field.getType()));
        }
    }

    public Object build_model (final JsonElement json_element, final Class<?> prototype) {
        try {
            Map<String, JsonElement> m_map = json_element.map();
            m_model = JsonSerializationUtils.create_instance(prototype);
            m_serializable_fields = JsonSerializationUtils.get_serializable_fields(prototype);

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
