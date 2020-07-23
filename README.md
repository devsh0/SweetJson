### Features
- Super naive.
- Only 90% compliant with the specs.
- Serialization not supported yet.
- Written to overcome boredom.

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
    public static void main(String[] args) {
        // Get data as map.
        var json = JsonParser.parse(Paths.get("data.json")).map();

        for (JsonElement element : json.get("people").arryalist()) {
            var person = element.map();
            var firstname = person.get("firstname").string();
            var lastname = person.get("lastname").string();
            var age = person.get("age").number(); // returns double
            var skills = person.get("skills").arraylist();
        }
    }
}
```

Data binding is easy. The `parse` method returns a `JsonElement` which can be mapped to a data model by calling
the `bind_to` method on `JsonElement`.

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
        var user = (User)JsonParser.parse(json).bind_to(User.class);
        System.out.println(user.firstname); // prints "John"
    }
}
```

Primitive types as well as non-parameterized types and arrays are supported. Note that custom annotations aren't
required. The binder will write values to fields whose name corresponds to fields in the JSON string. Furthermore, 
the writer will leave out transient and inherited fields. If a JSON field is set to `null`, the binder will throw
if the corresponding field is primitive. Extra fields in the JSON string are simply skipped if there are no members
corresponding to that key.

We can specify custom binders to handle mapping to objects of types that do not conform to the structure of JSON
data:

```java
public class Main {
    // Register a binder for `java.util.List`. We can, if we want, specify type arguments
    // but as of now, that has no affect. From here on, the binder will treat all `List`s
    // the same. This limitation will go away soon.
    private static void register_list_binder () {
        SweetJson.register_binder(TypeDefinition.wrap(List.class), new JsonBinder() {
            @Override
            public Object construct (JsonElement element, TypeDefinition definition) {
                var model = new ArrayList<>();
                var arg = definition.first_type_arg();
                var list = element.arraylist();
                list.forEach(entry -> model.add(entry.bind_to(arg)));
                return model;
            }
        });
    }
    
        public static void main (String[] args) throws IOException {
            var json = Files.readString(Paths.get("data.json"));
            register_list_binder();
            var user = (User)JsonParser.parse(json).bind_to(User.class);
            // Assuming `User` has a `List` named `skills`...
            System.out.println(user.skills.get(0));
        }
}
```
Here we are hardcoding `ArrayList` as the container. If that's not acceptable, you'll need to register more
specific types. That also means that you have to use specific types while declaring variables (so not cool).
