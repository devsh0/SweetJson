package sweetjson;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("unchecked")
public class DataBindingTest {
    private static JsonParser parser (final String json) {
        return new JsonParser(json);
    }

    @BeforeEach
    void add_list_binder () {
        SweetJson.register_binder(Typedef.wrap(List.class), (element, typedef, bag) -> {
            var list = new ArrayList<>();
            var arg_type = typedef.first_type_arg();
            var elist = element.as_list();
            if (typedef.is_generic_type()) {
                elist.forEach(entry -> list.add(entry.bind_to_generic(arg_type, typedef.type_args())));
                return list;
            }
            elist.forEach(entry -> list.add(entry.bind_to(arg_type)));
            return list;
        });
    }

    static class G1<T> {
        private T value;
        private T[] array;
        private List<T> list;
    }

    @Test
    void test_binding_to_generic_types () {
        String data = "{\"value\": 24, \"array\": [1], \"list\": [1, 2]}";
        var json = parser(data).parse();
        var object = (G1<Byte>)json.bind_to_generic(G1.class, Byte.class);
        assertEquals(object.value.byteValue(), 24);
        assertEquals(object.array[0].byteValue(), 1);
        assertEquals(object.list.get(0).byteValue(), 1);
        assertEquals(object.list.get(1).byteValue(), 2);
    }

    static class G2<T> { private List<List<T>> values; }

    @Test
    @Disabled
    // TODO: we don't support nested generic parameters.
    void test_binding_to_complex_generic_types () {
        String data = "{\"values\": [[1], [2]]}";
        var json = parser(data).parse();
        var object = (G2<Byte>)json.bind_to_generic(G2.class, Byte.class);
        assertEquals(object.values.get(0).get(0).byteValue(), 1);
        assertEquals(object.values.get(1).get(0).byteValue(), 2);
    }
}
