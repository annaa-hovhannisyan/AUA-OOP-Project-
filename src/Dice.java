import java.util.Random;
public class Dice {
    private Random random;
    private int lastRoll1;
    private int lastRoll2;
    public Dice() {
        this.random = new Random();
    }
    public int roll() {
        lastRoll1 = random.nextInt(6) + 1;
        lastRoll2 = random.nextInt(6) + 1;
        System.out.println("Rolled: " + lastRoll1 + " + " + lastRoll2 + " = " + (lastRoll1 + lastRoll2));
        return lastRoll1 + lastRoll2;
    }
    public boolean isDoubles() {
        return lastRoll1 == lastRoll2;
    }
    public int getDie1() { return lastRoll1; }
    public int getDie2() { return lastRoll2; }
}
