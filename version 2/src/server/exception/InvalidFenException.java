package server.exception;

public class InvalidFenException extends RuntimeException{
    public InvalidFenException(String error){
        super(error);
    }
}
