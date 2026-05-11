public class Bank {
    private int funds;
    public static final int INITIAL_FUNDS = 20580; //standard Monopoly bank amount
    public Bank() {
        this.funds = INITIAL_FUNDS;
    }
    public void pay(Player player, int amount) {
        if (funds >= amount) {
            funds -= amount;
            player.addMoney(amount);
            System.out.println("Bank paid $" + amount + " to " + player.getName() + ". Bank remaining: $" + funds);
        } else {
            System.out.println("Bank is out of money!");
        }
    }
    public void collect(Player player, int amount) {
        player.subtractMoney(amount);
        funds += amount;
        System.out.println("Bank collected $" + amount + " from " + player.getName() + ". Bank remaining: $" + funds);
    }
    public void buyProperty(Player player, Property property) {
        if (player.getMoney() >= property.getPrice()) {
            collect(player, property.getPrice());
            property.setOwner(player);
            player.addProperty(property);
            System.out.println(player.getName() + " bought " + property.getName() + " for $" + property.getPrice());
        } else {
            System.out.println(player.getName() + " cannot afford " + property.getName());
        }
    }
    public void payGoSalary(Player player) {
        pay(player, 200);
        System.out.println(player.getName() + " collected $200 salary from GO!");
    }
    public void collectTax(Player player, int amount, String reason) {
        collect(player, amount);
        System.out.println(player.getName() + " paid $" + amount + " for " + reason);
    }
    public void collectMetroTicket(Player player) {
        collect(player, 25);
        System.out.println(player.getName() + " paid $25 metro ticket to the bank");
    }
    public void applyCard(Player player, int amount) {
        if (amount > 0) {
            pay(player, amount);
            System.out.println("Card: Bank paid $" + amount + " to " + player.getName());
        } else if (amount < 0) {
            collect(player, Math.abs(amount));
            System.out.println("Card: Bank collected $" + Math.abs(amount) + 
                " from " + player.getName());
        }
    }
    public int getFunds() {
        return funds;
    }
}
