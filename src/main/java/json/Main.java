package json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

class Order {
    static class User {
        String name;
        String email;
        String id;
        int[] methods;
    }

    private User user;
    private String id;
    private String product;
    private int quantity;
    private Integer price;
    private boolean success;

    void print_details() {
        System.out.println("user-id: " + user.id);
        System.out.println("user-name: " + user.name);
        System.out.println("user-email: " + user.email);
        for (var method : user.methods)
            System.out.println("method: " + method);
        System.out.println("order-id: " + id);
        System.out.println("product: " + product);
        System.out.println("quantity: " + quantity);
        System.out.println("price: " + price);
        System.out.println("status: " + (success ? "succeeded" : "failed"));
    }

    boolean success() {
        return success;
    }
}

public class Main {
    public static void main (String[] args) throws IOException {
        var json = Files.readString(Paths.get("test-file.json"));
        var order = (Order)JsonParser.parse(json).to_object(Order.class);
        if (order.success())
            order.print_details();
    }
}
