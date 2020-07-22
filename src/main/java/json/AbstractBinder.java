package json;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractBinder {
    private static final Map<TypeDefinition, Class<? extends AbstractBinder>> CUSTOM_BINDERS = new HashMap<>();

    public abstract Object construct (final JsonElement element, final TypeDefinition definition);

    public static void register_new (final TypeDefinition definition, final Class<? extends AbstractBinder> binder) {
        CUSTOM_BINDERS.put(definition, binder);
    }

    public static AbstractBinder get_binder (final TypeDefinition type) {
        var binder = CUSTOM_BINDERS.get(type);
        binder = (binder == null) ? (type.klass().isArray() ? JsonArrayBinder.class : JsonObjectBinder.class) : binder;
        return (AbstractBinder)JsonSerializationUtils.create_instance(binder);
    }
}
