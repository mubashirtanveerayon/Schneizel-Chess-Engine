package schneizel;

import util.*;
import engine.*;
import java.util.ArrayList;

public class Schneizel {


    public ArrayList<String> history;

    public ArrayList<int[]> moves;

    private Engine engine;
    private AI ai;

    private void printInfo(){
        System.out.println("Schneizel Chess Engine v1.0 27 December, 2021");
    }

    public Schneizel(String fen){
        engine = new Engine(fen);
        ai = new AI(engine);
        history = new ArrayList<>();
        moves = new ArrayList<>();
        history.add(engine.fen);
        printInfo();
    }

    public Schneizel(){
        engine = new Engine(Constants.STARTING_FEN);
        ai = new AI(engine);
        history = new ArrayList<>();
        moves = new ArrayList<>();
        history.add(engine.fen);
        printInfo();
    }

    public int[] getBestMove(){
        return ai.BestMove();
    }

    public String getLegalMoves(){
        ArrayList<int[]> legalMoves = engine.getLegalMoves();
        String moves = "";
        for(int[] move:legalMoves){
            moves+=Util.cvtMove(move)+"\n";
        }
        return moves;
    }

    public String generateLegalMoves(int[] position){
        String generatedMoves = "";
        String legalMoves = getLegalMoves();
        String posStr = cvtPosition(position[0],position[1]);
        for(String legals:legalMoves.split("\n")){
            int[] move = parseMove(legals);
            if(cvtPosition(move[0],move[1]).equals(posStr)){
                generatedMoves+=cvtMove(move)+"\n";
            }
        }
        return generatedMoves;
    }

    public float evaluateBoard(boolean white){
        return engine.evaluateBoard(white);
    }

    public float evaluateBoard(){
        return engine.evaluateBoard(engine.whiteToMove);
    }

    public boolean isValidMove(String moveStr){
        String legalMoves = getLegalMoves();
        for(String move:legalMoves.split("\n")){
            if(!moveStr.isEmpty()&&!move.isEmpty()&&move.equalsIgnoreCase(moveStr)){
                return true;
            }
        }
        return false;
    }

    public void makeMove(String movestr){
        if(isValidMove(movestr)){
            history.add(engine.fen);
            int[] moveint = Util.parseMove(movestr);
            engine.move(moveint);
            moves.add(moveint);
            return;
        }
        System.out.println("Not a legal move!");
    }

    public void undoMove(){
        if(history.size()!=0){
            engine = new Engine(history.get(history.size()-1));
            ai = new AI(engine);
            history.remove(history.size()-1);
            moves.remove(moves.size()-1);
        }
    }

    public void setDifficulty(int depth){
        Constants.SEARCH_DEPTH = depth;
    }

    public int getDifficulty(){
        return Constants.SEARCH_DEPTH;
    }


    public char[][] loadBoardFromFen(String fen){
        return Util.loadBoard(fen);
    }

    public String getFen(){
        return engine.fen;
    }

    public boolean isDraw(){
      return engine.isDraw();
    }

    public String getState(){
      if(engine.checkMate(engine.whiteToMove)){
         return engine.whiteToMove?"b":"w";
      }
      if(isDraw()){
        return "d";
      }
      return null;
     }

    public String loadFenFromBoard(char [][] board){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int gap = 0;
            for (int j = 0; j < 8; j++) {
                if (board[j][i] != 8) {
                    if (gap != 0) {
                        sb.append(gap);
                    }
                    sb.append(board[j][i]);
                    gap = 0;
                } else {
                    gap++;
                }
            }
            if (gap != 0) {
                sb.append(gap);
            }
            if (i != Constants.COLUMNS - 1) {
                sb.append('/');
            }
        }
        return sb.toString()+" w KQkq - 0 1";
    }

    public char[][] getBoard(){
        return engine.board;
    }

    public char getTurn(){
        return engine.whiteToMove?'w':'b';
    }

    public boolean isValidFEN(String fen){
        return Util.FENValidator(fen);
    }

    public String cvtPosition(int file,int rank){
        return Util.cvtPosition(file,rank);
    }

    public String cvtMove(int[] move){
        return Util.cvtMove(move);
    }

    public int[] parseMove(String move){
        return Util.parseMove(move);
    }

    public int[] parsePosition(String position){
        return Util.parsePosition(position);
    }

    public boolean isKingInCheck(boolean white){
        return engine.isKingInCheck(white);
    }

    public int[] getKingPosition(boolean white){
        return Util.getKingPosition(engine.board,white);
    }


}
