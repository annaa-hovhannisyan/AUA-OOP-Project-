/**
 * Entry point of the Monopoly application.
 * Starts the program execution.
 */

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Welcome to Yerevan Monopoly! ===\n");
        Game game = new Game();
        game.setup();
        game.play();
    }
}
