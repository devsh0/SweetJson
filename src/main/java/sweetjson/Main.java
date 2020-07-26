package sweetjson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

class Order<T> {
    static class PaymentOptions {
        private String name;
        private double limit;
    }

    public static class User {
        private String name;
        private String email;
        private String id;
        private List<PaymentOptions> payment_options;
    }

    private List<String> something;
    private User user;
    private String id;
    private String product;
    private int quantity;
    private double price;
    private boolean success;

    void print_details () {
        System.out.println("something: " + something.get(0));
        System.out.println("user-id: " + user.id);
        System.out.println("user-name: " + user.name);
        System.out.println("user-email: " + user.email);
        for (var option : user.payment_options)
            System.out.println(option.name);
        System.out.println("order-id: " + id);
        System.out.println("product: " + product);
        System.out.println("quantity: " + quantity);
        System.out.println("price: " + price);
        System.out.println("status: " + (success ? "succeeded" : "failed"));
    }

    boolean success () {
        return success;
    }
}

public class Main {

    private static void register_list_binder () {
        SweetJson.register_binder(Typedef.wrap(List.class), new JsonBinder() {
            @Override
            public Object construct (JsonElement element, Typedef type, Bag bag) {
                var model = new ArrayList<>();
                var arg = type.first_type_arg();
                var list = element.arraylist();
                list.forEach(entry -> model.add(entry.bind_to(arg, bag)));
                return model;
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static void main (String[] args) throws IOException {
        var json = Files.readString(Paths.get("test-file.json"));
        var element = JsonParser.parse(json);
        register_list_binder();
        // The serializer knows how to serialize lists.
        // We're good even if `Order` includes `List`s.
        var order = (Order<String>) element.bind_to_generic(Order.class, String.class);
        if (order.success())
            order.print_details();
    }
}
