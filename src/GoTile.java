/**
 * Represents the GO tile.
 * Gives money to players passing or landing on it.
 */

public class GoTile extends Tile {
    public GoTile() {
        super("GO", 0);
    }

    /**
     * Gives the GO reward to the player.
     * @param player current player
     */
    @Override
    public void landOn(Player player) {
        player.addMoney(200);
        System.out.println(player.getName() + " landed on GO! Collect $200.");
    }
}
