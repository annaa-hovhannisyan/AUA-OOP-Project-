public class Property extends Tile {
    private int price;
    private int rent;
    private Player owner;
    private String colorGroup;
    public Property(String name, int position, int price, int rent, String colorGroup) {
        super(name, position);
        this.price = price;
        this.rent = rent;
        this.owner = null;
        this.colorGroup = colorGroup;
    }
    @Override
    public void landOn(Player player) {
        if (owner == null) {
            // Ask player if they want to buy
            System.out.println(player.getName() + " landed on " + getName() + ". Price: $" + price);
            System.out.println("Do you want to buy it? (yes/no)");
            Scanner sc = new Scanner(System.in);
            if (sc.nextLine().equalsIgnoreCase("yes")) {
                if (player.getMoney() >= price) {
                    player.subtractMoney(price);
                    player.addProperty(this);
                    this.owner = player;
                    System.out.println(player.getName() + " bought " + getName());
                } else {
                    System.out.println("Not enough money!");
                }
            }
        } else if (!owner.equals(player)) {
            System.out.println(player.getName() + " pays $" + rent + " rent to " + owner.getName());
            player.subtractMoney(rent);
            owner.addMoney(rent);
        } else {
            System.out.println(player.getName() + " owns this property.");
        }
    }
    public boolean isOwned() { return owner != null; }
    public Player getOwner() { return owner; }
    public int getPrice() { return price; }
    public int getRent() { return rent; }
    public String getColorGroup() { return colorGroup; }
}
