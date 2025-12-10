# copilot-workshop-01-01

A minimal Spring Boot application with Java 21 and Maven.

## Prerequisites
- Java 21
- Maven 3.6+

## Build and Run

```bash
mvn clean install
mvn spring-boot:run
```

## Test the Application

Once running, visit: http://localhost:8080/api/hello

## Project Structure

```
src/
├── main/
│   ├── java/com/example/workshop/
│   │   ├── Application.java
│   │   └── controller/
│   │       └── HelloController.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/example/workshop/
        └── ApplicationTests.java
```
