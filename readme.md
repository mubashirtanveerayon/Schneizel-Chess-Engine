
# Schneizel Chess Engine

### **A UCI chess engine written in Java.**


## Features

 - Integrable with your favourite GUI
 - Can be used as a chess library
 - Faster and bug free move generation
 - PGN utilities
 - Search time constraints
 - Can play bullet, blitz, rapid
 - Analysis mode
 - Less resource consumption

## Technical details
[Schneizel v2](https://github.com/mubashirtanveerayon/Schneizel-v2) is a mailbox chess engine. That means, it takes the array based approach for board representation.

### Search
 - AB pruning
 - Iterative deepening
 - Internal move ordering
 - Transposition table (caches legal moves, capture moves and evalutation)

### Evaluation
 - Piece square table
 - Pawn chain
 - Passed pawn
 - Material count
 - Cornering opponent's king in endgames

## Does not feature

 - GUI

## Example project
[My Chess Game](https://github.com/mubashirtanveerayon/ChessGame)

## Things To Note
 - Schneizel v2 performs best when it is paired with JDK 8.

## License
This project is licensed under [Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License](https://creativecommons.org/licenses/by-nc-sa/4.0/)
