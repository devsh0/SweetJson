package sweetjson;

@FunctionalInterface
public interface JsonBinder<T> {
     T construct (final JsonElement element, final Typedef<T> definition, final Bag bag);
}
