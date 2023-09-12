package server.exception;

public class NoPieceOnSquareException extends RuntimeException {
    public NoPieceOnSquareException(int file, int rank) {
        super("["+file+","+rank+"]");
    }
}
