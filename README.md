### Features
- Super naive.
- Only 90% compliant with the specs.
- Purposefully written to overcome boredom.

### Usage
_data.json_
```json
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
      var json = JsonParser.parse(Paths.get("data.json")).map(); // get data as map
      for (JsonElement json_element : json.get("people").arryalist()) {
          var person = json_element.map();
          var firstname = person.get("firstname").string();
          var lastname = person.get("lastname").string();
          var age = person.get("age").number();
          var skills = person.get("skills").arraylist();
      }
    }
}
```

Serialization is easy. The `parse` method returns a `JsonElement` which allows serializing itself to some data model
through the `to_object` method.

_data.json_
```json
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
        var json = Files.readString(Paths.get("data.json"));
        var order = (Order)JsonParser.parse(json).to_object(Order.class);
        if (order.success())
            order.print_details();
    }
}
```
