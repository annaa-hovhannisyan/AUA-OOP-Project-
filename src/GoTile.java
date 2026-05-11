public class GoTile extends Tile {

    public GoTile() {
        super("GO", 0);
    }

    @Override
    public void landOn(Player player) {
        // Landing directly on GO grants a bonus (passing GO is handled in Player.move())
        player.addMoney(200);
        System.out.println(player.getName() + " landed on GO! Collect $200.");
    }
}