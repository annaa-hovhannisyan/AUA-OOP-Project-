/**
 * Holds all mutable game state and notifies the UI on changes.
 * Acts as the model layer between Java game logic and Swing views.
 */

import java.util.*;
import java.util.function.Consumer;

public class GameState {

    public enum Phase { ROLL, BUY, GAME_OVER }

    List<Player> players = new ArrayList<>();
    List<Tile> tiles = new ArrayList<>();
    int currentIdx = 0;
    int[] dice = {0, 0};
    Phase phase = Phase.ROLL;
    Tile pendingBuy = null;
    boolean lastDoubles = false;
    int bankFunds = Bank.INITIAL_FUNDS;
    final List<String> log = new ArrayList<>();
    boolean gameOver = false;
    String winnerName = null;

    private final List<Runnable> listeners = new ArrayList<>();

    public void addChangeListener(Runnable r) { listeners.add(r); }

    public void fireChange() {
        for (Runnable r : listeners) javax.swing.SwingUtilities.invokeLater(r);
    }

    public Player currentPlayer() { return players.isEmpty() ? null : players.get(currentIdx); }
    public String getCurrentPlayerName() {
        Player p = currentPlayer();
        return p == null ? "" : p.getName();
    }
    public int[] getDice() { return dice; }
    public boolean isGameOver() { return gameOver; }
    public Phase getPhase() { return phase; }
    public Tile getPendingBuy() { return pendingBuy; }
    public List<String> getLog() { return Collections.unmodifiableList(log); }
    public List<Player> getPlayers() { return Collections.unmodifiableList(players); }
    public List<Tile> getTiles() { return tiles; }
    public int getBankFunds() { return bankFunds; }
    public String getWinnerName() { return winnerName; }

    public void addLog(String msg) {
        log.add(0, msg);
        if (log.size() > 60) log.remove(log.size() - 1);
    }
}
