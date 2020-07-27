package sweetjson;

public class JsonPrimitiveBinder<T> implements JsonBinder<T> {
    public static final JsonBinder<?> INSTANCE = new JsonPrimitiveBinder<>();

    @SuppressWarnings("unchecked")
    @Override
    public T construct (final JsonElement element, final Typedef<T> definition, final Bag bag) {
        var type = element.get_type();
        return switch (type) {
            case STRING -> (T)element.string();
            case NUMBER -> (T)JsonSerializationUtils.get_number_field(element, definition.klass());
            case BOOL -> (T)Boolean.valueOf(element.bool());
            case NULL -> (T)element.object();
            default -> throw new RuntimeException("Attempted to construct primitive from `" +  (type) + "`!");
        };
    }
}
