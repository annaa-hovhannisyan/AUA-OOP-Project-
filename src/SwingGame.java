/**
 * Drives the game logic using the original Java classes (Board, Player, Dice, Bank, etc.)
 * and updates GameState so the Swing UI can reflect every change.
 */

import java.util.*;

public class SwingGame {

    private final GameState state;
    private final Board     board;
    private final Dice      dice;
    private final Bank      bank;

    // Card decks (reuse CardDeck from original code)
    private final CardDeck chanceDeck    = new CardDeck("chance");
    private final CardDeck communityDeck = new CardDeck("community");

    public SwingGame(GameState state, List<String> playerNames) {
        this.state = state;
        this.bank  = new Bank();
        this.board = new Board(bank);
        this.dice  = new Dice();

        // Build players
        for (String name : playerNames) {
            state.players.add(new Player(name, 1500));
        }

        // Mirror tiles from Board into state
        for (int i = 0; i < board.getSize(); i++) {
            state.tiles.add(board.getTile(i));
        }
        state.bankFunds = bank.getFunds();

        state.addLog("— " + state.getCurrentPlayerName() + "'s turn —");
        state.fireChange();
    }

    /** Roll dice and process the current player's turn up to any buy decision. */
    public void doRoll() {
        if (state.phase != GameState.Phase.ROLL || state.gameOver) return;
        Player p = state.currentPlayer();

        // ---- Jail handling ----
        if (p.isInJail()) {
            // If ALL 4 active players are in jail, everyone is released automatically.
            long activeInJail = state.players.stream()
                    .filter(x -> !x.isBankrupt() && x.isInJail())
                    .count();
            long activeTotal = state.players.stream()
                    .filter(x -> !x.isBankrupt())
                    .count();
            if (activeInJail == activeTotal && activeTotal == 4) {
                state.addLog("🔓 All 4 players in jail! Everyone is released.");
                state.players.forEach(x -> { if (x.isInJail()) x.releaseFromJail(); });
                // fall through to normal roll
            } else {
                // Player skips 2 turns before being released on the 3rd.
                p.incrementJailTurns();
                state.addLog("🔒 " + p.getName() + " is in jail — misses this turn. ("
                        + p.getJailTurns() + "/2)");
                if (p.getJailTurns() >= 2) {
                    p.releaseFromJail();
                    state.addLog("🔓 " + p.getName() + " released from jail after 2 skipped turns.");
                }
                nextTurn();
                refreshBank();
                state.fireChange();
                return;
            }
        }

        // ---- Roll ----
        int total    = dice.roll();
        boolean dbls = dice.isDoubles();
        state.dice   = new int[]{ dice.getDie1(), dice.getDie2() };
        state.addLog("🎲 " + p.getName() + " rolled " + dice.getDie1() + "+" + dice.getDie2()
                + "=" + total + (dbls ? " (doubles!)" : ""));

        int oldPos = p.getPosition();
        p.move(total, board.getSize());
        int newPos = p.getPosition();

        // Only pay GO salary when the player *passes* GO (wraps around),
        // not when they land exactly on GO (newPos == 0). The GoTile.landOn handler
        // already pays the salary for an exact landing.
        if (newPos < oldPos && newPos != 0) {
            bank.payGoSalary(p);
            state.addLog("⬛ " + p.getName() + " passed GO — collect $200! Balance: $" + p.getMoney());
        }

        state.lastDoubles = dbls;
        landOn(p, board.getTile(newPos));
        refreshBank();

        // Check if bank ran out of money after this turn's transactions
        checkBankEmpty();

        state.fireChange();
    }

    /** Player chooses to buy (yes) or skip (no) the pending tile. */
    public void doBuy(boolean yes) {
        if (state.phase != GameState.Phase.BUY || state.pendingBuy == null) return;
        Player p    = state.currentPlayer();
        Tile   tile = state.pendingBuy;

        if (yes) {
            if (tile instanceof Property prop) {
                bank.buyProperty(p, prop);
                state.addLog("🏷️ " + p.getName() + " bought " + prop.getName()
                        + " for $" + prop.getPrice() + ". Balance: $" + p.getMoney());
            } else if (tile instanceof MetroStationTile metro) {
                p.subtractMoney(200);
                metro.setOwner(p);
                state.addLog("🚇 " + p.getName() + " bought " + metro.getName()
                        + " for $200. Balance: $" + p.getMoney());
            }
        } else {
            state.addLog("🚫 " + p.getName() + " declined to buy " + tile.getName() + ".");
        }

        state.pendingBuy = null;
        state.phase      = GameState.Phase.ROLL;
        checkBankruptAndContinue(p);
        refreshBank();

        // Check if bank ran out of money after this purchase/skip
        checkBankEmpty();

        state.fireChange();
    }

