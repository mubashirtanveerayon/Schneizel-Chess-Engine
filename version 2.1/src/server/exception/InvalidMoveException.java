package server.exception;

import server.move.Move;

public class InvalidMoveException extends RuntimeException{

    public InvalidMoveException(Move move){
        super("Invalid move: "+move);
    }

}
