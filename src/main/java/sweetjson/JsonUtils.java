/*
 * Copyright (C) 2020 Devashish Jaiswal.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sweetjson;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class JsonUtils
{
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

    public static Object get_number_field (final JsonValue value, final Class<?> klass) {
        var number = value.as_double();
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
        // FIXME: We don't handle cases where type argument itself is parameterized (e.g.: List<List<String>>)
        var parameterized_type_of_field = (ParameterizedType) generic_type_of_field;
        Type[] type_arguments = parameterized_type_of_field.getActualTypeArguments();
        Class<?>[] klass_of_args = new Class<?>[type_arguments.length];

        for (int i = 0; i < klass_of_args.length; i++) {
            var mapping = owner_typedef.get_type_argument(type_arguments[i].getTypeName());
            klass_of_args[i] = mapping == null ? (Class<?>) type_arguments[i] : mapping;
        }

        return Typedef.<T>builder().set_klass(type_of_field).set_type_args(klass_of_args).build();
    }

    private static String[] get_type_parameter_names (final Class<?> klass) {
        var gstring = klass.toGenericString();
        var type_parameters_str = gstring.substring(gstring.indexOf("<"))
                .replace("<", "")
                .replace(">", "")
                .replaceAll("\\s","");
        return type_parameters_str.split(",");
    }
    
    public static Map<String, Class<?>> get_typearg_map (final Class<?> klass, final Class<?>[] arguments) {
        final Map<String, Class<?>> type_parameter_map = new HashMap<>();
        var type_parameters = get_type_parameter_names(klass);
        if (arguments.length != type_parameters.length) {
            final var format = "Too few/many type arguments (expected: %d, supplied: %d)!";
            throw new RuntimeException(String.format(format, type_parameters.length, arguments.length));
        }
        for (int i = 0; i < type_parameters.length; i++) {
            // Don't look...just don't look. Pretend it doesn't exist.
            var base = type_parameters[i];
            type_parameter_map.put(base, arguments[i]);
            type_parameter_map.put(base + "[]", arguments[i].arrayType());
            type_parameter_map.put(base + "[][]", arguments[i].arrayType().arrayType());
            type_parameter_map.put(base + "[][][]", arguments[i].arrayType().arrayType().arrayType());
        }
        return type_parameter_map;
    }
}
