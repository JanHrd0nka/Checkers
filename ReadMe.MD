# Checkers

A multiplayer Checkers game implemented using a client-server architecture in Java. The server manages game sessions and communication between players, while clients connect to the server to play against each other over a network.

## Requirements

* Java JDK 22
* Maven

## Building the Project

Run the following command in the project's root directory:

mvn clean package

After a successful build, the following files will be created:

* server/target/server-1.0-SNAPSHOT.jar
* client/target/client-1.0-SNAPSHOT.jar

## Running the Server

In PowerShell, run:

& "C:\Program Files\Java\jdk-22\bin\java.exe" -jar server/target/server-1.0-SNAPSHOT.jar

## Running the Client

Open a new PowerShell window and run:

& "C:\Program Files\Java\jdk-22\bin\java.exe" -jar client/target/client-1.0-SNAPSHOT.jar

## Notes

The server should be started before launching the client. Once the server is running, the client can connect and join the game.
