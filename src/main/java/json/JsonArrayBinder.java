package json;

import java.lang.reflect.Array;
import java.util.List;

public class JsonArrayBinder extends AbstractBinder {
    private final List<JsonElement> m_list;
    private final Object m_model;

    public JsonArrayBinder (final List<JsonElement> list, final Class<?> prototype) {
        super(prototype);
        m_list = list;
        m_model = Array.newInstance(prototype, list.size());
    }

    public Object build_model() {
        for (int i = 0; i < m_list.size(); i++) {
            var element = m_list.get(i);
            if (element.is_primitive())
                Array.set(m_model, i, get_primitive(element, m_prototype));
            else Array.set(m_model, i, m_list.get(i).to_object(m_prototype));
        }
        return m_model;
    }
}
