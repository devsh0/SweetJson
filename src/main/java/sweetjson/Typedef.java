package sweetjson;

import java.lang.reflect.InvocationTargetException;

public class Typedef {
    private final String m_id;
    private final Class<?> m_klass;
    private final Class<?>[] m_type_args;

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

    private Typedef (final Class<?> klass, final Class<?>[] type_args) {
        m_id = klass.getCanonicalName().toLowerCase();
        m_klass = klass;
        m_type_args = type_args;
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

    public Class<?> first_type_arg () {
        return m_type_args[0];
    }

    public Class<?> second_type_arg () {
        return m_type_args[1];
    }

    public boolean has_type_args () {
        return m_type_args.length > 0;
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

    public static Typedef wrap (final Class<?> klass) {
        return new Typedef(klass, new Class<?>[]{});
    }

    public static Builder builder () {
        return new Builder();
    }
}
