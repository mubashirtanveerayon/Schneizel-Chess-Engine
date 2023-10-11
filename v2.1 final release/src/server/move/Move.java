package server.move;

import server.util.Constants;
import server.util.Util;

public final class Move {

    public int locFile, locRank, destFile, destRank,halfMoveClock;
    public char capturedPiece ;
    public char promotionPiece = Constants.EMPTY_SQUARE;

    public boolean isKingSideCastling = false, isQueenSideCastling = false, isEnPassant = false;

    String castlingFEN;

    String enPassantSquare;


    public Move(int lf,int lr,int df,int dr,char[][] board,String castle,String enPassant,int halfMove){
        locFile = lf;
        locRank = lr;
        destFile = df;
        destRank = dr;
        capturedPiece = board[dr][df];
        castlingFEN = castle;
        enPassantSquare = enPassant;
        halfMoveClock = halfMove;
    }

    @Override
    public String toString(){
        String algebra = Util.cvtMove(locFile,locRank,destFile,destRank);
        if(promotionPiece != Constants.EMPTY_SQUARE){
            algebra += promotionPiece;
        }
        return algebra;
    }




}
