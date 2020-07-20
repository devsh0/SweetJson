### Features
- Super naive.
- Only 95% compliant with the specs.
- Serialization/Deserialization not (yet) supported.
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
      for (Element json_element : json.get("people").arryalist()) {
          var person = json_element.map();
          var firstname = person.get("firstname").string();
          var lastname = person.get("lastname").string();
          var age = person.get("age").number();
          var skills = person.get("skills").arraylist();
      }
    }
}
```