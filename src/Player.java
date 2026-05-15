import java.util.ArrayList;
import java.util.List;
public class Player {
    private String name;
    private int money;
    private int position;
    private boolean inJail;
    private int jailTurns;          
    private List<Property> properties;
    private boolean bankrupt;
    public Player(String name, int startingMoney) {
        this.name = name;
        this.money = startingMoney;
        this.position = 0;
        this.inJail = false;
        this.jailTurns = 0;
        this.properties = new ArrayList<>();
        this.bankrupt = false;
    }
    public void move(int steps, int boardSize) {
        position = (position + steps) % boardSize;
    }
    public void addMoney(int amount) {
        money += amount;
    }
    public void subtractMoney(int amount) {
        money -= amount;
        if (money <= 0) {
            money = 0;
            bankrupt = true;
            System.out.println(name + " is bankrupt!");
        }
    }
    public void addProperty(Property p) { 
        properties.add(p); 
    }
    public void goToJail(int jailPosition) {
        inJail = true;
        jailTurns = 0;
        position = jailPosition;
        System.out.println(name + " is sent to Jail!");
    }
    public int getJailTurns() { 
        return jailTurns; 
    }
    public void incrementJailTurns() { 
        jailTurns++; 
    }
    public void releaseFromJail() {
        inJail = false;
        jailTurns = 0;
    }
    public String getName() { 
        return name; 
    }
    public int getMoney() { 
        return money; 
    }
    public int getPosition() { 
        return position; 
    }
    public void setPosition(int pos) { 
        this.position = pos; 
    }
    public boolean isInJail() { 
        return inJail; 
    }
    public void setInJail(boolean b) { 
        this.inJail = b; 
    }
    public boolean isBankrupt() { 
        return bankrupt; 
    }
    public List<Property> getProperties() { 
        return properties; 
    }
    @Override
    public String toString() {
        return name + " | Money: $" + money + " | Position: " + position + (inJail ? " [IN JAIL]" : "");
    }
}
