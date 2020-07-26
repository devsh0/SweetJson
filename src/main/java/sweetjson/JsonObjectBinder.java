package sweetjson;

import java.util.Map;

public class JsonObjectBinder implements JsonBinder {
    public static final JsonBinder INSTANCE = new JsonObjectBinder();

    public Object construct (final JsonElement element, final Typedef definition, final Bag bag) {
        try {
            Map<String, JsonElement> m_map = element.map();
            var model = definition.create_instance();
            var serializable_fields = JsonSerializationUtils.get_serializable_fields(definition.klass());

            for (var entry : m_map.entrySet()) {
                var field = serializable_fields.get(entry.getKey());
                if (field != null) {
                    field.setAccessible(true);
                    var field_type = JsonSerializationUtils.get_field_typedef(field, definition);
                    var binder = SweetJson.get_binder(field_type);
                    field.set(model, binder.construct(entry.getValue(), field_type, bag));
                }
            }

            return model;
        } catch (IllegalAccessException exception) {
            throw new RuntimeException(exception);
        }
    }
}
