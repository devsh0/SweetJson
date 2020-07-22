package json;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
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
}
