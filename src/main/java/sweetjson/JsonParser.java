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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class JsonParser
{
    private static final List<Character> whitespaces = List.of(' ', '\r', '\n', '\b', '\t', '\f');

    private enum ParserState
    {
        UNINITIATED,
        INITIATED,
        TERMINATED
    }

    private ParserState m_state = ParserState.UNINITIATED;
    private int m_depth = 0;
    private final BufferedReader m_reader;

    public JsonParser (Path file_path) throws IOException
    {
        this(Files.readString(file_path));
    }

    public JsonParser (String json)
    {
        json = json.strip();
        var stream = new InputStreamReader(new ByteArrayInputStream(json.getBytes()), StandardCharsets.UTF_8);
        m_reader = new BufferedReader(stream);
    }

    private char read ()
    {
        try
        {
            int read = m_reader.read();
            throw_if(read == -1, "Attempted reading beyond EOF!");
            return (char) read;
        } catch (IOException ioe)
        {
            throw new RuntimeException("Failed to parse JSON!");
        }
    }

    private char peek ()
    {
        try
        {
            m_reader.mark(1);
            char next = read();
            m_reader.reset();
            return next;
        } catch (IOException ioe)
        {
            throw new RuntimeException("Failed to parse JSON!");
        }
    }

    private void throw_if (boolean condition, String message)
    {
        if (condition)
            throw new RuntimeException(message);
    }

    private RuntimeException expected (char... any_of)
    {
        Objects.requireNonNull(any_of, "Unexpected null argument!");
        throw_if(any_of.length == 0, "Unexpected empty argument!");
        StringBuilder message = new StringBuilder("Expected [");
        for (char ch : any_of)
            message.append("`").append(ch).append("`,");
        message.append("]");
        return new RuntimeException(message.toString());
    }

    private char read_or_throw (char... any_of)
    {
        Objects.requireNonNull(any_of, "Unexpected null argument!");
        throw_if(any_of.length == 0, "Unexpected empty argument!");
        var next = peek();
        for (char ch : any_of)
        {
            if (next == ch)
                return read();
        }
        throw expected(any_of);
    }

    private boolean read_on_match (char ch)
    {
        if (peek() == ch)
        {
            read();
            return true;
        }
        return false;
    }

    private String read_string (int length)
    {
        var builder = new StringBuilder();
        for (int i = 0; i < length; i++)
            builder.append(read());
        return builder.toString();
    }

    private boolean eof_reached ()
    {
        try
        {
            m_reader.mark(1);
            var next = m_reader.read();
            m_reader.reset();
            return next == -1;
        } catch (IOException ioe)
        {
            throw new RuntimeException(ioe);
        }
    }

    private void push_depth ()
    {
        boolean bad_state = m_state == ParserState.TERMINATED;
        throw_if(bad_state, "Unexpected parser state!");
        m_depth++;
        m_state = ParserState.INITIATED;
    }

    private void pop_depth ()
    {
        boolean bad_state = m_state == ParserState.UNINITIATED || m_state == ParserState.TERMINATED;
        throw_if(bad_state, "Unexpected parser state!");
        m_depth--;
        m_state = m_depth == 0 ? ParserState.TERMINATED : m_state;
    }

    private void consume_whitespaces ()
    {
        if (eof_reached()) return;
        while (whitespaces.contains(peek()))
            read();
    }

    private JsonElement.JsonType get_next_value_type ()
    {
        char next = peek();
        switch (next)
        {
            case '{':
                return JsonElement.JsonType.OBJECT;
            case '[':
                return JsonElement.JsonType.ARRAY;
            case '"':
                return JsonElement.JsonType.STRING;
            case 't':
            case 'f':
                return JsonElement.JsonType.BOOL;
            case 'n':
                return JsonElement.JsonType.NULL;
        }
        if (is_numeric(next))
            return JsonElement.JsonType.NUMBER;
        return JsonElement.JsonType.UNKNOWN;
    }

    private JsonElement parse_element (final JsonElement.JsonType type)
    {
        return switch (type)
                {
                    case STRING -> parse_string();
                    case NUMBER -> parse_number();
                    case BOOL -> parse_boolean();
                    case ARRAY -> parse_array();
                    case OBJECT -> parse_object();
                    case NULL -> parse_null();
                    default -> throw new RuntimeException("Element of UNKNOWN type cannot be parsed!");
                };
    }

    private JsonElement parse_string ()
    {
        throw_if(m_state != ParserState.INITIATED, "Invalid parser state!");
        StringBuilder builder = new StringBuilder();
        read_or_throw('"'); // consume " at the beginning
        while (true)
        {
            char current = read();
            switch (current)
            {
                case '\\':
                    switch (read())
                    {
                        case '\\' -> builder.append('\\');
                        case 'n' -> builder.append('\n');
                        case 'b' -> builder.append('\b');
                        case 'f' -> builder.append('\f');
                        case 'r' -> builder.append('\r');
                        case '"' -> builder.append('"');
                        case 't' -> builder.append('\t');
                        case 'u' -> {
                            var code_point_str = String.valueOf(read()) + read() + read() + read();
                            int code_point = Integer.parseInt(code_point_str, 16);
                            builder.appendCodePoint(code_point);
                        }
                        default -> throw new RuntimeException("Unknown escape character!");
                    }

                    break;
                case '"':
                    return new JsonElement(builder.toString());
                default:
                    builder.append(current);
            }
        }
    }

    private boolean is_numeric (char ch)
    {
        if (ch == '+' || ch == '-')
            return true;
        if (ch >= '0' && ch <= '9')
            return true;
        if (ch == '.' || ch == 'e' || ch == 'E')
            return true;
        return false;
    }

    private JsonElement parse_number ()
    {
        throw_if(m_state != ParserState.INITIATED, "Invalid parser state!");
        StringBuilder builder = new StringBuilder();
        while (is_numeric(peek()))
            builder.append(read());
        return new JsonElement(Double.parseDouble(builder.toString()));
    }

    private JsonElement parse_boolean ()
    {
        throw_if(m_state != ParserState.INITIATED, "Invalid parser state!");
        String literal = read_string(4);
        if (literal.equals("true"))
            return new JsonElement(Boolean.TRUE);
        literal += read();
        if (literal.equals("false"))
            return new JsonElement(Boolean.FALSE);
        throw new RuntimeException(String.format("Invalid literal `%s`!", literal));
    }

    private JsonElement parse_null ()
    {
        throw_if(m_state != ParserState.INITIATED, "Invalid parser state!");
        String literal = read_string(4);
        if (literal.equals("null"))
            return new JsonElement(null);
        throw new RuntimeException(String.format("Invalid literal `%s`!", literal));
    }

    private enum ArrayState
    {
        BEGIN,
        SEEN_OPEN,
        SEEN_CLOSE,
        SEEN_COMA,
        SEEN_ELEMENT
    }

    private JsonElement parse_array ()
    {
        ArrayState state = ArrayState.BEGIN;
        List<JsonElement> list = new ArrayList<>();
        char next = 0;

        for (; ; )
        {
            consume_whitespaces();
            switch (state)
            {
                case BEGIN:
                    read_or_throw('[');
                    push_depth();
                    state = ArrayState.SEEN_OPEN;
                    break;
                case SEEN_OPEN:
                    if (read_on_match(']'))
                    {
                        state = ArrayState.SEEN_CLOSE;
                        break;
                    }
                    var type = get_next_value_type();
                    list.add(parse_element(type));
                    state = ArrayState.SEEN_ELEMENT;
                    break;
                case SEEN_ELEMENT:
                    next = read_or_throw(']', ',');
                    state = next == ']' ? ArrayState.SEEN_CLOSE : ArrayState.SEEN_COMA;
                    break;
                case SEEN_COMA:
                    var value_type = get_next_value_type();
                    throw_if(value_type == JsonElement.JsonType.UNKNOWN, "UNKNOWN value type!");
                    // We didn't, but the state is identical.
                    state = ArrayState.SEEN_OPEN;
                    break;
                case SEEN_CLOSE:
                    pop_depth();
                    return new JsonElement(list);
            }
        }
    }

    private enum ObjectState
    {
        BEGIN,
        SEEN_OPEN,
        SEEN_KEY,
        SEEN_COLON,
        SEEN_VALUE,
        SEEN_COMA,
        SEEN_CLOSE
    }

    private JsonElement parse_object ()
    {
        ObjectState state = ObjectState.BEGIN;
        Map<String, JsonElement> map = new HashMap<>();
        String key = "";
        char next = 0;

        for (; ; )
        {
            consume_whitespaces();
            switch (state)
            {
                case BEGIN:
                    read_or_throw('{');
                    push_depth();
                    state = ObjectState.SEEN_OPEN;
                    break;
                case SEEN_OPEN:
                    if (read_on_match('}'))
                    {
                        state = ObjectState.SEEN_CLOSE;
                        break;
                    }
                    var tmp = parse_string();
                    key = tmp.as_string();
                    throw_if(key.isEmpty(), "Empty key not allowed!");
                    state = ObjectState.SEEN_KEY;
                    break;
                case SEEN_KEY:
                    read_or_throw(':');
                    state = ObjectState.SEEN_COLON;
                    break;
                case SEEN_COLON:
                    var type = get_next_value_type();
                    var element = parse_element(type);
                    map.put(key, element);
                    state = ObjectState.SEEN_VALUE;
                    break;
                case SEEN_VALUE:
                    next = read_or_throw(',', '}');
                    state = next == '}' ? ObjectState.SEEN_CLOSE : ObjectState.SEEN_COMA;
                    break;
                case SEEN_COMA:
                    var value_type = get_next_value_type();
                    throw_if(value_type == JsonElement.JsonType.UNKNOWN, "UNKNOWN value type!");
                    state = ObjectState.SEEN_OPEN;
                    break;
                case SEEN_CLOSE:
                    pop_depth();
                    return new JsonElement(map);
            }
        }
    }

    public JsonElement parse ()
    {
        try
        {
            var value_type = get_next_value_type();
            throw_if(value_type == null, "Invalid JSON input!");
            var element = value_type == JsonElement.JsonType.OBJECT ? parse_object() : parse_array();
            var bad_state = m_state != ParserState.TERMINATED || !eof_reached();
            throw_if(bad_state, "Invalid JSON input!");
            return element;
        } catch (RuntimeException re)
        {
            String message = re.getMessage();
            String vicinity = read_string(20);
            if (!vicinity.isEmpty())
                message += "\nFailed before reaching here: `" + vicinity + "`";
            System.err.println(message);
            throw re;
        }
    }

    private static void dispose (final JsonParser parser)
    {
        try
        {
            if (parser != null)
                parser.m_reader.close();
        } catch (IOException ioe)
        {
            throw new RuntimeException(ioe);
        }
    }

    public static JsonElement parse (final Path file_path)
    {
        JsonParser parser = null;
        try
        {
            parser = new JsonParser(file_path);
            return parser.parse();
        } catch (IOException ioe)
        {
            throw new RuntimeException(ioe);
        } finally
        {
            dispose(parser);
        }
    }

    public static JsonElement parse (final String json)
    {
        return (new JsonParser(json)).parse();
    }
}
