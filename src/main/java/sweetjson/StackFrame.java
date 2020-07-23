package sweetjson;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class StackFrame {
    private final Stack<Map<String, Object>> m_stack = new Stack<>();

    public void put (final String key, final Object value) {
        m_stack.peek().put(key, value);
    }

    public Object get (final String key) {
        return m_stack.peek().get(key);
    }

    public void init () {
        m_stack.push(new HashMap<>());
    }

    public void discard () {
        m_stack.pop();
    }
}
