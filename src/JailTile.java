public class JailTile extends Tile {

    private static final int BAIL_AMOUNT = 50;

    public JailTile(String name, int position) {
        super(name, position);
    }

    @Override
    public void landOn(Player player) {
        if (player.isInJail()) {
            // Player is currently serving jail time
            System.out.println(player.getName() + " is in Jail. Pay $" + BAIL_AMOUNT + " to get out.");
            player.subtractMoney(BAIL_AMOUNT);
            player.setInJail(false);
        } else {
            // Just visiting
            System.out.println(player.getName() + " is just visiting Jail. No penalty.");
        }
    }

    // Called when a player is sent to jail (e.g. from a Chance card)
    public void sendToJail(Player player) {
        System.out.println(player.getName() + " is sent to Jail!");
        player.setPosition(getPosition());
        player.setInJail(true);
    }

    public int getBailAmount() { return BAIL_AMOUNT; }
}