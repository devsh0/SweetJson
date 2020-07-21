package json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main (String[] args) throws IOException {
        var json = Files.readString(Paths.get("test-file.json"));
        var object = JsonParser.parse(json).arraylist();
        var middle = object.get(0).map();
        var threads = middle.get("threads").arraylist();
        var first_thread = threads.get(0).map();
        System.out.println(first_thread.get("com").string());
    }
}
