package json;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

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

    public static List<Field> get_serializable_fields (final Class<?> prototype) {
        var fields = prototype.getDeclaredFields();
        List<Field> filtered = new ArrayList<>(fields.length);
        for (var field : fields) {
            var modifier = field.getModifiers();
            if (!Modifier.isTransient(modifier))
                filtered.add(field);
        }
        return filtered;
    }

    public static void set_number_field (final Object model, final Field field, final JsonElement element)
            throws IllegalAccessException {
        var number = element.number();
        var class_name = field.getType().getName().toLowerCase();
        if (class_name.contains("byte"))
            field.set(model, (byte) number);
        else if (class_name.contains("char"))
            field.set(model, (char) number);
        else if (class_name.contains("short"))
            field.set(model, (short) number);
        else if (class_name.contains("int"))
            field.set(model, (int) number);
        else if (class_name.contains("long"))
            field.set(model, (long) number);
        else if (class_name.contains("float"))
            field.set(model, (float) number);
        else field.set(model, number);
    }
}
