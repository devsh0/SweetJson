package sweetjson;

public class JsonPrimitiveBinder implements JsonBinder {
    public static final JsonBinder INSTANCE = new JsonPrimitiveBinder();

    @Override
    public Object construct (final JsonElement element, final Typedef definition, final Bag bag) {
        var type = element.get_type();
        return switch (type) {
            case STRING -> element.string();
            case NUMBER -> JsonSerializationUtils.get_number_field(element, definition.klass());
            case BOOL -> element.bool();
            case NULL -> element.object();
            default -> throw new RuntimeException("Attempted to construct primitive from `" +  (type) + "`!");
        };
    }
}
