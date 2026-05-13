package cz.vse.java.checkers.server;

import java.util.HashMap;
import java.util.Map;

public class WaitingRoom
{
    private final Map<String, Player> players = new HashMap<>();

    public boolean addPlayer(String name){
        boolean result = true;
        if (!players.containsKey(name)) {
            players.put(name, new Player());
        }
        else {
            result = false;
        }
        return result;
    }
}
