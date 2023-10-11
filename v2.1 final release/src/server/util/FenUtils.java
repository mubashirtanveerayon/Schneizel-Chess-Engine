package server.util;

import server.board.ChessBoard;
import server.exception.InvalidFenException;

import java.util.Arrays;

public class FenUtils {




    public static String generate(ChessBoard board){
        String rest = Character.toString(board.whiteToMove?Constants.WHITE:Constants.BLACK);
        rest += " "+board.castlingFEN+" "+board.enPassantSquare+" "+board.halfMoveClock+" "+board.fullMoveClock;
        return generate(board.board,rest);
    }

    public static String generate(char[][] board,String rest){
        String fen = "";
        for(int i=0;i<board.length-1;i++){
            fen+=FenUtils.getRank(board[i])+"/";
        }
        fen+=FenUtils.getRank(board[board.length-1])+" "+rest;
        return fen;
    }

    public static char[][] getBoardFromFEN(String fen){
        String[] fenParts =split(fen);
        int file = 0;
        int rank = 0;
        char[][] board = Util.getEmptyBoard();
        for(int i=0;i<8;i++){
            for(char c:fenParts[i].toCharArray()){
                if(Character.isDigit(c)){
                    file += Util.getNumericValue(c);
                }else{
                    board[rank][file] = c;
                    file++;
                }
            }
            rank++;
            file = 0;
        }
        return board;
    }

    public static void printFile(char[] file){
        for(char c:file){
            Util.writeToLog(Character.toString(c));
        }
        Util.writeToLog("\n");
    }

    public static char[] parseFile(String fileFen){
        char[] file = new char[8];
        Arrays.fill(file, Constants.EMPTY_SQUARE);
        int f = 0;
        for(char c : fileFen.toCharArray()){
            if(Character.isDigit(c)) {
                f += Util.getNumericValue(c);
            }else{
                file[f] = c;
                f++;
            }
        }
        return file;
    }

    public static String getRank(char[] rank){
        String fen="";
        int f = -1;
        for(int i=0;i<rank.length;i++){
            if(rank[i] == Constants.EMPTY_SQUARE){
                if(f<0) {
                    f=0;
                }
                f++;
                if(i+1 == rank.length){
                    fen+=String.valueOf(f);
                }
            }else{
                if(f>0){
                    fen+=String.valueOf(f);
                    f=-1;
                }
                fen+=rank[i];
            }
        }
        return fen;
    }


    public static void isValid(String fen) throws InvalidFenException {
        String[] parts = fen.split("/");
        if(parts.length!=8){
            throw new InvalidFenException("Fen string is invalid! Rank missing! "+fen);
        }
        String[] fenUtils = parts[7].split(" ");
        if(fenUtils.length!=6){
            throw new InvalidFenException("Fen string is invalid! Some parts are missing at the end! "+fen);
        }
        try{
            Integer.parseInt(fenUtils[5]);
            Integer.parseInt(fenUtils[4]);
        }catch(Exception e){
            throw new InvalidFenException("Fen string is invalid! Could not parse halfmove or fullmove! "+fen);
        }
        if(!fen.contains(Character.toString(Constants.WHITE_KING)) || !fen.contains(Character.toString(Constants.BLACK_KING))){
            throw new InvalidFenException("Fen string is invalid! Could not find the king! "+fen);
        }
//        ChessBoard cb = new ChessBoard(fen,false);
//        int[] kingPosition = cb.kingPosition();
//        if(cb.gs == GameState.CHECK){
//            switch(cb.turn){
//                case Constants.WHITE:
//                    cb.turn = Constants.BLACK;
//                    if(cb.squareUnderAttack(kingPosition[0],kingPosition[1])){
//                        throw new InvalidFenException("Fen string is invalid! White king is in check but its black's turn to move! "+fen);
//                    }
//                    break;
//                case Constants.BLACK:
//                    cb.turn = Constants.WHITE;
//                    if(cb.squareUnderAttack(kingPosition[0],kingPosition[1])){
//                        throw new InvalidFenException("Fen string is invalid! Black king is in check but its white's turn to move! "+fen);
//                    }
//                    break;
//            }
//        }
    }

    public static String[] split(String fen){
        String[] split = new String[13];
        String[] fenPartsBySlash = fen.split("/");
        String[] fenPartsBySpace = fen.split(" ");
        for(int i=0;i<fenPartsBySlash.length;i++){
            if(i+1 == fenPartsBySlash.length){
                split[i] = fenPartsBySlash[i].split(" ")[0];
            }else {
                split[i] = fenPartsBySlash[i];
            }
        }
        for(int i=1;i<fenPartsBySpace.length;i++){
            split[7+i] = fenPartsBySpace[i];
        }
        return split;
    }

    public static String toString(String[] fenParts){
        String fen = "";
        for(int i=0;i<fenParts.length;i++){
            if(i < 7){
                fen += fenParts[i] + "/";
            }else if(i==7){
                fen += fenParts[i];
            }else{
                fen += " "+fenParts[i];
            }
        }
        return  fen;
    }


    public static String cat(String[] fenParts){
        return cat(fenParts,false);
    }

    public static String cat(String[] fenParts,boolean positionOnly) {
        String fen="";
        int lim = positionOnly?9:fenParts.length;
        for(int i=0;i<lim;i++){
            if(i<7) {
                fen += fenParts[i] + "/";
            }else{
                fen += fenParts[i] + " ";
            }
        }

        return fen.trim();
    }
}
