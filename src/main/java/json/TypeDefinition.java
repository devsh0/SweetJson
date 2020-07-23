package json;

public class TypeDefinition {
    private final String m_id;
    private final Class<?> m_klass;
    private final Class<?>[] m_args;

    public TypeDefinition (final Class<?> klass, Class<?>... args) {
        m_id = klass.getCanonicalName();
        m_klass = klass;
        m_args = args;
    }

    public Class<?> klass () {
        return m_klass;
    }

    public Class<?>[] type_args () {
        return m_args;
    }

    public Class<?> first_type_arg () {
        return m_args[0];
    }

    public Class<?> second_type_arg () {
        return m_args[1];
    }

    public boolean has_type_args () {
        return m_args != null;
    }

    @Override
    public int hashCode () {
        return m_id.hashCode() + 888;
    }

    @Override
    public boolean equals (final Object other) {
        if (!(other instanceof TypeDefinition))
            return false;
        var other_type = (TypeDefinition) other;
        return other_type.m_id.equals(m_id);
    }

    public static TypeDefinition wrap (final Class<?> klass) {
        return new TypeDefinition(klass);
    }
}
