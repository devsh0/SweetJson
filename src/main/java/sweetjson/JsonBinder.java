package sweetjson;

@FunctionalInterface
public interface JsonBinder {
    Object construct (final JsonElement element, final Typedef definition, final Bag bag);
}
