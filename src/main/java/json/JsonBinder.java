package json;

import java.util.HashMap;
import java.util.Map;

public abstract class JsonBinder {
    private static final Map<TypeDefinition, Class<? extends JsonBinder>> CUSTOM_BINDERS = new HashMap<>();

    public abstract Object construct (final JsonElement element, final TypeDefinition definition);

    public static void register_new (final TypeDefinition definition, final Class<? extends JsonBinder> binder) {
        CUSTOM_BINDERS.put(definition, binder);
    }

    public static JsonBinder get_binder (final TypeDefinition type) {
        var binder = CUSTOM_BINDERS.get(type);
        binder = (binder == null) ? (type.is_array() ? JsonArrayBinder.class : JsonObjectBinder.class) : binder;
        return (JsonBinder)JsonSerializationUtils.create_instance(binder);
    }
}
