/**
 * Represents a free/rest tile.
 * No action happens when landing on it.
 */

public class FreeTile extends Tile {
    public FreeTile(String name, int position) {
        super(name, position);
    }

    /**
     * Executes the free tile action.
     * @param player current player
     */
    @Override
    public void landOn(Player player) {
        System.out.println(player.getName() + " landed on " + getName() + ". Enjoy the free rest!");
    }
}
