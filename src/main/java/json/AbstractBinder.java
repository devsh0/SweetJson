package json;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractBinder {
    private static final Map<Class<?>, Class<? extends AbstractBinder>> CUSTOM_BINDERS = new HashMap<>();

    protected Object get_primitive (final JsonElement element, final Class<?> prototype) {
        return switch (element.get_type()) {
            case STRING -> element.string();
            case NUMBER -> JsonSerializationUtils.get_number_field(element, prototype);
            case BOOL -> element.bool();
            case NULL -> null;
            default -> throw new RuntimeException("Attempted to construct primitive from non-primitive value!");
        };
    }

    public abstract Object build_model (final JsonElement json_element, final Class<?> prototype);

    public static void register_binder (final Class<?> prototype, final Class<? extends AbstractBinder> binder) {
        CUSTOM_BINDERS.put(prototype, binder);
    }

    public static AbstractBinder get_binder (final Class<?> prototype) {
        var binder_type = CUSTOM_BINDERS.get(prototype);
        if (binder_type == null) {
            binder_type = prototype.isArray() ? JsonArrayBinder.class : JsonObjectBinder.class;
        }
        return (AbstractBinder)JsonSerializationUtils.create_instance(binder_type);
    }
}
