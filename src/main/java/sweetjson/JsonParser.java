package sweetjson;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonParser {
    private final InputStream m_stream;

    public JsonParser (final Path file_path) throws IOException {
        m_stream = new BufferedInputStream(Files.newInputStream(file_path, StandardOpenOption.READ));
    }

    public JsonParser (final String json) {
        m_stream = new BufferedInputStream(new ByteArrayInputStream(json.getBytes()));
    }

    private byte[] read (int count) {
        StringBuilder builder = new StringBuilder();
        try {
            builder.append(new String(m_stream.readNBytes(count)));
            return builder.toString().getBytes();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private char read () {
        return (char) read(1)[0];
    }

    private byte[] peek (int count) {
        m_stream.mark(count);
        try {
            var bytes = read(count);
            m_stream.reset();
            return bytes;
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private char peek () {
        return (char) peek(1)[0];
    }

    private void consume_whitespaces () {
        while (true) {
            switch (peek()) {
                case ' ':
                case '\r':
                case '\n':
                case '\t':
                case '\f':
                    read();
                    break;
                default:
                    return;
            }
        }
    }

    private JsonElement.Type get_next_value_type () {
        char next = peek();
        switch (next) {
            case '{':
                return JsonElement.Type.OBJECT;
            case '[':
                return JsonElement.Type.ARRAY;
            case '"':
                return JsonElement.Type.STRING;
            case 't':
            case 'f':
                return JsonElement.Type.BOOL;
            case 'n':
                return JsonElement.Type.NULL;
        }
        if (is_numeric(next))
            return JsonElement.Type.NUMBER;
        return JsonElement.Type.UNKNOWN;
    }

    private JsonElement parse_element (final JsonElement.Type type) {
        return switch (type) {
            case STRING -> parse_string();
            case NUMBER -> parse_number();
            case BOOL -> parse_boolean();
            case ARRAY -> parse_array();
            case OBJECT -> parse_object();
            case NULL -> parse_null();
            default -> throw new RuntimeException("Element of UNKNOWN type cannot be parsed!");
        };
    }

    private JsonElement parse_string () {
        StringBuilder builder = new StringBuilder();
        read(); // consume " at the beginning
        while (true) {
            char current = read();
            switch (current) {
                case '\\':
                    builder.append(current);
                    builder.append(read());
                    break;
                case '"':
                    return new JsonElement(builder.toString());
                default:
                    builder.append(current);
            }
        }
    }

    private boolean is_numeric (char ch) {
        if (ch == '+' || ch == '-')
            return true;
        if (ch >= '0' && ch <= '9')
            return true;
        if (ch == '.' || ch == 'e' || ch == 'E')
            return true;
        return false;
    }

    private JsonElement parse_number () {
        StringBuilder builder = new StringBuilder();
        while (is_numeric(peek()))
            builder.append(read());
        return new JsonElement(Double.parseDouble(builder.toString()));
    }

    private JsonElement parse_boolean () {
        String literal = new String(read(4));
        if (literal.equals("true"))
            return new JsonElement(Boolean.TRUE);
        literal += read();
        if (literal.equals("false"))
            return new JsonElement(Boolean.FALSE);
        throw new RuntimeException("Invalid literal `" + literal + "` for boolean!");
    }

    private JsonElement parse_null () {
        String literal = new String(read(4));
        if (literal.equals("null"))
            return new JsonElement(null);
        throw new RuntimeException("Invalid literal `" + literal + "` for null!");
    }

    private JsonElement parse_array () {
        final int BEGIN = 0, SEEN_OPEN = 1, SEEN_CLOSE = 2, SEEN_COMA = 3, SEEN_ELEMENT = 4;
        int state = BEGIN;
        List<JsonElement> list = new ArrayList<>();

        char next = 0;
        while (true) {
            switch (state) {
                case BEGIN:
                    consume_whitespaces();
                    if (read() != '[')
                        throw new RuntimeException("Expected `[`!");
                    state = SEEN_OPEN;
                    break;
                case SEEN_OPEN:
                    consume_whitespaces();
                    if (peek() == ']') {
                        read();
                        state = SEEN_CLOSE;
                        break;
                    }
                    var type = get_next_value_type();
                    list.add(parse_element(type));
                    state = SEEN_ELEMENT;
                    break;
                case SEEN_ELEMENT:
                    consume_whitespaces();
                    next = read();
                    if (next != ']' && next != ',')
                        throw new RuntimeException("Expected `]` or `,`!");
                    state = next == ']' ? SEEN_CLOSE : SEEN_COMA;
                    break;
                case SEEN_COMA:
                    consume_whitespaces();
                    if (get_next_value_type() == JsonElement.Type.UNKNOWN)
                        throw new RuntimeException("Unknown value type!");
                    // We didn't...but the state is identical.
                    state = SEEN_OPEN;
                    break;
                case SEEN_CLOSE:
                    return new JsonElement(list);
            }
        }
    }

    private JsonElement parse_object () {
        final int BEGIN = 0, SEEN_OPEN = 1, SEEN_KEY = 2, SEEN_COLON = 3;
        final int SEEN_VALUE = 4, SEEN_COMA = 5, SEEN_CLOSE = 6;
        int state = BEGIN;
        Map<String, JsonElement> map = new HashMap<>();

        String key = "";
        char next = 0;
        while (true) {
            switch (state) {
                case BEGIN:
                    consume_whitespaces();
                    if (read() != '{')
                        throw new RuntimeException("Expected `{`!");
                    state = SEEN_OPEN;
                    break;
                case SEEN_OPEN:
                    consume_whitespaces();
                    next = peek();
                    if (next == '}') {
                        read();
                        state = SEEN_CLOSE;
                        break;
                    }
                    if (next != '"')
                        throw new RuntimeException("Expected `\"`!");
                    var tmp = parse_string();
                    key = tmp.string();
                    state = SEEN_KEY;
                    break;
                case SEEN_KEY:
                    consume_whitespaces();
                    if (read() != ':')
                        throw new RuntimeException("Expected `:`!");
                    state = SEEN_COLON;
                    break;
                case SEEN_COLON:
                    consume_whitespaces();
                    var type = get_next_value_type();
                    var element = parse_element(type);
                    map.put(key, element);
                    state = SEEN_VALUE;
                    break;
                case SEEN_VALUE:
                    consume_whitespaces();
                    next = read();
                    if (next != ',' && next != '}')
                        throw new RuntimeException("Expected `,` or `}`!");
                    state = next == '}' ? SEEN_CLOSE : SEEN_COMA;
                    break;
                case SEEN_COMA:
                    consume_whitespaces();
                    if (get_next_value_type() == JsonElement.Type.UNKNOWN)
                        throw new RuntimeException("Unknown value type!");
                    state = SEEN_OPEN;
                    break;
                case SEEN_CLOSE:
                    return new JsonElement(map);
            }
        }
    }

    public JsonElement parse () {
        try {
            JsonElement json_element = switch (get_next_value_type()) {
                case ARRAY -> parse_array();
                case OBJECT -> parse_object();
                default -> null;
            };
            if (json_element == null) throw new RuntimeException("Invalid JSON file!");
            return json_element;
        } catch (RuntimeException re) {
            String message = re.getMessage();
            String vicinity = new String(read(20)).replaceAll("\\s", "");
            if (!vicinity.isEmpty())
                message += "\nFailed before reaching here: `" + vicinity + "`";
            System.err.println(message);
            throw re;
        }
    }

    public static JsonElement parse (final Path file_path) {
        try {
            return (new JsonParser(file_path)).parse();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public static JsonElement parse (final String json) {
        return (new JsonParser(json)).parse();
    }
}
