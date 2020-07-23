package sweetjson;

import java.lang.reflect.Array;

public class JsonArrayBinder implements JsonBinder {
    public static final JsonBinder INSTANCE = new JsonArrayBinder();

    private JsonArrayBinder () {
    }

    public Object construct (final JsonElement element, final TypeDefinition definition) {
        final var list = element.arraylist();
        final var size = list.size();
        final var component_type = definition.klass().componentType();
        final var model = Array.newInstance(component_type, size);
        for (int i = 0; i < size; i++)
            Array.set(model, i, list.get(i).to(component_type));
        return model;
    }
}
