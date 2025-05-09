# BYOPL 24 -- Lox Truffle Implementation 


[![Build Status Github](https://github.com/hpi-swa-teaching/byopl24-00a/actions/workflows/maven.yml/badge.svg)](https://github.com/hpi-swa-teaching/byopl24-00a/actions?query=maven%3ACI)


The Lox implementation is developed as part of the Build Your Own Programming Language course at Software Architecture Group, Hasso Plattner Institute, Potsdam. 

## Getting Started 

For development, you can use Oracle JDK 21, OpenJDK 21, or any of its derivatives.
For best performance use GraalVM for JDK 24 version 24.0.1. 

## Maven

To directly use the command line, this might be helpful:


### Compile ...

```bash
./mvnw package
```

### Run Tests

```bash
./mvnw test
```


### Running the main class

```bash
./mvnw exec:java -Dexec.args="-c 'print true;'"
```

### Cleanup

```bash
./mvnw clean
```
