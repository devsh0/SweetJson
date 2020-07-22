package json;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

public class JsonSerializationUtils {
    public static Object create_instance (final Class<?> prototype) {
        try {
            return prototype.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException
                | InstantiationException
                | IllegalAccessException
                | InvocationTargetException exc) {
            throw new RuntimeException(exc);
        }
    }

    public static Map<String, Field> get_serializable_fields (final Class<?> prototype) {
        var fields = prototype.getDeclaredFields();
        Map<String, Field> filtered = new HashMap<>();
        for (var field : fields) {
            var modifier = field.getModifiers();
            if (!Modifier.isTransient(modifier))
                filtered.put(field.getName(), field);
        }
        return filtered;
    }

    public static Object get_number_field (final JsonElement element, Class<?> klass) {
        var number = element.number();
        var class_name = klass.getName().toLowerCase();
        if (class_name.contains("byte"))
            return (byte) number;
        else if (class_name.contains("char"))
            return (char) number;
        else if (class_name.contains("short"))
            return (short) number;
        else if (class_name.contains("int"))
            return (int) number;
        else if (class_name.contains("long"))
            return (long) number;
        else if (class_name.contains("float"))
            return (float) number;
        return number;
    }

    public static TypeDefinition get_type_definition (final Field field) {
        var gen_type = field.getGenericType();
        var klass = field.getType();
        if (!(gen_type instanceof ParameterizedType))
            return new TypeDefinition(klass);
        var type = (ParameterizedType) gen_type;
        var type_args = type.getActualTypeArguments();
        Class<?>[] args = new Class<?>[type_args.length];
        for (int i = 0; i < args.length; i++)
            args[i] = (Class<?>) type_args[i];
        return new TypeDefinition(klass, args);
    }

    public static Object get_primitive (final JsonElement element, final Class<?> prototype) {
        return switch (element.get_type()) {
            case STRING -> element.string();
            case NUMBER -> JsonSerializationUtils.get_number_field(element, prototype);
            case BOOL -> element.bool();
            case NULL -> null;
            default -> throw new RuntimeException("Attempted to construct primitive from non-primitive value!");
        };
    }
}
