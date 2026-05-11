public class FreeTile extends Tile {

    public FreeTile(String name, int position) {
        super(name, position);
    }

    @Override
    public void landOn(Player player) {
        System.out.println(player.getName() + " landed on " + getName() + ". Enjoy the free rest!");
        // No effect on the player — this is a safe resting spot.
    }
}