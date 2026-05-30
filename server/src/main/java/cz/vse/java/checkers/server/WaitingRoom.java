package cz.vse.java.checkers.server;

import java.util.HashMap;
import java.util.Map;

public class WaitingRoom
{
    private final Map<String, Player> name2player = new HashMap<>();
    private final Map<Player, String> player2name = new HashMap<>();

    private final Map<String, Player> allPlayers = new HashMap<>();


    public synchronized Player addPlayer(String name, ClientHandler clientHandler){
        Player result = null;
//        if (!name2player.containsKey(name)) {
//            result = new Player(clientHandler);
//            name2player.put(name, result);
//            player2name.put(result, name);
//        }
            if (!allPlayers.containsKey(name)) {
                result = new Player(clientHandler);
                allPlayers.put(name, result);
                result.setName(name);
                result.setInWaitingRoom(true);
            }
        return result;
    }
//    public synchronized Player getPlayer(String name){
//        return name2player.get(name);
//    }

    public synchronized Player getPlayer(String name) {
        return allPlayers.get(name);
    }


//    public synchronized String getName(Player player){
//        return player2name.get(player);
//    }

    public synchronized String getName(Player player) {
        for (Map.Entry<String, Player> entry : allPlayers.entrySet()) {
            if (entry.getValue() == player) {
                return entry.getKey();
            }
        }
        return null;
    }

//    public synchronized boolean renamePlayer(Player player, String newName){
//        boolean result = !name2player.containsKey(newName);
//        if (result){
//            String originalName = player2name.remove(player);
//            name2player.remove(originalName);
//            name2player.put(newName, player);
//            player2name.put(player, newName);
//        }
//        return result;
//    }
//    public synchronized String getPlayerNames() {
//        StringBuilder result = new StringBuilder();
//        for (var name : name2player.keySet()) {
//            result.append(name).append(" ");
//        }
//        return result.toString().trim();
//    }
//    public synchronized void removePlayer(Player player){
//        String name = player2name.get(player);
//        player2name.remove(player);
//        name2player.remove(name);
//    }

    public synchronized boolean renamePlayer(Player player, String newName) {
        // Zkontrolujeme, že nové jméno není obsazeno
        if (allPlayers.containsKey(newName)) {
            return false;
        }

        // Najdeme staré jméno hráče
        String oldName = null;
        for (Map.Entry<String, Player> entry : allPlayers.entrySet()) {
            if (entry.getValue() == player) {
                oldName = entry.getKey();
                break;
            }
        }

        if (oldName != null) {
            allPlayers.remove(oldName);
        }

        allPlayers.put(newName, player);
        player.setName(newName);
        return true;
    }

    public synchronized void setPlayerInWaitingRoom(Player player, boolean inWaitingRoom) {
        player.setInWaitingRoom(inWaitingRoom);
    }

    public synchronized String getPlayerNames() {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, Player> entry : allPlayers.entrySet()) {
            if (entry.getValue().isInWaitingRoom()) {  // Jen hráči v waiting room
                result.append(entry.getKey()).append(" ");
            }
        }
        return result.toString().trim();
    }

    public synchronized void removePlayer(Player player) {
        // NE - už nesmažeme hráče, jen je označíme jako mimo waiting room
        player.setInWaitingRoom(false);
    }

    public synchronized void disconnectPlayer(Player player){
        for(Map.Entry<String, Player> p : allPlayers.entrySet()){
            p.getValue().setScoreAgainst(player, 0);
        }
        allPlayers.remove(player.getName());
    }





}
