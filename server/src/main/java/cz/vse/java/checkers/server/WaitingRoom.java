package cz.vse.java.checkers.server;

import java.util.HashMap;
import java.util.Map;

public class WaitingRoom
{
    private final Map<String, Player> name2player = new HashMap<>();
    private final Map<Player, String> player2name = new HashMap<>();

    public synchronized Player addPlayer(String name, ClientHandler clientHandler){
        Player result = null;
        if (!name2player.containsKey(name)) {
            result = new Player(clientHandler);
            name2player.put(name, result);
            player2name.put(result, name);
        }
        return result;
    }
    public synchronized Player getPlayer(String name){
        return name2player.get(name);
    }

    public synchronized String getName(Player player){
        return player2name.get(player);
    }
    public synchronized boolean renamePlayer(Player player, String newName){
        boolean result = !name2player.containsKey(newName);
        if (result){
            String originalName = player2name.remove(player);
            name2player.remove(originalName);
            name2player.put(newName, player);
            player2name.put(player, newName);
        }
        return result;
    }
    public synchronized String getPlayerNames() {
        StringBuilder result = new StringBuilder();
        for (var name : name2player.keySet()) {
            result.append(name).append(" ");
        }
        return result.toString().trim();
    }
    public synchronized void removePlayer(Player player){
        String name = player2name.get(player);
        player2name.remove(player);
        name2player.remove(name);
    }
}
