package cz.vse.java.checkers.common;

import java.util.*;

public class Message {
    private final String ID;
    private final String token;
    private final String content;

    public Message(String message){
        ArrayList<String> parts = new ArrayList<>(List.of(message.split(" ")));
        token = parts.getFirst();
        parts.removeFirst();
        ID = parts.getFirst();
        parts.removeFirst();
        content = parts.toString();


    }

    public String getToken() {
        return token;
    }

    public String getContent() {
        return content;
    }

    public String getID(){
        return ID;
    }

}
