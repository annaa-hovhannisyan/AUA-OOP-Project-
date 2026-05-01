import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
public class Game {
    private Board board;
    private Dice dice;
    private List<Player> players;
    private int currentPlayerIndex;
    private Scanner scanner;
    public Game() {
        board = new Board();
        dice = new Dice();
        players = new ArrayList<>();
        currentPlayerIndex = 0;
        scanner = new Scanner(System.in);
    }
    public void setup() {
        System.out.print("Enter number of players (2-4): ");
        int numPlayers = Integer.parseInt(scanner.nextLine());
        for (int i = 0; i < numPlayers; i++) {
            System.out.print("Enter name for Player " + (i + 1) + ": ");
            String name = scanner.nextLine();
            players.add(new Player(name, 1500));
        }
    }
    public void play() {
        System.out.println("Game started!");
        while (!isGameOver()) {
            takeTurn(players.get(currentPlayerIndex));
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            players.removeIf(Player::isBankrupt);
        }
        announceWinner();
    }
    private void takeTurn(Player player) {
        System.out.println("\n--- " + player.getName() + "'s turn ---");
        System.out.println(player);
        System.out.println("Press ENTER to roll dice...");
        scanner.nextLine();
        int steps = dice.roll();
        player.move(steps, board.getSize());
        Tile currentTile = board.getTile(player.getPosition());
        System.out.println("Landed on: " + currentTile.getName());
        currentTile.landOn(player);
    }
    private boolean isGameOver() {
        return players.size() == 1;
    }
    private void announceWinner() {
        if (!players.isEmpty()) {
            System.out.println("\n🏆 " + players.get(0).getName() + " wins!");
        }
    }
}
