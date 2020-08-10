/*
 * Copyright (C) 2020 Devashish Jaiswal.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sweetjson;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
            elist.forEach(entry -> list.add(entry.bind_to(arg_type)));
            return list;
        });
    }

    static class C1<T> {
        private T value;
        private T[] array;
        private List<T> list;
    }

    @Test
    void test_binding_to_generic_types () {
        String data = "{\"value\": 24, \"array\": [1], \"list\": [1, 2]}";
        var json = parser(data).parse();
        C1<Byte> object = json.bind_to_generic(C1.class, Byte.class);
        assertEquals(object.value.byteValue(), 24);
        assertEquals(object.array[0].byteValue(), 1);
        assertEquals(object.list.get(0).byteValue(), 1);
        assertEquals(object.list.get(1).byteValue(), 2);
    }

    static class C2<T> { private List<List<T>> values; }

    @Test
    @Disabled
    // TODO: we don't support nested generic parameters.
    void test_binding_to_complex_generic_types () {
        String data = "{\"values\": [[1], [2]]}";
        var json = parser(data).parse();
        C2<Byte> object = json.bind_to_generic(C2.class, Byte.class);
        assertEquals(object.values.get(0).get(0).byteValue(), 1);
        assertEquals(object.values.get(1).get(0).byteValue(), 2);
    }

    static class C3 {
        private C3 object;
        private int value = 5;
        private int integer = 10;
        private String string = "string";
    }
    
    @Test
    void test_null_skipped_in_objects () {
        String data = "{\"value\": null}";
        var g3 = parser(data).parse().bind_to(C3.class);
        assertEquals(5, g3.value);

        data = "{\"object\": {\"integer\": 1, \"string\": null}}";
        g3 = parser(data).parse().bind_to(C3.class);
        assertEquals(1, g3.object.integer);
        assertEquals("string", g3.object.string);
    }

    @Test
    void test_null_skipped_in_arrays () {
        String data = "[1, 2, null, 4]";
        var array = parser(data).parse().bind_to(Integer[].class);
        assertEquals(3, array.length);
        assertEquals(1, array[0]);
        assertEquals(4, array[2]);
    }

    static class C4 {
        static class IC4 {
            private String message;
        }
        private IC4 object;
    }

    @Test
    void test_binding_to_inner_static_class () {
        String message = "Message from the inner class";
        String data = "{\"object\": {\"message\": \"Message from the inner class\"}}";
        var g4 = parser(data).parse().bind_to(C4.class);
        assertEquals(message, g4.object.message);
    }

    static class C5<A, B> {
        private B b;
        private A a;
        private Map<A, B> map;
    }

    @Test
    void test_binding_with_multiple_type_arguments () {
        SweetJson.register_binder(Typedef.wrap(Map.class), (element, definition, bag) -> {
            // Type Argument 1 is always string.
            var type_arg2 = definition.second_type_arg();
            var map = element.as_map();
            var model = new HashMap<>();
            for (var entry : map.entrySet())
                model.put(entry.getKey(), entry.getValue().bind_to(type_arg2));
            return model;
        });
        String data = "{\"a\":\"A\", \"b\":2, \"map\": {\"key1\": 40, \"key2\": 50}}";
        var json = parser(data).parse();
        C5<String, Integer> object = json.bind_to_generic(C5.class, String.class, Integer.class);
        assertEquals("A", object.a);
        assertEquals(2, object.b);
        assertEquals(40, object.map.get("key1"));
        assertEquals(50, object.map.get("key2"));
    }

    @Test
    void test_fails_while_binding_to_arrays_having_different_types_of_data () {
        String data = "[1, 2, \"string\"]";
        assertThrows(RuntimeException.class, () -> parser(data).parse().bind_to(Integer[].class));
    }

    @Test
    void test_binding_to_multi_dimensional_array () {
        String data = "[[1], [2]]";
        var array = parser(data).parse().bind_to(Integer[][].class);
        assertEquals(1, array[0][0]);
        assertEquals(2, array[1][0]);
    }

    static class C6<T> {
        private T[][] array;
    }

    @Test
    void test_binding_to_multi_dimensional_array_fields_of_parameterized_type () {
        String data = "{\"array\": [[1], [2]]}";
        var object = parser(data).parse().bind_to_generic(C6.class, Integer.class);
        assertEquals(1, object.array[0][0]);
    }
}
