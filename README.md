# NodelistJ

NodelistJ is a Java library for parsing and managing Fidonet nodelists. It provides functionality to read nodelists
from files or input streams, index them in memory, and retrieve specific entries based on their addresses.

## Features

- Parse Fidonet nodelists from files or input streams.
- Index nodelists in memory for quick access.
- Retrieve nodelist entries by their addresses.
- Support for modern Java features and best practices.

## Getting Started

### Prerequisites

- Java 21
- Maven or Gradle for dependency management

### Installation

Add the following dependency to your `pom.xml` if you are using Maven:

```xml

<dependency>
    <groupId>ru.gavrilovegor519</groupId>
    <artifactId>nodelistj</artifactId>
    <version>1.0.0</version>
</dependency>
```

Or add the following dependency to your `build.gradle` if you are using Gradle:

```groovy
implementation 'ru.gavrilovegor519:nodelistj:1.0.0'
```

### Usage

#### Reading a Nodelist from a File

```java
import ru.gavrilovegor519.nodelistj.Nodelist;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        Path path = Paths.get("path/to/nodelist.txt");
        Nodelist nodelist = new Nodelist(path);
        System.out.println(nodelist.getNodelist());
    }
}
```

#### Reading a Nodelist from an Input Stream

```java
import ru.gavrilovegor519.nodelistj.Nodelist;

import java.io.InputStream;

public class Main {
    public static void main(String[] args) {
        InputStream inputStream = getClass().getResourceAsStream("/nodelist.txt");
        Nodelist nodelist = new Nodelist(inputStream);
        System.out.println(nodelist.getNodelist());
    }
}
```

#### Retrieving Nodelist Entries by Address

```java
import ru.gavrilovegor519.nodelistj.NodelistMap;

public class Main {
    public static void main(String[] args) {
        NodelistMap nodelistMap = new NodelistMap();
        String address = "1:247/1";
        NodelistEntryMap entryMap = nodelistMap.getNodelistEntryMap(address);
        System.out.println(entryMap);
    }
}
```

## Contributing

Contributions are welcome! Please open an issue or submit a pull request with your changes.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.