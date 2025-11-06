# Synthetic Data Generator

A powerful and flexible Java library for generating synthetic data based on dynamic templates.

Developed by: **Siddharth Mishra** <connectwithsiddharthm@gmail.com>

---

## 🚀 Quick Start

Getting started is as simple as providing a template string with placeholders. The engine will do the rest.

**1. Add the Maven Dependency:**

```xml
<dependency>
  <groupId>com.syntheticdata</groupId>
  <artifactId>synthetic-data-generator</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

**2. Create Your First Template:**

```java
public class QuickStart {
    public static void main(String[] args) {
        String template = """
            {
              "transactionId": "TXN-{{RANDSTR(8)}}",
              "user": {
                "name": "{{faker.name().fullName()}}",
                "email": "{{faker.internet().emailAddress()}}"
              },
              "eventTimestamp": "{{T-1}}",
              "amount": {{RANGE(50-250)}}
            }
        """;

        String syntheticEvent = com.syntheticdata.engine.SyntheticDataEngine.generateData(template);
        System.out.println(syntheticEvent);
    }
}
```

**3. Run and See the Magic!**

*Example Output:*
```json
{
  "transactionId": "TXN-aB3xZ9Pq",
  "user": {
    "name": "John Doe",
    "email": "john.doe@example.com"
  },
  "eventTimestamp": "2025-10-30T10:05:15.123456",
  "amount": 173
}
```

---

## 🏃‍♀️ Running the Application

This project can be run directly from the command line as a standalone application. The output behavior is controlled by a configuration file.

### How to Run

1.  **Configure the Application:** Create a `config.yaml` or `config.properties` file in the `src/main/resources` directory.
2.  **Execute via Maven:** Run the following command from the project root:
    ```bash
    mvn clean install exec:java
    ```
3.  **Check the Output:** Your generated synthetic data will be in the configured output (either files or the database).

### Configuration

The application is configured via a `config.yaml` or `config.properties` file in `src/main/resources`.

**Example `config.yaml`:**
```yaml
outputMode: file
# outputMode: database

database:
  url: jdbc:postgresql://localhost:5432/mydatabase
  user: myuser
  password: mypassword
  table: mytable
  column: mycolumn
```

**Example `config.properties`:**
```properties
outputMode=file
# outputMode=database

