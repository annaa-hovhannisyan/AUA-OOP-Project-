import java.util.Random;

public class CommunityChestTile extends Tile {

    private static final int[][] CARDS = {
            { 200, 1},  // Bank error in your favor
            { 100, 1},  // Doctor's fees refund
            { 50,  1},  // Sale of stock
            {-100, -1}, // Pay school fees
            {-50,  -1}, // Doctor's fees
            { 150, 1},  // Insurance matures
    };

    public CommunityChestTile(String name, int position) {
        super(name, position);
    }

    @Override
    public void landOn(Player player) {
        System.out.println(player.getName() + " landed on Community Chest!");
        Random rand = new Random();
        int[] card = CARDS[rand.nextInt(CARDS.length)];
        int amount = card[0];
        if (amount > 0) {
            player.addMoney(amount);
            System.out.println("Community Chest: Collect $" + amount + "!");
        } else {
            player.subtractMoney(-amount);
            System.out.println("Community Chest: Pay $" + (-amount) + ".");
        }
    }
}