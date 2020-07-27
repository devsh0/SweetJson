package sweetjson;

import java.util.HashMap;
import java.util.Map;

public class SweetJson {
    private static final Map<Typedef<?>, JsonBinder<?>> CUSTOM_BINDERS = new HashMap<>();

    public static <T> void register_binder (final Typedef<T> definition, final JsonBinder<T> binder) {
        CUSTOM_BINDERS.put(definition, binder);
    }

    @SuppressWarnings("unchecked")
    public static <T> JsonBinder<T> get_binder (final Typedef<T> type) {
        var binder = (JsonBinder<T>)CUSTOM_BINDERS.get(type);
        if (binder != null) return binder;
        return type.is_json_primitive() ? (JsonBinder<T>)JsonPrimitiveBinder.INSTANCE
                : (type.is_array() ? (JsonBinder<T>)JsonArrayBinder.INSTANCE
                : (JsonBinder<T>)JsonObjectBinder.INSTANCE);
    }
}
