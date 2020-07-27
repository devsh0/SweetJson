package sweetjson;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class JsonSerializationUtils {
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

    public static Object get_number_field (final JsonElement element, final Class<?> klass) {
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

    @SuppressWarnings("unchecked")
    public static <T> Typedef<T> get_field_typedef (final Field field, final Typedef<?> owner_typedef) {
        Type generic_type_of_field = field.getGenericType();
        Class<T> type_of_field = (Class<T>)owner_typedef.get_type_argument(generic_type_of_field.getTypeName());
        type_of_field = type_of_field == null ? (Class<T>)field.getType() : type_of_field;
        if (!(generic_type_of_field instanceof ParameterizedType))
            return Typedef.wrap(type_of_field);

        // Field is parameterized (e.g.: List<String> something).
        // FIXME: We don't handle cases like the type argument itself is parameterized (e.g.: List<List<String>>)
        var parameterized_type_of_field = (ParameterizedType) generic_type_of_field;
        Type[] type_arguments = parameterized_type_of_field.getActualTypeArguments();
        Class<?>[] klass_of_args = new Class<?>[type_arguments.length];

        for (int i = 0; i < klass_of_args.length; i++) {
            var mapping = owner_typedef.get_type_argument(type_arguments[i].getTypeName());
            klass_of_args[i] = mapping == null ? (Class<?>) type_arguments[i] : mapping;
        }

        return Typedef.<T>builder().set_klass(type_of_field).set_type_args(klass_of_args).build();
    }
}
