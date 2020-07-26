package sweetjson;

import java.util.HashMap;
import java.util.Map;

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

    public static Bag empty () {
        return new Bag();
    }
}
