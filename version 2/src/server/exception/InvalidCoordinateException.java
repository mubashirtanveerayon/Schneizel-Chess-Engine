package server.exception;

public class InvalidCoordinateException extends RuntimeException{

    public InvalidCoordinateException(int file,int rank){
        super("Invalid coordinate: ["+file+","+rank+"]");
    }

}
