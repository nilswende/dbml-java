# DBML Parser for the JVM

Database Markup Language (DBML), designed to define and document database structures.
See the [original repository](https://github.com/holistics/dbml).

Using Java 17.

Example usage:
```java
import com.wn.dbml.compiler.DbmlParser;
import com.wn.dbml.model.Database;

class Example {
  public static void main(String[] args) {
    var dbml = """
        Table table1 {
          id integer
        }""";
    // parse the dbml
    Database database = DbmlParser.parse(dbml);
    // process the database structure
    database.getSchemas().stream()
        .flatMap(schema -> schema.getTables().stream())
        .forEach(System.out::println); // prints "table1"
  }
}
```

Maven dependency:
```xml
<dependency>
    <groupId>io.github.nilswende</groupId>
    <artifactId>dbml-java</artifactId>
    <version>1.1.0</version>
</dependency>
```