    private void landOn(Player p, Tile tile) {
        int pos = tile.getPosition();

        if (tile instanceof GoTile) {
            bank.payGoSalary(p);
            state.addLog("✅ " + p.getName() + " landed on GO! +$200. Balance: $" + p.getMoney());
            finishTurn(p);

        } else if (tile instanceof JailTile) {
            state.addLog("👀 " + p.getName() + " is in jail.");
            finishTurn(p);

        } else if (tile instanceof FreeTile) {
            state.addLog("😌 " + p.getName() + " rests at " + tile.getName() + ". Nothing happens.");
            finishTurn(p);

        } else if (tile instanceof TaxTile tax) {
            bank.collectTax(p, tax.getTaxAmount(), tax.getName());
            state.addLog("💰 " + p.getName() + " paid $" + tax.getTaxAmount()
                    + " tax (" + tax.getName() + "). Balance: $" + p.getMoney());
            checkBankruptAndContinue(p);

        } else if (tile instanceof ChanceTile) {
            CardDeck.Card card = chanceDeck.draw();
            state.addLog("🃏 " + p.getName() + " drew Chance: \"" + card.getDescription() + "\"");
            applyCard(p, card);

        } else if (tile instanceof CommunityChestTile) {
            CardDeck.Card card = communityDeck.draw();
            state.addLog("📦 " + p.getName() + " drew Community Chest: \"" + card.getDescription() + "\"");
            applyCard(p, card);

        } else if (tile instanceof MetroStationTile metro) {
            handlePurchasable(p, metro);

        } else if (tile instanceof Property prop) {
            handlePurchasable(p, prop);
        }
    }

    private void applyCard(Player p, CardDeck.Card card) {
        int effect = card.getCashEffect();
        if (effect > 0) {
            bank.pay(p, effect);
            state.addLog("💵 " + p.getName() + " receives $" + effect + ". Balance: $" + p.getMoney());
        } else if (effect < 0) {
            bank.collect(p, Math.abs(effect));
            state.addLog("💸 " + p.getName() + " pays $" + Math.abs(effect) + ". Balance: $" + p.getMoney());
        }
        // Check for special card keywords
        String desc = card.getDescription().toLowerCase();
        if (desc.contains("go to jail")) {
            p.goToJail(JailTile.JAIL_POSITION);
            state.addLog("🚔 " + p.getName() + " goes to Jail!");
            finishTurn(p);
            return;
        }
        checkBankruptAndContinue(p);
    }

    private void handlePurchasable(Player p, Property prop) {
        if (!prop.isOwned()) {
            if (p.getMoney() >= prop.getPrice()) {
                state.pendingBuy = prop;
                state.phase      = GameState.Phase.BUY;
            } else {
                state.addLog("❌ " + p.getName() + " can't afford " + prop.getName()
                        + " ($" + prop.getPrice() + "). Skipping.");
                finishTurn(p);
            }
        } else if (prop.getOwner().equals(p)) {
            state.addLog("🏠 " + p.getName() + " owns " + prop.getName() + ". No rent due.");
            finishTurn(p);
        } else {
            Player owner = prop.getOwner();
            p.subtractMoney(prop.getRent());
            owner.addMoney(prop.getRent());
            state.addLog("🏠 " + p.getName() + " pays $" + prop.getRent() + " rent to "
                    + owner.getName() + " for " + prop.getName()
                    + ". " + p.getName() + ": $" + p.getMoney()
                    + " | " + owner.getName() + ": $" + owner.getMoney());
            checkBankruptAndContinue(p);
        }
    }

