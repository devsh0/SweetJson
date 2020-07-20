package json;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class Element {
    public enum Type {
        UNKNOWN,
        STRING,
        NUMBER,
        BOOL,
        OBJECT,
        ARRAY,
        NULL
    }

    private final Object m_value;
    private final Type m_type;

    private Map<String, Element> m_as_map = null;
    private List<Element> m_as_list = null;

    Element (final Object value) {
        m_value = value;
        if (value instanceof String)
            m_type = Type.STRING;
        else if (value instanceof Double)
            m_type = Type.NUMBER;
        else if (value instanceof Boolean)
            m_type = Type.BOOL;
        else if (value == null)
            m_type = Type.NULL;
        else if (value instanceof Map) {
            m_as_map = (Map<String, Element>) value;
            m_type = Type.OBJECT;
        } else if (value instanceof List) {
            m_as_list = (List<Element>) value;
            m_type = Type.ARRAY;
        } else throw new RuntimeException("Incompatible value type `" + value.getClass().getName() + "`");
    }

    private void verify_type_or_throw (final Type type, final String type_str) {
        if (m_type != type)
            throw new RuntimeException("Cannot convert value to " + type_str + "!");
    }

    final List<Element> arraylist () {
        verify_type_or_throw(Type.ARRAY, "ArrayList");
        return m_as_list;
    }

    final Map<String, Element> map () {
        verify_type_or_throw(Type.OBJECT, "Map");
        return m_as_map;
    }

    final String string () {
        verify_type_or_throw(Type.STRING, "String");
        return (String) m_value;
    }

    final double number () {
        verify_type_or_throw(Type.NUMBER, "double");
        return (Double) m_value;
    }

    final boolean bool () {
        verify_type_or_throw(Type.BOOL, "boolean");
        return (Boolean) m_value;
    }

    final Object object () {
        return m_value;
    }
}
