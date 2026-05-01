import java.util.ArrayList;
import java.util.List;
public class Player {
    private String name;
    private int money;
    private int position;
    private boolean inJail;
    private List<Property> properties;
    private boolean bankrupt;
    public Player(String name, int startingMoney) {
        this.name = name;
        this.money = startingMoney;
        this.position = 0;
        this.inJail = false;
        this.properties = new ArrayList<>();
        this.bankrupt = false;
    }
    public void move(int steps, int boardSize) {
        int newPos = (position + steps) % boardSize;
        // Passed GO
        if (newPos < position) {
            addMoney(200);
            System.out.println(player.getName() + " passed GO! Collect $200");
        }
        position = newPos;
    }
    public void addMoney(int amount) { money += amount; }
    public void subtractMoney(int amount) {
        money -= amount;
        if (money <= 0) {
            money = 0;
            bankrupt = true;
            System.out.println(name + " is bankrupt!");
        }
    }
    public void addProperty(Property p) { properties.add(p); }
    public String getName() { return name; }
    public int getMoney() { return money; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    public boolean isInJail() { return inJail; }
    public void setInJail(boolean inJail) { this.inJail = inJail; }
    public boolean isBankrupt() { return bankrupt; }
    public List<Property> getProperties() { return properties; }
    @Override
    public String toString() {
        return name + " | Money: $" + money + " | Position: " + position;
    }
}
