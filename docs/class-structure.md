# Class Structure

## Execution Entry Points

### `Main`
Console entry point. Creates a `Game` instance, calls `setup()` then `play()`.

### `MonopolyApp`
Swing GUI entry point. Shows a player-name dialog, builds `GameState` and `SwingGame`, and launches the `JFrame` with `BoardPanel` and a side panel.

---

## Core Model Layer
Shared by both console and GUI paths.

### `Bank`
Central financial authority with a finite fund pool (`$20,580`).
- `pay(player, amount)` — bank → player
- `collect(player, amount)` — player → bank
- `buyProperty(player, property)` — validates and executes a property purchase
- `payGoSalary(player)` — pays $200 on GO
- `collectTax(player, amount, reason)` — tax collection with logging
- `collectMetroTicket(player)` — charges $25 metro fare
- `applyCard(player, amount)` — positive pays player, negative charges player

### `Board`
Constructs and stores the ordered list of all 40 `Tile` instances.
- `BOARD_SIZE = 40`
- `getTile(position)` — returns tile at index
- `getBank()` — returns the shared `Bank`

### `Player`
Represents a single player's complete state.
- Fields: `name`, `money`, `position`, `inJail`, `jailTurns`, `properties`, `bankrupt`
- `move(steps, boardSize)` — advances position with wrap-around
- `goToJail(jailPosition)` — teleports player, sets jail flag
- `releaseFromJail()` — clears jail state
- `subtractMoney(amount)` — sets `bankrupt = true` if balance hits 0

### `Dice`
Simulates two six-sided dice.
- `roll()` — rolls both dice, returns sum
- `isDoubles()` — true if both dice match
- `getDie1()` / `getDie2()` — individual die values

### `CardDeck`
A shuffled, cyclically-drawn deck of cards. Used for both Chance and Community Chest.
- Inner class `Card`: holds `description` (String) and `cashEffect` (int)
- `draw()` — returns next card, wraps around on exhaustion
- Built with 25 cards per deck type; shuffled on construction

---

## Tile Hierarchy

```
Tile  (abstract)
├── GoTile
├── FreeTile
├── JailTile
├── TaxTile
├── ChanceTile
├── CommunityChestTile
├── MetroStationTile
└── Property
```

### `Tile` (abstract)
Base class for all board spaces.
- Fields: `name`, `position`
- Abstract method: `landOn(Player)`

### `GoTile` — position 0
Awards $200 to the player on landing.

### `FreeTile` — positions 10 (Cascade), 30 (Republic Square)
No effect on landing. Position 30 is intercepted by `SwingGame` to send the player to jail before `landOn` is called.

### `JailTile` — position 20
- `landOn`: "Just Visiting" message.
- `handleJailTurn(player, allPlayers)`: manages 3-turn jail rule and mass-release when all active players are jailed.

### `TaxTile` — positions 4, 12, 28, 38
- Fields: `taxAmount`, `color`
- `landOn`: deducts `taxAmount` from player (console path). Swing path uses `Bank.collectTax` instead.

### `ChanceTile` — positions 7, 22, 36
Draws from a shared `static` Chance deck. Applies cash effect directly to player in console mode; Swing uses `SwingGame.applyCard`.

### `CommunityChestTile` — positions 2, 17, 33
Same structure as `ChanceTile` but uses the Community Chest deck.

### `MetroStationTile` — positions 5, 15, 25, 35
Purchasable for $200; charges a flat $25 rent.
- Fields: `price = 200`, `BASE_RENT = 25`, `owner` (private, no setter — set via reflection in Swing path), `color`

### `Property`
All purchasable street tiles.
- Fields: `price`, `rent`, `owner`, `colorGroup`
- `setOwner(player)`, `isOwned()`, `getOwner()`, `getPrice()`, `getRent()`, `getColorGroup()`

---

## Console Path

### `Game`
Text-based game loop.
- Fields: `board`, `dice`, `players`, `currentPlayerIndex`, `scanner`, `bank`
- `setup()` — prompts for player count and names
- `play()` — main loop until one player remains
- `takeTurn(player)` — jail check → roll → move → GO check → `tile.landOn()`

---

## Swing GUI Path

### `GameState`
Observable shared model; all mutable state lives here.
- Enum `Phase`: `ROLL`, `BUY`, `GAME_OVER`
- Fields: `players`, `tiles`, `currentIdx`, `dice`, `phase`, `pendingBuy`, `lastDoubles`, `bankFunds`, `log`, `gameOver`, `winnerName`
- `addChangeListener(Runnable)` / `fireChange()` — minimal observer pattern; updates run on the EDT via `SwingUtilities.invokeLater`

### `SwingGame`
GUI game controller. Translates button clicks into model operations and writes results into `GameState`.
- `doRoll()` — handles jail, rolls dice, moves player, calls tile handler
- `doBuy(boolean yes)` — completes or cancels a purchase on `state.pendingBuy`
- `landOn(player, tile)` — dispatches by tile type using `instanceof`
- `finishTurn(player)` — checks game-over, handles doubles re-roll, advances turn
- `setMetroOwner(metro, player)` — uses reflection to set `MetroStationTile.owner`

### `MonopolyApp`
Swing window builder and view updater.
- `showSetupDialog()` — modal dialog for player names
- `buildSidePanel(state, game, board)` — creates right-hand panel (player cards, buttons, log)
- `refreshSide(state, side)` — walks the component tree by name to update dynamic UI parts

### `BoardPanel`
Custom `JPanel` that paints the entire board using Java2D.
- Constants: `BOARD_PX = 724`, `CORNER_PX = 80`, `CELL_PX = 56`
- `GROUP_COLORS` — maps color group names (Red, Light Blue, Gray, Orange, Pink, Green, Yellow, Dark Blue, Metro) to `Color` objects
- `cellRect(pos)` — maps board position 0–39 to a pixel `Rectangle`
- `paintComponent` — draws background, tiles, color bars, text, player tokens
- `drawTokens` — places up to 4 player tokens per cell in a 2×2 grid
