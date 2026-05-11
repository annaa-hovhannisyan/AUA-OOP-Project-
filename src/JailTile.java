import java.util.List;
public class JailTile extends Tile {
    public static final int JAIL_POSITION = 20;
    private java.util.List<Player> jailedPlayers = new java.util.ArrayList<>();
    public JailTile(String name, int position) {
        super(name, position);
    }
    @Override
    public void landOn(Player player) {
        System.out.println(player.getName() + " is Just Visiting Jail.");
    }
    /**
     * Called by Game when it's a jailed player's turn.
     * Rule: 1 player in jail → misses the turn.
     *       2+ players in jail together → they all get released and move on.
     */
    public void handleJailTurn(Player player, List<Player> allPlayers) {
        if (!player.isInJail()) 
            return;
        long othersInJail = allPlayers.stream()
                .filter(p -> !p.equals(player) && p.isInJail())
                .count();
        if (othersInJail >= 1) { 
            System.out.println("Two players in Jail! Everyone gets released and moves on.");
            allPlayers.stream()
                    .filter(Player::isInJail)
                    .forEach(Player::releaseFromJail);
        } else {
            player.incrementJailTurns();
            System.out.println(player.getName() + " is in Jail and misses this turn. (Turn " + player.getJailTurns() + " in jail)"); 
            if (player.getJailTurns() >= 3) {
                System.out.println(player.getName() + " is released from Jail after 3 turns.");
                player.releaseFromJail();
            }
        }
    }
}
