package json;

import java.lang.reflect.Array;

public class JsonArrayBinder extends AbstractBinder {
    public Object build_model(final JsonElement json_element, final Class<?> prototype) {
        final var list = json_element.arraylist();
        final var component_type = prototype.componentType();
        final var model = Array.newInstance(component_type, list.size());
        for (int i = 0; i < list.size(); i++) {
            var element = list.get(i);
            if (element.is_primitive())
                Array.set(model, i, get_primitive(element, component_type));
            else Array.set(model, i, list.get(i).serialize(component_type));
        }
        return model;
    }
}
