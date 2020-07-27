A sweet little JSON serializer/deserializer ideal for personal projects.

### State of this project
- Development in progress.
- Not fully compatible with the specs.
- Serialization not supported yet.

**Note:** None of the methods described below are thread safe. Concurrency must be manually managed.

### Usage
_data.json_
```json
{
  "people": [
    {
      "firstname": "John",
      "lastname": "Doe",
      "skills": ["java", "c", "c++", "sql"]
    },
    {
      "firstname": "Jane",
      "lastname": "Doe",
      "skills": ["javascript", "python", "rust"]
    }
  ]
}
```

_Main.java_
```java
public class Main {
    public static void main(String[] args) {
        // Get data as map.
        var json = JsonParser.parse(Paths.get("data.json")).as_map();
        for (JsonElement element : json.get("people").as_list()) {
            var person = element.as_map();
            System.out.println(person.get("firstname").as_string());
            System.out.println(person.get("lastname").as_string());
            System.out.println(person.get("skills").as_list().size());
        }
    }
}
```

Data binding is easy. The `parse` method returns a `JsonElement` which can be mapped to a data model by calling
the `bind_to` method on `JsonElement`. Partial bindings are also supported.

_data.json_
```json
{
  "user_id": 123456,
  "firstname": "John",
  "lastname": "Doe"
}
```

_Main.java_
```java
class User {
    private int user_id;
    private String firstname;
    private String lastname;
}

public class Main {
    public static void main (String[] args) throws IOException {
        var json = Files.readString(Paths.get("data.json"));
        var user = JsonParser.parse(json).bind_to(User.class);
        System.out.println(user.firstname); // prints "John"
    }
}
```

Primitive types as well as parameterized and array types are supported. Note that custom annotations aren't
required (will be added in the future, hopefully soon). The binder will write values to fields whose name
corresponds to fields in the JSON string. Furthermore, the writer will leave out transient and inherited fields.
If a JSON field is set to `null`, the binder will throw if the corresponding field is primitive (will be changed
soon). Extra fields in the JSON string are simply skipped if there are no members corresponding to that key.

We can specify custom binders to handle mapping to objects of types that do not conform to the structure of JSON
data:

```java
public class Main {
    // Register a binder for `java.util.List`.
    private static void register_list_binder () {
        SweetJson.register_binder(Typedef.wrap(List.class), new JsonBinder() {
            @Override
            public Object construct (JsonElement element, Typedef definition, Bag bag) {
                var model = new ArrayList<>();
                var arg = definition.first_type_arg();
                var list = element.as_list();
                list.forEach(entry -> model.add(entry.bind_to(arg)));
                return model;
            }
        });
    }
    
        public static void main (String[] args) throws IOException {
            var json = Files.readString(Paths.get("data.json"));
            register_list_binder();
            var user = JsonParser.parse(json).bind_to(User.class);
            // Assuming `User` has a `List` named `skills`...
            System.out.println(user.skills.get(0));
        }
}
```
Here we are hardcoding `ArrayList` as the container. If that's not acceptable, you'll need to register more
specific types. That also means that you have to use specific types while declaring variables (so not cool).

### Generic Types

`SweetJson` provides basic support for deserializing generic types. Here's a (bad) example:

```json
{
  "user": {
    "name": "John Doe",
    "alias": "doejohn",
    "email": "johndoes@anonymous.com"
  }
}
```

```java
class Employee {
    private String name;
    private String alias;
    private String email;

    public void print_alias () {
        System.out.println(alias);
    }
}

class Server <T> {
    private T user;
    private transient Connection handle;
    // ...more fields
    
    public T get_user() {
        return user;
    }
}

public class Main {
    public static void main (String[] args) {
        // bind_to_generic(Prototype.class, Typearg1.class...TypeargN.class)
        var server = JsonParser .parse(args[0]).bind_to_generic(Server.class, Employee.class);
        System.out.println(server.get_user().print_alias();) // Prints "doejohn"
    }
}
```

**Note:** Documentation is updated from time to time as the development proceeds. But the README usually won't
reflect the latest API changes.