package json;

import java.lang.reflect.Field;
import java.util.Map;

public abstract class AbstractBinder {
    protected final Class<?> m_prototype;
    protected final Map<String, Field> m_serializable_fields;

    protected AbstractBinder (final Class<?> prototype) {
        m_prototype = prototype;
        m_serializable_fields = JsonSerializationUtils.get_serializable_fields(m_prototype);
    }

    protected Object get_primitive (final JsonElement element, final Class<?> prototype) {
        return switch (element.get_type()) {
            case STRING -> element.string();
            case NUMBER -> JsonSerializationUtils.get_number_field(element, prototype);
            case BOOL -> element.bool();
            case NULL -> null;
            default -> throw new RuntimeException("Attempted to construct primitive from non-primitive value!");
        };
    }

    public abstract Object build_model ();
}
