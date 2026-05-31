# Checkers

## Game Name

Checkers

## Team Members

* Hrdonka Jan
* Filinger Adam

## Project Description

Checkers is a multiplayer board game implemented in Java using a client-server architecture. The server manages game sessions and communication between connected players, while clients provide the graphical user interface and allow players to participate in games over a network.

## Requirements

* Java JDK 22
* Maven

## Compilation and Build

Run the following command in the project's root directory:

```bash
mvn clean package
```

After a successful build, the following files will be created:

* `server/target/server-1.0-SNAPSHOT.jar`
* `client/target/client-1.0-SNAPSHOT.jar`

## Running the Application

### Start the Server

```powershell
& "C:\Program Files\Java\jdk-22\bin\java.exe" -jar server/target/server-1.0-SNAPSHOT.jar
```

### Start the Client

* The client configuration file is located at:

  client/src/main/resources/META-INF/config.xml

  During build, it is packaged into the JAR and loaded at runtime via the Java classpath using getResourceAsStream().

Open a new PowerShell window and run:

```powershell
& "C:\Program Files\Java\jdk-22\bin\java.exe" -jar client/target/client-1.0-SNAPSHOT.jar
```

The server should be started before launching any clients.

## Known Issues

* No known issues.

## Deviations from the Original Assignment

* An additional message ID is included in all messages exchanged between the server and clients.
* Game history is not preserved or displayed on the client side.
* The rematch functionality returns players to the waiting room instead of starting a new game directly. A new match is then offered automatically.
* Client disconnection is interpreted as a resignation and results in an immediate loss.
* The server supports multi-jump moves (capturing multiple opponent pieces in a single turn). However, the current client implementation is unable to correctly send such move requests.