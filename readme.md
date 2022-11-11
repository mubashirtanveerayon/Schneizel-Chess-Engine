# Schneizel Chess Engine
## _A Chess engine fully written in java_

### The engine is named after the anime character 'Schneizel'  from _Code Geass_.

## Features

- Move generation
- Fen validation, export and load
- AI which uses minimax with alpha-beta pruning

## Usage(LIB)

### Import Schneizel from `Schneizel_v1.0.jar`:

```sh
import schneizel.Schneizel;
```

### Instantiate an object:

```sh
Schneizel engine = new Schneizel();
```

Note that when nothing is passed in the constructor, the board will be initialized with the
starting chess board position. A FEN string can be passed to the constructor to initialize the
expected chess board position as shown below:

```sh
Schneizel engine = new Schneizel("4N3/2P3Rr/3pK1pP/5p2/1n2kB2/4P3/p3p3/5N2 w - - 0 1");
```


### Get best move for the current board position:

```sh
int[] bestMove = engine.getBestMove();
```

Calculating a good move for a given board position requires time and computation.
That is why the `getBestMove()` method is recommended to use in a seperate thread from the main application thread.

### Convert move of type `int[]` to String:

```sh
String moveStr = engine.cvtMove(move);
```

### Convert moveStr of type `String` to type `int[]`:

```sh
int[] move = engine.parseMove(moveStr);
```

### Make move:

```sh
engine.makeMove(moveStr);
```

The move String should look like this:

```
<from_file><from_rank><to_file><to_rank>
```

To specify a pawn promotion(by default the pawn will be promoted to a queen, in that case the following example is not applicable):

```
<from_file><from_rank><to_file><to_rank><promotion_type>
```

Here:

```sh
String promotion_type = "1"; // knight promotion
promotion_type = "2"; // bishop promotion
promotion_type = "3"; // rook promotion
```

Examples:

```sh
String moveStr = "a2a4"; // from square a2 to square a4
moveStr = "a7a8"; // pawn peomotion to queen from square a7 to square a8
moveStr = "a7a8"+promotion_type; // pawn peomotion to knight from square a7 to square a8 where promotion_type = 1
moveStr = "a7a82"+promotion_type; // pawn peomotion to bishop from square a7 to square a8 where promotion_type = 2
moveStr = "a7a83"+promotion_type; // pawn peomotion to rook from square a7 to square a8 where promotion_type = 3
```

### Get all legal moves for a given board position:

```sh
String legalMoves = engine.getLegalMoves();
```

The `getLegalMoves()` method returns a sum of all legal move string. Individual move String can be found using the `split(regex)` method of java.lang.String class as the move Strings are added to the resulting string seperated by new lines.

The following example shows how to get all move Strings seperated in an array:

```sh
String legalMoves = engine.getLegalMoves();
String[] legalMovesStr = legalMoves.split("\n");
```

#### Threre are many other useful methods all of which are self explanatory. One can easily find  those out by going through the `schneizel.Schneizel` class. An example project of the _Schneizel Chess Engine_ can be found [here](https://github.com/mubashirtanveerayon/My-Games/tree/master/Chess%20Game).

## Usage(CLI)

### Print board:

```sh
d
```

### Load fen:

```sh
position;fen;rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1
```

Print fen:

```sh
fen
```

### Generate number of moves for a certain depth:

```sh
go;4
```
### Print legal moves for the current board position:

```sh
moves
```

### Print best move:

```sh
go
```

### Play the best move:

```sh
play
```

### Count pieces:
```sh
c
```

### Set search depth:

```sh
4
```

### Print search depth:

```sh
s
```

### Make move:

```sh
e2e4
```

### Export fen string:

```sh
export;C:\Users\user\Desktop\fen.txt
```

### Flip board:

```sh
flip
```

### Evaluate board:

```sh
evaluate;w
```

or,

```sh
evaluate;b
```

## About the engine
This is not a uci supported chess engine. The ai is very weak compared to stockfish or other chess engines out there as it uses classic evaluation.
The engine is getting a [re-write](https://github.com/mubashirtanveerayon/Schneizel-Rewrite) soon.

## License

### This engine is licensed under [Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License](https://creativecommons.org/licenses/by-nc-sa/4.0/)
