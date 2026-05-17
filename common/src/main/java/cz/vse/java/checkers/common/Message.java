package cz.vse.java.checkers.common;

import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

public class Message {
    private String ID;
    private String token;
    private String content;

    public Message(String message){
        ArrayList<String> parts = (ArrayList<String>) Arrays.asList(message.split(" "));
        ID = parts.getFirst();
        parts.removeFirst();
        token = parts.getFirst();
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
