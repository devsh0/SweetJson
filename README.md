A sweet little JSON serializer/deserializer ideal for personal projects.

### State of this project
- Quick and dirty implementation. Don't expect 100% spec compliance or off-the-charts performance.
- Development in progress.
- Serialization not supported yet.

**Note:** None of the methods described below are thread safe. Concurrency must be manually handled.

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
        var json = JsonParser.parse(Paths.get("data.json")).as_map();
        for (JsonValue value : json.get("people").as_list()) {
            var person = value.as_map();
            System.out.println(person.get("firstname").as_string());
            System.out.println(person.get("lastname").as_string());
            System.out.println(person.get("skills").as_list().get(0));
        }
    }
}
```

#### Data Binding
The `parse` method returns a `JsonValue` which can be mapped to a data model by calling the `bind_to` method on
it. Partial bindings are also supported.

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

    public String get_firstname () { return firstname; }
    // ...
}

public class Main {
    public static void main (String[] args) throws IOException {
        var json = Files.readString(Paths.get("data.json"));
        var user = JsonParser.parse(json).bind_to(User.class);
        System.out.println(user.get_firstname()); // prints "John"
    }
}
```

Primitive types as well as parameterized and array types are supported. Note that custom annotations aren't
required (will be added in the future, hopefully soon). The binder will write values to field whose name
corresponds to a field in the JSON string. Furthermore, the writer will leave out transient and inherited fields.
JSON fields that are `null` are by default ignored and there is no way to change that behavior as of now. Extra
fields in the JSON string are skipped if there are no members corresponding to that key.

#### Custom Binders
We can specify custom binders to handle mapping to objects of types that do not conform to the structure of JSON
data:

```java
public class Main {
    public static void main (String[] args) throws IOException {
        var json = Files.readString(Paths.get("temp.json"));
        
        // Register a binder for `java.util.List`.
        // `element` is the JSON element that we are dealing with.
        // `typedef` describes the type of the variable to which the element will be bound.
        // `bag` contains data that maybe useful to the binder (such as hints for implementation
        // to be used as the model for this binding).
        SweetJson.register_binder(Typedef.wrap(List.class), (element, typedef, bag) -> {
            var model = new ArrayList<>();
            var arg = definition.first_type_arg();
            var list = element.as_list();
            list.forEach(entry -> model.add(entry.bind_to(arg)));
            return model;
        });
        
        var user = JsonParser.parse(json).bind_to(User.class);
        // Assuming `User` has a `List` named `skills`...
        System.out.println(user.skills.get(0));
    }
}
```
Here we are hardcoding `ArrayList` as the container. If that's not acceptable, you can insert data into the `bag` and
inspect it before creating a collection instance. The data stored in the `bag` is guaranteed to persist throughout the
lifetime of a binding request.

### Binding to generic types

SweetJson provides basic deserialization support for generic types. Here's a (bad) example:

```json
{
  "user": {
    "name": "John Doe",
    "alias": "doejohn",
    "email": "johndoe@example.com"
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
    // ...more fields

    public T get_user() {
        return user;
    }
}

@SuppressWarnings("unchecked")
public class Main {
    public static void main (String[] args) {
        var server_element = JsonParser.parse(Paths.get("server_response.json"));
        // bind_to_generic(Prototype.class, Typearg1.class...TypeargN.class)
        Server<Employee> server = server_element.bind_to_generic(Server.class, Employee.class);
        server.get_user().print_alias(); // Prints "doejohn"
    }
}
```

Nested parameterized types, such as `List<MyParameterizedType<String>>` are not supported yet.

**Note:** Documentation is updated from time to time as the development proceeds. But the README usually won't
reflect the latest API changes.