database.url=jdbc:postgresql://localhost:5432/mydatabase
database.user=myuser
database.password=mypassword
database.table=mytable
database.column=mycolumn
```

---

## 📖 How to Use as a Library

The `SyntheticDataEngine` is the main entry point for using this project as a library. It supports several modes of operation.

### 1. Direct String Processing

This is the most straightforward way to use the engine. Pass a string containing placeholders to the `generateData` method.

```java
String template = "{\"id\": \"{{UUID()}}\"}";
String result = SyntheticDataEngine.generateData(template);
```

### 2. Generating Multiple Records

You can generate a list of synthetic records from a single template by providing a `count`.

```java
String template = "{\"id\": \"{{UUID()}}\"}";
List<String> results = SyntheticDataEngine.generateData(template, 5);
// results will contain 5 unique JSON strings
```

### 3. File-Based Processing

The engine can process `.json` files directly from the filesystem. It supports two modes, which are detected automatically.

#### a) Direct Placeholder Processing

If your `.json` file contains `{{...}}` placeholders, the engine will read the file and resolve them directly.

**Example `event.json`:**
```json
{
  "eventId": "{{UUID()}}",
  "source": "direct-file"
}
```

**Code:**
```java
SyntheticDataEngine engine = new SyntheticDataEngine();
String result = engine.generateFromFile("path/to/event.json");
// To generate multiple records from the file:
List<String> results = engine.generateFromFile("path/to/event.json", 5);
```

#### b) JSONPath-Based Processing

For more complex scenarios, you can use a clean JSON template and a separate configuration file to define the transformations.

**Example `event.json`:**
```json
{
  "eventId": "",
  "user": {
    "name": "",
    "email": ""
  }
}
```

**Example `event_config.yaml`:**
```yaml
"$.eventId": "{{UUID()}}"
"$.user.name": "{{faker.name().fullName()}}"
"$.user.email": "{{faker.internet().emailAddress()}}"
```
*(You can also use a `_config.properties` file)*

The engine will automatically detect the presence of the `_config.yaml` or `_config.properties` file and apply the transformations.

**Code:**
```java
SyntheticDataEngine engine = new SyntheticDataEngine();
String result = engine.generateFromFile("path/to/event.json");
// To generate multiple records from the file:
List<String> results = engine.generateFromFile("path/to/event.json", 5);
```

### 4. Directory Processing

The engine can process an entire directory of `.json` files, automatically detecting the correct processing mode for each file and generating a specified number of records for each.

```java
SyntheticDataEngine engine = new SyntheticDataEngine();
// Generate 1 record per file
List<String> results = engine.generateFromDirectory("path/to/my-events");
// Generate 5 records per file
List<String> multiResults = engine.generateFromDirectory("path/to/my-events", 5);
```

---

## ✨ Features

The library supports a wide range of placeholders, which can be mixed and matched to generate complex and realistic data.

### 1. Faker-Based Data Generation

Leverage the power of the popular **DataFaker** library to generate a massive variety of realistic data.

-   **Syntax:** `{{faker.category().method()}}`

-   **Examples:**
    -   `{{faker.name().fullName()}}` -> "Jane Smith"
    -   `{{faker.internet().emailAddress()}}` -> "jane.smith@example.com"
    -   `{{faker.address().city()}}` -> "New York"
    -   `{{faker.finance().iban()}}` -> "DE89370400440532013000"
    -   `{{faker.company().name()}}` -> "Tech Solutions Inc."

### 2. Plugin-Based Functions

Use built-in or custom functions for common data generation needs.

-   **UUIDs:** Generate random universally unique identifiers.
    -   **Syntax:** `{{UUID()}}`
    -   **Example Output:** "a1b2c3d4-e5f6-7890-1234-567890abcdef"

-   **Random Alphanumeric Strings:**
    -   **Syntax:** `{{RANDSTR(length)}}`
    -   **Example:** `{{RANDSTR(8)}}` -> "aB3xZ9Pq"
    -   *(Default length is 8 if not specified)*

-   **Random Alphabetic Strings:**
    -   **Syntax:** `{{ALPHA(length)}}`
    -   **Example:** `{{ALPHA(5)}}` -> "XyZaB"
    -   *(Default length is 6 if not specified)*

### 3. Numeric Ranges

Generate random integers within a specified inclusive range.

-   **Syntax:** `{{RANGE(min-max)}}`
-   **Examples:**
    -   `{{RANGE(1-100)}}` -> A number between 1 and 100.
    -   `{{RANGE(1000-9999)}}` -> A 4-digit number.

### 4. Date and Time Expressions

Generate timestamps relative to the current date and time.

-   **Day-based Offsets:**
    -   **Syntax:** `{{T+days}}` or `{{T-days}}`
    -   **Example:** `{{T+3}}` -> The date 3 days from now.
    -   **Example:** `{{T-1}}` -> Yesterday's date.

-   **Hour-based Offsets:**
    -   **Syntax:** `{{t+hours}}` or `{{t-hours}}`
    -   **Example:** `{{t+5}}` -> The time 5 hours from now.

### 5. Custom Pattern Generation

Create custom formatted strings with random digits and letters.

-   **`#` for Random Digits (0-9):**
    -   **Example:** `ORDER-####` -> "ORDER-1234"

-   **`$` for Random Uppercase Letters (A-Z):**
    -   **Example:** `INV-$$$` -> "INV-XYZ"

### 6. Chained and Nested Expressions

The true power of the library comes from its ability to combine any of the above features in a single placeholder.

-   **Examples:**
    -   `{{faker.name().lastName()}}_{{RANGE(1-10)}}` -> "Smith_7"
    -   `TRAN-{{RANGE(1000-9999)}}-##` -> "TRAN-5432-89"
    -   `ID-{{ALPHA(3)}}-{{RANDSTR(5)}}` -> "ID-ABC-a1B2c"

---

## 🧩 Extending the Engine with Custom Plugins

You can easily create and register your own function-style plugins.

**1. Implement the `FunctionPlugin` Interface:**

```java
package com.mycompany.plugins;

import com.syntheticdata.expression.plugins.FunctionPlugin;

public class MyCustomPlugin implements FunctionPlugin {
    @Override
    public String name() {
        return "MY_PLUGIN";
    }

    @Override
    public String execute(String[] args) {
        // Your custom logic here
        return "Hello, " + String.join(" ", args);
    }
}
```

**2. Register and Use Your Plugin:**

The `SyntheticDataEngine` does not yet support dynamic plugin registration. This is a planned future enhancement.

---

## 👨‍💻 Developer

-   **Siddharth Mishra**
-   **Email:** <connectwithsiddharthm@gmail.com>
