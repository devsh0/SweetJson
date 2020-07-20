package json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main (String[] args) throws IOException {
        var json = Files.readString(Paths.get("test-file.json"));
        var object = JsonParser.parse(json).map();
        var null_value = object.get("null").object();
        System.out.println(null_value);
    }
}
