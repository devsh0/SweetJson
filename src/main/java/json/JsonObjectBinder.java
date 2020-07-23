package json;

import java.lang.reflect.Field;
import java.util.Map;

@SuppressWarnings("unchecked")
public class JsonObjectBinder extends JsonBinder {
    public static final JsonBinder INSTANCE = new JsonObjectBinder();

    private final State m_state;
    private final String m_model_key = "model";
    private final String m_serializable_fields_key = "serializables";

    private JsonObjectBinder () {
        m_state = new State();
    }

    private void bind_object (final String key, final JsonElement element) throws IllegalAccessException {
        var model = m_state.get(m_model_key);
        var serializable_fields = (Map<String, Field>)m_state.get(m_serializable_fields_key);
        var field = serializable_fields.get(key);
        if (field != null) {
            field.setAccessible(true);
            var field_type = JsonSerializationUtils.get_type_definition(field);
            var binder = JsonBinder.get_binder(field_type);
            field.set(model, binder.construct(element, field_type));
        }
    }

    private void bind_primitive (final String key, final JsonElement element) throws IllegalAccessException {
        var model = m_state.get(m_model_key);
        var serializable_fields = (Map<String, Field>)m_state.get(m_serializable_fields_key);
        var field = serializable_fields.get(key);
        if (field != null) {
            field.setAccessible(true);
            field.set(model, JsonSerializationUtils.get_primitive(element, field.getType()));
        }
    }

    public Object construct (final JsonElement element, final TypeDefinition definition) {
        try {
            m_state.init();

            Map<String, JsonElement> m_map = element.map();
            var model = JsonSerializationUtils.create_instance(definition.klass());
            var serializable_fields = JsonSerializationUtils.get_serializable_fields(definition.klass());

            m_state.put(m_model_key, model);
            m_state.put(m_serializable_fields_key, serializable_fields);

            for (var entry : m_map.entrySet()) {
                var key = entry.getKey();
                var value = entry.getValue();
                if (value.is_primitive())
                    bind_primitive(key, value);
                else bind_object(key, value);
            }

            return model;
        } catch (IllegalAccessException exception) {
            throw new RuntimeException(exception);
        } finally {
            m_state.discard();
        }
    }
}
