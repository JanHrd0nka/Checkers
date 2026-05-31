package cz.vse.java.checkers.server;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages all connected players and their presence in the waiting room.
 * <p>
 * The WaitingRoom is responsible for:
 * - storing active players by name
 * - handling player registration and renaming
 * - tracking which players are available for matchmaking
 * - providing list of available players
 * - cleaning up player state on disconnect
 * <p>
 * This class acts as a central registry for all players
 * outside of active matches.
 *
 * @author Jan Hrdonka
 * @version 1.0
 */
public class WaitingRoom {
    private final Map<String, Player> allPlayers = new HashMap<>();


    public synchronized Player addPlayer(String name, ClientHandler clientHandler) {
        Player result = null;
        if (!allPlayers.containsKey(name)) {
            result = new Player(clientHandler);
            allPlayers.put(name, result);
            result.setName(name);
            result.setInWaitingRoom(true);
        }
        return result;
    }

    public synchronized Player getPlayer(String name) {
        return allPlayers.get(name);
    }

    public synchronized String getName(Player player) {
        for (Map.Entry<String, Player> entry : allPlayers.entrySet()) {
            if (entry.getValue() == player) {
                return entry.getKey();
            }
        }
        return null;
    }


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

    public synchronized void disconnectPlayer(Player player) {
        for (Map.Entry<String, Player> p : allPlayers.entrySet()) {
            p.getValue().setScoreAgainst(player, 0);
        }
        allPlayers.remove(player.getName());
    }


}
