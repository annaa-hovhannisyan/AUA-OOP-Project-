/**
 * Represents a metro station tile.
 * Players can buy it and collect rent.
 */

public class MetroStationTile extends Tile {
    private static final int BASE_RENT = 25;
    private final String color;
    private Player owner;
    private final int price = 200;

    public MetroStationTile(String name, int position, String color) {
        super(name, position);
        this.color = color;
        this.owner = null;
    }

    @Override
    public void landOn(Player player) {
        System.out.println(player.getName() + " landed on Metro Station: " + getName() + "!");
        if (owner == null) {
            System.out.println("This station is unowned. Price: $" + price);
            java.util.Scanner sc = new java.util.Scanner(System.in);
            System.out.println("Do you want to buy it? (yes/no)");
            if (sc.nextLine().equalsIgnoreCase("yes")) {
                if (player.getMoney() >= price) {
                    player.subtractMoney(price);
                    this.owner = player;
                    System.out.println(player.getName() + " bought " + getName() + "!");
                } else {
                    System.out.println("Not enough money!");
                }
            }
        } else if (!owner.equals(player)) {
            int rent = BASE_RENT;
            System.out.println(player.getName() + " pays $" + rent + " to " + owner.getName() + ".");
            player.subtractMoney(rent);
            owner.addMoney(rent);
        } else {
            System.out.println(player.getName() + " owns this station.");
        }
    }

    public Player getOwner() { return owner; }

    public void setOwner(Player player) { this.owner = player; }

    public String getColor() { return color; }
}
