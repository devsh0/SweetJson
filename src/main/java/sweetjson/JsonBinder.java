package sweetjson;

@FunctionalInterface
public interface JsonBinder {
    Object construct (final JsonElement element, final TypeDefinition definition);
}
