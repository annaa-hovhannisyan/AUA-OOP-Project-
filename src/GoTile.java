public class GoTile extends Tile {
    public GoTile() {
        super("GO", 0);
    }
    @Override
    public void landOn(Player player) {
        player.addMoney(200);
        System.out.println(player.getName() + " landed on GO! Collect $200.");
    }
}
