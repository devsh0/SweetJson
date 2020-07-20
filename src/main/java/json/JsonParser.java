package json;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonParser {
    private boolean m_failed = false;
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
        } catch (IOException ioe) {
            System.out.println("IOError: " + ioe.getMessage());
            set_failed();
        }
        return builder.toString().getBytes();
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
            System.out.println("IOError: " + ioe.getMessage());
            set_failed();
            return "".getBytes();
        }
    }

    private char peek () {
        return (char) peek(1)[0];
    }

    private void set_failed () {
        m_failed = true;
    }

    private boolean failed () {
        boolean old = m_failed;
        m_failed = false;
        return old;
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

    private Element.Type get_next_element_type () {
        char next = peek();
        switch (next) {
            case '{':
                return Element.Type.OBJECT;
            case '[':
                return Element.Type.ARRAY;
            case '"':
                return Element.Type.STRING;
            case 't':
            case 'f':
                return Element.Type.BOOL;
        }
        if ((next >= '0' && next <= '9') || next == '+' || next == '-' || next == '.')
            return Element.Type.NUMBER;
        return Element.Type.UNKNOWN;
    }

    private Element parse_element (final Element.Type type) {
        return switch (type) {
            case STRING -> parse_string();
            case NUMBER -> parse_number();
            case BOOL -> parse_boolean();
            case ARRAY -> parse_array();
            case OBJECT -> parse_object();
            default -> null;
        };
    }

    private Element parse_string () {
        StringBuilder builder = new StringBuilder();
        read(); // consume " at the beginning
        while (true) {
            if (failed())
                return null;
            char current = read();
            switch (current) {
                case '\\':
                    builder.append(current);
                    builder.append(read());
                    break;
                case '"':
                    return new Element(builder.toString());
                default:
                    builder.append(current);
            }
        }
    }

    private Element parse_number () {
        StringBuilder builder = new StringBuilder();
        while (peek() != ',') {
            if (failed())
                return null;
            builder.append(read());
        }
        return new Element(Double.parseDouble(builder.toString()));
    }

    private Element parse_boolean () {
        String literal = new String(read(4));
        if (literal.equalsIgnoreCase("true"))
            return new Element(Boolean.TRUE);
        literal += read();
        if (literal.equalsIgnoreCase("false"))
            return new Element(Boolean.FALSE);
        return null;
    }

    private Element parse_array () {
        final int BEGIN = 0, SEEN_OPEN = 1, SEEN_CLOSE = 2, SEEN_COMA = 3, SEEN_ELEMENT = 4;
        int state = BEGIN;
        List<Element> list = new ArrayList<>();

        char next = 0;
        while (true) {
            switch (state) {
                case BEGIN:
                    consume_whitespaces();
                    if (read() != '[') return null;
                    state = SEEN_OPEN;
                    break;
                case SEEN_OPEN:
                    consume_whitespaces();
                    var type = get_next_element_type();
                    var element = parse_element(type);
                    if (element == null) return null;
                    list.add(element);
                    state = SEEN_ELEMENT;
                    break;
                case SEEN_ELEMENT:
                    consume_whitespaces();
                    next = read();
                    if (next != ']' && next != ',') return null;
                    state = next == ']' ? SEEN_CLOSE : SEEN_COMA;
                    break;
                case SEEN_COMA:
                    consume_whitespaces();
                    if (get_next_element_type() == Element.Type.UNKNOWN) return null;
                    // We didn't...but the state is identical.
                    state = SEEN_OPEN;
                    break;
                case SEEN_CLOSE:
                    return new Element(list);
            }
        }
    }

    private Element parse_object () {
        final int BEGIN = 0, SEEN_OPEN = 1, SEEN_KEY = 2, SEEN_COLON = 3;
        final int SEEN_VALUE = 4, SEEN_COMA = 5, SEEN_CLOSE = 6;
        int state = BEGIN;
        Map<String, Element> map = new HashMap<>();

        String key = "";
        char next = 0;
        while(true) {
            switch (state) {
                case BEGIN:
                    consume_whitespaces();
                    if (read() != '{') return null;
                    state = SEEN_OPEN;
                    break;
                case SEEN_OPEN:
                    consume_whitespaces();
                    next = peek();
                    if (next == '}') {
                        read();
                        state = SEEN_CLOSE;
                        continue;
                    }
                    if (next != '"') return null;
                    var tmp = parse_string();
                    if (tmp == null) return null;
                    key = tmp.string();
                    state = SEEN_KEY;
                    break;
                case SEEN_KEY:
                    consume_whitespaces();
                    if (read() != ':') return null;
                    state = SEEN_COLON;
                    break;
                case SEEN_COLON:
                    consume_whitespaces();
                    var type = get_next_element_type();
                    var element = parse_element(type);
                    if (element == null) return null;
                    map.put(key, element);
                    state = SEEN_VALUE;
                    break;
                case SEEN_VALUE:
                    consume_whitespaces();
                    next = read();
                    if (next != ',' && next != '}') return null;
                    state = next == '}' ? SEEN_CLOSE : SEEN_COMA;
                    break;
                case SEEN_COMA:
                    consume_whitespaces();
                    if (get_next_element_type() == Element.Type.UNKNOWN) return null;
                    state = SEEN_OPEN;
                    break;
                case SEEN_CLOSE:
                    return new Element(map);
            }
        }
    }

    public Element parse () {
        var error = new RuntimeException("ParseError");
        Element element = switch (get_next_element_type()) {
            case ARRAY -> parse_array();
            case OBJECT -> parse_object();
            default -> throw error;
        };
        if (element == null) throw error;
        return element;
    }

    public static Element parse (final Path file_path) {
        try {
            return (new JsonParser(file_path)).parse();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public static Element parse (final String json) {
        return (new JsonParser(json)).parse();
    }
}
