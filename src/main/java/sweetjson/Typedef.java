package sweetjson;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class Typedef {
    private final String m_id;
    private final Class<?> m_klass;
    private final Class<?>[] m_type_args;
    private boolean m_is_generic_type;
    private final Map<String, Class<?>> m_type_parameter_map = new HashMap<>();

    private Typedef (final Class<?> klass, final Class<?>[] type_arguments) {
        m_id = klass.getCanonicalName().toLowerCase();
        m_klass = klass;
        m_type_args = type_arguments;
        if (klass.toGenericString().contains("<") && type_arguments.length > 0) {
            m_is_generic_type = true;
            associate_type_arguments();
        }
    }

    private void associate_type_arguments () {
        var gstring = klass().toGenericString();
        var type_parameters_str = gstring.substring(gstring.indexOf("<"))
                .replace("<", "")
                .replace(">", "")
                .replaceAll("\\s","");
        var type_parameters = type_parameters_str.split(",");
        if (m_type_args.length != type_parameters.length) {
            final var format = "Too few/many type arguments (expected: %d, supplied: %d)!";
            throw new RuntimeException(String.format(format, type_parameters.length, m_type_args.length));
        }
        for (int i = 0; i < type_parameters.length; i++) {
            m_type_parameter_map.put(type_parameters[i], m_type_args[i]);
            m_type_parameter_map.put(type_parameters[i] + "[]", m_type_args[i].arrayType());
        }
    }

    public Class<?> klass () {
        return m_klass;
    }

    public boolean is_array () {
        return klass().isArray();
    }

    public boolean is_json_primitive () {
        if (m_klass.isPrimitive()) return true;
        return m_klass == String.class
                || m_klass == Byte.class
                || m_klass == Short.class
                || m_klass == Character.class
                || m_klass == Integer.class
                || m_klass == Long.class
                || m_klass == Float.class
                || m_klass == Double.class;
    }

    public Class<?>[] type_args () {
        return m_type_args;
    }

    public boolean has_type_argument_mapping (final String type_parameter) {
        return m_type_parameter_map.containsKey(type_parameter);
    }

    public Class<?> get_type_argument (final String type_parameter) {
        return m_type_parameter_map.get(type_parameter);
    }

    public Class<?> first_type_arg () {
        return m_type_args[0];
    }

    public Class<?> second_type_arg () {
        return m_type_args[1];
    }

    public boolean has_type_args () {
        return m_type_args.length > 0;
    }

    public boolean is_generic_type () {
        return m_is_generic_type;
    }

    @Override
    public int hashCode () {
        return m_id.hashCode() + 888;
    }

    @Override
    public boolean equals (final Object other) {
        if (!(other instanceof Typedef))
            return false;
        var other_type = (Typedef) other;
        return other_type.m_id.equals(m_id);
    }

    public Object create_instance () {
        try {
            return klass().getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException
                | InstantiationException
                | IllegalAccessException
                | InvocationTargetException exc) {
            throw new RuntimeException(exc);
        }
    }

    public static class Builder {
        private Class<?> m_klass;
        private Class<?>[] m_type_args;

        public Builder set_klass (final Class<?> klass) {
            m_klass = klass;
            return this;
        }

        public Builder set_type_args (final Class<?>... type_args) {
            m_type_args = type_args == null ? new Class<?>[]{} : type_args;
            return this;
        }

        public Typedef build () {
            if (m_klass == null)
                throw new RuntimeException("No klass specified!");
            return new Typedef(m_klass, m_type_args);
        }
    }

    public static Typedef wrap (final Class<?> klass) {
        return new Typedef(klass, new Class<?>[]{});
    }

    public static Builder builder () {
        return new Builder();
    }
}
