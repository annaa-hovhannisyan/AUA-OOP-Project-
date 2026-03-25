# Class Structure

## Game
Controls the flow of the game (turns, win condition).

## Player
Represents a player.
- name
- money
- position

## Board
Contains all tiles.

## Tile (parent class)
Represents a space on the board.

## Property (extends Tile)
- price
- owner

## Dice
Generates random numbers for movement.
