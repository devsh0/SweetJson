package sweetjson;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Bag {
    private static final String TYPE_ARGUMENT_KEY = "__type_argument__";

    private final Map<String, Object> m_container = new HashMap<>();

    public Bag put (final String key, final Object object) {
        m_container.put(key.toLowerCase(), object);
        return this;
    }

    public Object take (final String key) {
        return m_container.remove(key);
    }

    public Object peek (final String key) {
        return m_container.get(key);
    }

    public boolean has (final String key) {
        return m_container.containsKey(key);
    }

    public boolean has_type_arguments () {
        return has(TYPE_ARGUMENT_KEY);
    }

    public Class<?>[] take_type_arguments () {
        return (Class<?>[])take(TYPE_ARGUMENT_KEY);
    }

    public static Bag put_type_arguments (final Class<?>... type_args) {
        var args = Objects.requireNonNull(type_args);
        return Bag.empty().put(TYPE_ARGUMENT_KEY, args);
    }

    public static Bag empty () {
        return new Bag();
    }
}
