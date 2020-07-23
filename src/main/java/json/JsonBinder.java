package json;

import java.util.HashMap;
import java.util.Map;

public abstract class JsonBinder {
    private static final Map<TypeDefinition, JsonBinder> CUSTOM_BINDERS = new HashMap<>();

    public abstract Object construct (final JsonElement element, final TypeDefinition definition);

    public static void register_new (final TypeDefinition definition, final JsonBinder binder) {
        CUSTOM_BINDERS.put(definition, binder);
    }

    public static JsonBinder get_binder (final TypeDefinition type) {
        var binder = CUSTOM_BINDERS.get(type);
        if (binder != null) return binder;
        return type.is_json_primitive() ? JsonPrimitiveBinder.INSTANCE
                : (type.is_array() ? JsonArrayBinder.INSTANCE : JsonObjectBinder.INSTANCE);
    }
}
