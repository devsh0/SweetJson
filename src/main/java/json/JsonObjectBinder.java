package json;

import java.lang.reflect.Field;
import java.util.Map;

public class JsonObjectBinder extends JsonBinder {
    public static final JsonBinder INSTANCE = new JsonObjectBinder();

    private JsonObjectBinder () {
    }

    private void bind_object (final String key, final JsonElement element, final Object model, final Map<String, Field> serializable_fields)
            throws IllegalAccessException {
        var field = serializable_fields.get(key);
        if (field != null) {
            field.setAccessible(true);
            var field_type = JsonSerializationUtils.get_type_definition(field);
            var binder = JsonBinder.get_binder(field_type);
            field.set(model, binder.construct(element, field_type));
        }
    }

    private void bind_primitive (final String key, final JsonElement element, final Object model, final Map<String, Field> serializable_fields)
            throws IllegalAccessException {
        var field = serializable_fields.get(key);
        if (field != null) {
            field.setAccessible(true);
            field.set(model, JsonSerializationUtils.get_primitive(element, field.getType()));
        }
    }

    public Object construct (final JsonElement element, final TypeDefinition definition) {
        try {
            Map<String, JsonElement> m_map = element.map();
            var model = JsonSerializationUtils.create_instance(definition.klass());
            var serializable_fields = JsonSerializationUtils.get_serializable_fields(definition.klass());

            for (var entry : m_map.entrySet()) {
                var key = entry.getKey();
                var value = entry.getValue();
                if (value.is_primitive())
                    bind_primitive(key, value, model, serializable_fields);
                else bind_object(key, value, model, serializable_fields);
            }
            return model;
        } catch (IllegalAccessException exception) {
            throw new RuntimeException(exception);
        }
    }
}
