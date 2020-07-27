package sweetjson;

import java.lang.reflect.Array;

public class JsonArrayBinder<T> implements JsonBinder<T> {
    public static final JsonBinder<?> INSTANCE = new JsonArrayBinder<>();

    private JsonArrayBinder () {
    }

    @SuppressWarnings("unchecked")
    public T construct (final JsonElement element, final Typedef<T> definition, final Bag bag) {
        final var list = element.arraylist();
        final var size = list.size();
        final var component_type = definition.klass().componentType();
        final var model = Array.newInstance(component_type, size);
        for (int i = 0; i < size; i++)
            Array.set(model, i, list.get(i).bind_to(component_type, bag));
        return (T)model;
    }
}
