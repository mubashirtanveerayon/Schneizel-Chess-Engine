# Schneizel Chess Engine
## _A Chess engine fully written in java_

### The engine is named after the anime character 'Schneizel'  from _Code Geass_.

## Features

- Move generation
- Fen validation, export and load
- AI which uses minimax with alpha-beta pruning
- GUI

## Installation
Schneizel chess engine requires JDK **1.8** or above to run properly.
The Schneizel.jar file can be downloaded from [release.](https://github.com/mubashirtanveerayon/Schneizel-Chess-Engine/releases)

Run from the terminal using

```sh
java -jar Schneizel.jar
```

## Usage

Print board:

```sh
d
```

Load fen:

```sh
position;fen;rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1
```

Print fen:

```sh
fen
```

Generate number of moves for a certain depth:

```sh
go;4
```
Print legal moves for the current board position:

```sh
moves
```

Print best move:

```sh
go
```

Play the best move:

```sh
play
```

Count pieces:
```sh
c
```

Set search depth:

```sh
4
```

Print search depth:

```sh
s
```

Make move:

```sh
e2e4
```

Export fen string:

```sh
export;C:\Users\user\Desktop\fen.txt
```

Flip board:

```sh
flip
```

Evaluate board:

```sh
evaluate;w
```

or,

```sh
evaluate;b
```

## What not to expect
This is not a uci chess engine, as I am not a professional chess protgrammer but would like to be one. The ai is a pretty weak compared to stockfish or other chess engines out there as it uses a static evaluation function. I might implement neural network and other better approaches to make the A.I stronger in the future.
