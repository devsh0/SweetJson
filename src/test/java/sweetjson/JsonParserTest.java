/*
 * Copyright (C) 2010 Google Inc.
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonParserTest {

    private static JsonParser parser (final String json) {
        return new JsonParser(json);
    }

    @Test
    void test_parsing_object () {
        var json = parser("{}").parse();
        assertEquals(json.get_type(), JsonElement.JsonType.OBJECT);
        assertTrue(json.as_map().isEmpty());

        json = parser("{\"a\": \"b\"}").parse();
        assertEquals("b", json.as_map().get("a").as_string());
    }

    @Test
    void test_parsing_array () {
        var json = parser("[]").parse();
        assertEquals(json.get_type(), JsonElement.JsonType.ARRAY);
        assertTrue(json.as_list().isEmpty());

        json = parser("[1, 2]").parse();
        var alist = json.as_list();
        assertEquals(2, alist.size());
        assertEquals(alist.get(0).as_double(), 1);
        assertEquals(alist.get(1).as_double(), 2);
    }

    @Test
    void test_parsing_string_with_escape_sequences () {
        var json = parser("{\"string\": \"\\\\some\nstring\"}").parse();
        assertEquals("\\some\nstring", json.as_map().get("string").as_string());
    }

    @Test
    void test_parsing_null () {
        var json = parser("{\"null\": null}").parse();
        assertNull(json.as_map().get("null").as_object());
    }

    @Test
    void test_empty_string () {
        // FIXME: We need to throw better exceptions.
        assertThrows(RuntimeException.class, () -> parser("").parse());
    }

    @Test
    public void test_doubles () {
        String json = "[-0.0,"
                + "1.0,"
                + "1.7976931348623157E308,"
                + "4.9E-324,"
                + "0.0,"
                + "-0.5,"
                + "2.2250738585072014E-308,"
                + "3.141592653589793,"
                + "2.718281828459045]";
        var list = parser(json).parse().as_list();
        assertEquals(-0.0, list.get(0).as_double());
        assertEquals(1.0, list.get(1).as_double());
        assertEquals(1.7976931348623157E308, list.get(2).as_double());
        assertEquals(4.9E-324, list.get(3).as_double());
        assertEquals(0.0, list.get(4).as_double());
        assertEquals(-0.5, list.get(5).as_double());
        assertEquals(2.2250738585072014E-308, list.get(6).as_double());
        assertEquals(3.141592653589793, list.get(7).as_double());
        assertEquals(2.718281828459045, list.get(8).as_double());
    }

    @Test
    void test_longs () {
        String json = "[0,0,0,"
                + "1,1,1,"
                + "-1,-1,-1,"
                + "-9223372036854775808,"
                + "9223372036854775807]";
        var list = parser(json).parse().as_list();
        assertEquals(0L, list.get(0).as_long());
        assertEquals(0, list.get(1).as_long());
        assertEquals(0.0, list.get(2).as_long());
        assertEquals(1L, list.get(3).as_long());
        assertEquals(1, list.get(4).as_long());
        assertEquals(1.0, list.get(5).as_long());
        assertEquals(-1L, list.get(6).as_long());
        assertEquals(-1, list.get(7).as_long());
        assertEquals(-1.0, list.get(8).as_long());
        assertEquals(Long.MIN_VALUE, list.get(9).as_long());
        assertEquals(Long.MAX_VALUE, list.get(10).as_long());
    }

    @Test
    void test_boolean () {
        var json = parser("[true, false]").parse();
        assertTrue(json.as_list().get(0).as_bool());
        assertFalse(json.as_list().get(1).as_bool());
    }

    private void assert_not_number (final String number) {
        assertThrows(RuntimeException.class, () -> parser("[" + number + "]").parse());
    }

    @Test
    void test_malformed_numbers () {
        assert_not_number("-");
        assert_not_number(".");

        // exponent lacks digit
        assert_not_number("e");
        assert_not_number("0e");
        assert_not_number(".e");
        assert_not_number("0.e");
        assert_not_number("-.0e");

        // no integer
        assert_not_number("e1");
        assert_not_number(".e1");
        assert_not_number("-e1");

        // trailing characters
        assert_not_number("1x");
        assert_not_number("1.1x");
        assert_not_number("1e1x");
        assert_not_number("1ex");
        assert_not_number("1.1ex");
        assert_not_number("1.1e1x");

        // FIXME: These throw in Gson.
        // fraction has no digit
        //assert_not_number("0.");
        //assert_not_number("-0.");
        //assert_not_number("0.e1");
        //assert_not_number("-0.e1");

        // no leading digit
        // assert_not_number(".0");
        //assert_not_number("-.0");
        // assert_not_number(".0e1");
        // assert_not_number("-.0e1");
    }

    @Test
    public void test_missing_value () {
        assertThrows(RuntimeException.class, () -> parser("{\"a\":}").parse());
    }

    @Test
    void test_premature_end_of_input () {
        assertThrows(RuntimeException.class, () -> parser("{\"a\": 1,}").parse());
    }

    @Test
    void test_unquoted_keys () {
        assertThrows(RuntimeException.class, () -> parser("{a:true}").parse());
    }

    @Test
    void test_single_quoted_keys () {
        assertThrows(RuntimeException.class, () -> parser("{'a':true}").parse());
    }

    @Test
    void test_multiple_top_level_values () {
        assertThrows(RuntimeException.class, () -> parser("[][]").parse());
        assertThrows(RuntimeException.class, () -> parser("{}{}").parse());
    }

    @Test
    void test_missing_top_level_value () {
        assertThrows(RuntimeException.class, () -> parser("\"a\": \"1\"").parse());
    }

    @Test
    void test_unterminated_array () {
        assertThrows(RuntimeException.class, () -> parser("{\n" +
                "  \"a\": [1, 2\n" +
                "}").parse());
    }

    @Test
    void test_unterminated_object () {
        assertThrows(RuntimeException.class, () -> parser("{\n" +
                "  \"a\": {\"b\":  \"c\",\n" +
                "    \"d\": \"e\"\n" +
                "}").parse());
    }

    @Test
    void test_unterminated_string () {
        assertThrows(RuntimeException.class, () -> parser("{\"a\":  \"an unterminated string}").parse());
    }

    @Test
    void test_empty_key_string () {
        assertThrows(RuntimeException.class, () -> parser("{\"\": 1}").parse());
    }

    @Test
    void test_empty_value_string () {
        assertTrue(parser("{\"a\": \"\"}").parse().as_map().get("a").as_string().isEmpty());
    }

    @Test
    void test_extra_values_in_object () {
        assertThrows(RuntimeException.class, () -> parser("{\"a\": 1,2}").parse());
    }
}
