import java.util.Random;

public class ChanceTile extends Tile {

    private static final int[][] CARDS = {
            { 150, 1},  // Bank pays dividend
            { 100, 1},  // Your building loan matures
            {-100, -1}, // Pay poor tax
            {-50,  -1}, // Speeding fine
            { 200, 1},  // Advance to GO (simplified: just collect $200)
            {-150, -1}, // Go to jail (simplified: fine)
    };

    public ChanceTile(String name, int position) {
        super(name, position);
    }

    // This constructor variant matches: new ChanceTile("Chance", 7, 0)
    // The third argument (e.g. 0) can serve as a card-set selector (unused here but accepted)
    public ChanceTile(String name, int position, int cardSet) {
        super(name, position);
    }

    @Override
    public void landOn(Player player) {
        System.out.println(player.getName() + " landed on Chance!");
        Random rand = new Random();
        int[] card = CARDS[rand.nextInt(CARDS.length)];
        int amount = card[0];
        if (amount > 0) {
            player.addMoney(amount);
            System.out.println("Chance: Collect $" + amount + "!");
        } else {
            player.subtractMoney(-amount);
            System.out.println("Chance: Pay $" + (-amount) + ".");
        }
    }
}