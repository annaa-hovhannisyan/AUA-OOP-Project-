import java.util.List;

public class JailTile extends Tile {
    public static final int JAIL_POSITION = 20;

    public JailTile(String name, int position) {
        super(name, position);
    }

    @Override
    public void landOn(Player player) {
        System.out.println(player.getName() + " is in Jail.");
    }

    /**
     * Called by Game when it's a jailed player's turn.
     *
     * Rules:
     *  - A jailed player skips 2 turns; they are released on their 3rd turn.
     *  - If ALL 4 active (non-bankrupt) players are in jail at the same time,
     *    everyone is released automatically without skipping a turn.
     *  - The old "2 players in jail → everyone free" rule has been removed.
     */
    public void handleJailTurn(Player player, List<Player> allPlayers) {
        if (!player.isInJail())
            return;

        long activeInJail = allPlayers.stream()
                .filter(p -> !p.isBankrupt() && p.isInJail())
                .count();
        long activeTotal = allPlayers.stream()
                .filter(p -> !p.isBankrupt())
                .count();

        if (activeInJail == activeTotal && activeTotal == 4) {
            System.out.println("All 4 players are in Jail! Everyone is released.");
            allPlayers.stream()
                    .filter(Player::isInJail)
                    .forEach(Player::releaseFromJail);
        } else {
            player.incrementJailTurns();
            System.out.println(player.getName() + " is in Jail and misses this turn. ("
                    + player.getJailTurns() + "/2 skipped)");
            if (player.getJailTurns() >= 2) {
                System.out.println(player.getName() + " is released from Jail after 2 skipped turns.");
                player.releaseFromJail();
            }
        }
    }
}
