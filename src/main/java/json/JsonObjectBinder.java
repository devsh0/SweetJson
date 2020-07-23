package json;

import java.util.Map;

public class JsonObjectBinder extends JsonBinder {
    public static final JsonBinder INSTANCE = new JsonObjectBinder();

    public Object construct (final JsonElement element, final TypeDefinition definition) {
        try {
            Map<String, JsonElement> m_map = element.map();
            var model = JsonSerializationUtils.create_instance(definition.klass());
            var serializable_fields = JsonSerializationUtils.get_serializable_fields(definition.klass());

            for (var entry : m_map.entrySet()) {
                var field = serializable_fields.get(entry.getKey());
                if (field != null) {
                    field.setAccessible(true);
                    var field_type = JsonSerializationUtils.get_type_definition(field);
                    var binder = JsonBinder.get_binder(field_type);
                    field.set(model, binder.construct(entry.getValue(), field_type));
                }
            }

            return model;
        } catch (IllegalAccessException exception) {
            throw new RuntimeException(exception);
        }
    }
}
