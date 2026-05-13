/**
 * Controls the main Monopoly game logic.
 * Handles turns, movement, and game rules.
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner; 
public class Game {
    private Board board;
    private Dice dice;
    private List<Player> players;
    private int currentPlayerIndex;
    private Scanner scanner;
    private Bank bank; 
    public Game() {
        bank = new Bank();
        board = new Board(bank);
        dice = new Dice();
        players = new ArrayList<>();
        currentPlayerIndex = 0;
        scanner = new Scanner(System.in);
    }
    public void setup() {
        System.out.print("Enter number of players (2-4): ");
        int numPlayers = Integer.parseInt(scanner.nextLine().trim());
        for (int i = 0; i < numPlayers; i++) {
            System.out.print("Enter name for Player " + (i + 1) + ": ");
            String name = scanner.nextLine().trim();
            players.add(new Player(name, 1500));
        }
        System.out.println("\nAll players start with $1500. Let's play!\n");
    }

    /**
     * Starts the game loop.
     */
    public void play() {
        System.out.println("Game started!");
        while (!isGameOver()) {
            Player current = players.get(currentPlayerIndex);
            takeTurn(current);
            // Remove bankrupt players AFTER the turn
            players.removeIf(Player::isBankrupt);
            if (!isGameOver()) {
                currentPlayerIndex = currentPlayerIndex % players.size();
                currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            }
        }
        announceWinner();
    }

    /**
     * Switches to the next player's turn.
     */
    private void takeTurn(Player player) {
        System.out.println("\n--- " + player.getName() + "'s turn ---");
        System.out.println(player);
        if (player.isInJail()) {
            JailTile jailTile = (JailTile) board.getTile(JailTile.JAIL_POSITION);
            jailTile.handleJailTurn(player, players);
            if (player.isInJail()) {
                return; // still in jail, skip turn
            }
        } 
        System.out.println("Press ENTER to roll dice...");
        scanner.nextLine();
        int steps = dice.roll();
        int oldPosition = player.getPosition();
        player.move(steps, board.getSize());
        int newPosition = player.getPosition();
        if (newPosition < oldPosition && newPosition != 0) {
            bank.payGoSalary(player);
        }
        Tile currentTile = board.getTile(newPosition);
        System.out.println("Landed on: " + currentTile.getName() + " (position " + newPosition + ")");
        currentTile.landOn(player);
    } 
    private boolean isGameOver() {
        return players.size() <= 1;
    }
    private void announceWinner() {
        if (!players.isEmpty()) {
            System.out.println("\n🏆 " + players.get(0).getName() + " wins!"
                    + " Final balance: $" + players.get(0).getMoney());
            System.out.println("Bank remaining funds: $" + bank.getFunds());
        } else {
            System.out.println("Everyone went bankrupt — no winner!");
        }
    }
    public Bank getBank() { 
        return bank; 
    }
}