    private void handlePurchasable(Player p, MetroStationTile metro) {
        Player owner = metro.getOwner();
        if (owner == null) {
            if (p.getMoney() >= 200) {
                state.pendingBuy = metro;
                state.phase      = GameState.Phase.BUY;
            } else {
                state.addLog("❌ " + p.getName() + " can't afford " + metro.getName() + ". Skipping.");
                finishTurn(p);
            }
        } else if (owner.equals(p)) {
            state.addLog("🚇 " + p.getName() + " owns " + metro.getName() + ". No ticket due.");
            finishTurn(p);
        } else {
            p.subtractMoney(25);
            owner.addMoney(25);
            state.addLog("🚇 " + p.getName() + " pays $25 metro ticket to " + owner.getName()
                    + ". " + p.getName() + ": $" + p.getMoney()
                    + " | " + owner.getName() + ": $" + owner.getMoney());
            checkBankruptAndContinue(p);
        }
    }

    private void checkBankruptAndContinue(Player p) {
        if (p.isBankrupt()) {
            state.addLog("💀 " + p.getName() + " is BANKRUPT!");
            // Release all owned properties back to the bank
            for (Tile t : state.tiles) {
                if (t instanceof Property prop && prop.isOwned() && prop.getOwner().equals(p)) {
                    prop.setOwner(null);
                }
                if (t instanceof MetroStationTile metro && metro.getOwner() != null
                        && metro.getOwner().equals(p)) {
                    metro.setOwner(null);
                }
            }
        }
        finishTurn(p);
    }

    /**
     * If the bank has run out of funds, end the game immediately.
     * Winner is the non-bankrupt player who owns the most properties
     * (Properties + MetroStationTiles combined). Ties are broken by
     * current cash balance.
     */
    private void checkBankEmpty() {
        if (state.gameOver || bank.getFunds() > 0) return;

        state.addLog("🏦 The bank has run out of money! Game over — counting properties...");

        List<Player> active = state.players.stream()
                .filter(p -> !p.isBankrupt())
                .toList();

        // Count properties (Property tiles) for every active player
        // MetroStationTiles are tracked separately in state.tiles
        active.forEach(p -> {
            int propCount = countAllProperties(p);
            state.addLog("📊 " + p.getName() + ": " + propCount + " properties");
        });

        // Find winner by most properties; break ties by cash
        Player winner = active.stream()
                .max(Comparator
                        .comparingInt((Player p) -> countAllProperties(p))
                        .thenComparingInt(Player::getMoney))
                .orElse(null);

        state.gameOver   = true;
        state.phase      = GameState.Phase.GAME_OVER;
        state.winnerName = winner != null ? winner.getName() : null;

        if (winner != null) {
            state.addLog("🏆 " + winner.getName()
                    + " wins with " + countAllProperties(winner) + " properties!");
        } else {
            state.addLog("No winner — everyone is bankrupt.");
        }
    }

    /**
     * Returns the total number of Properties + MetroStationTiles owned by a player.
     */
    public int countAllProperties(Player p) {
        int count = 0;
        for (Tile t : state.tiles) {
            if (t instanceof Property prop && prop.isOwned() && prop.getOwner().equals(p)) {
                count++;
            } else if (t instanceof MetroStationTile metro
                    && metro.getOwner() != null && metro.getOwner().equals(p)) {
                count++;
            }
        }
        return count;
    }

    private void finishTurn(Player p) {
        // Check game over by bankruptcy (only one active player left)
        List<Player> active = state.players.stream().filter(x -> !x.isBankrupt()).toList();
        if (active.size() <= 1) {
            state.gameOver   = true;
            state.phase      = GameState.Phase.GAME_OVER;
            state.winnerName = active.isEmpty() ? null : active.get(0).getName();
            state.addLog(state.winnerName != null ? "🏆 " + state.winnerName + " wins!" : "No winner.");
            return;
        }
        // Doubles: roll again (unless went to jail)
        if (state.lastDoubles && !p.isInJail() && !p.isBankrupt()
                && state.phase == GameState.Phase.ROLL) {
            state.addLog("🎲 " + p.getName() + " rolled doubles — rolls again!");
            return;
        }
        nextTurn();
    }

    private void nextTurn() {
        state.phase       = GameState.Phase.ROLL;
        state.lastDoubles = false;
        List<Player> all  = state.players;
        do {
            state.currentIdx = (state.currentIdx + 1) % all.size();
        } while (all.get(state.currentIdx).isBankrupt());
        state.addLog("— " + state.getCurrentPlayerName() + "'s turn —");
    }

    private void refreshBank() {
        state.bankFunds = bank.getFunds();
    }
}
