### Features
- Super naive.
- Only 90% compliant with the specs.
- Deserialization not supported yet.
- Purposefully written to overcome boredom.

### Usage
_data.json_
```sweetjson
{
  "people": [
    {
      "firstname": "John",
      "lastname": "Doe",
      "age": 40,
      "skills": ["java", "c", "c++", "sql"]
    },
    {
      "firstname": "Jane",
      "lastname": "Doe",
      "age": 30,
      "skills": ["javascript", "python", "rust"]
    }
  ]
}
```

_Main.java_
```java
public class Main {
    public static void main(String[] args){
      var sweetjson = JsonParser.parse(Paths.get("data.sweetjson")).map(); // get data as map
      for (JsonElement json_element : sweetjson.get("people").arryalist()) {
          var person = json_element.map();
          var firstname = person.get("firstname").string();
          var lastname = person.get("lastname").string();
          var age = person.get("age").number();
          var skills = person.get("skills").arraylist();
      }
    }
}
```

Serialization is easy. The `parse` method returns a `JsonElement` which allows serializing itself to some model
through the `serialize` method. If the `JsonElement` is an array type, the serializer will return an array of
specified type wrapped in `Object`.

_data.json_
```sweetjson
{
  "user": {
    "name": "John Doe",
    "email": "johndoe@example.com",
    "id": "USRLJFDLJ5353"
  },
  "id": "ODRFFEKDLAJ23R3",
  "product": "Book",
  "quantity": 2,
  "price": 200.45,
  "success": true
}
```

_Main.java_
```java
class Order {
    static class User {
        String name;
        String email;
        String id;
    }

    private User user;
    private String id;
    private String product;
    private int quantity;
    private double price;
    private boolean success;

    void print_details() {
        System.out.println("user-id: " + user.id);
        System.out.println("user-name: " + user.name);
        System.out.println("user-email: " + user.email);
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
        var sweetjson = Files.readString(Paths.get("data.sweetjson"));
        var order = (Order)JsonParser.parse(sweetjson).serialize(Order.class);
        if (order.success())
            order.print_details();
    }
}
```

We can register our own serializers. Here we register a serializer for `List`. 

```java
public class Main {
    private static void register_list_binder () {
            JsonBinder.register_new(TypeDefinition.wrap(List.class), new JsonBinder() {
                @Override
                public Object construct (JsonElement element, TypeDefinition definition) {
                    var model = new ArrayList<>();
                    var arg = definition.first_type_arg();
                    var list = element.arraylist();
                    list.forEach(entry -> model.add(entry.serialize(arg)));
                    return model;
                }
            }.getClass());
        }
    
        public static void main (String[] args) throws IOException {
            var sweetjson = Files.readString(Paths.get("test-file.sweetjson"));
            var element = JsonParser.parse(sweetjson);
            register_list_binder();
            // The serializer knows how to serialize lists.
            // We're good even if `Order` includes `List`s.
            var order = (Order)element.serialize(Order.class);
            if (order.success())
                order.print_details();
        }
}
```
