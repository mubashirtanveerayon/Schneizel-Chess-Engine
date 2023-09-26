package server.move;

import server.board.ChessBoard;

import java.util.ArrayList;
import server.util.*;

public class MoveManager {

    public ChessBoard cb;
    ArrayList<String> moves;


    public MoveManager(ChessBoard cb){
        this.cb = cb;
        moves = new ArrayList<>();
    }


    public String cvt(String moveStr){//move to algebra
        String stdMove;
        String[] moveParts = moveStr.split(Constants.MOVE_SEPARATOR);
        if(moveStr.contains(Constants.QUEEN_SIDE_CASTLING)){
            int rank = cb.turn == Constants.WHITE?7:0;
            stdMove = Util.cvtMove(4,rank,2,rank);
        }else if(moveStr.contains(Constants.KING_SIDE_CASTLING)){
            int rank = cb.turn == Constants.WHITE?7:0;
            stdMove = Util.cvtMove(4,rank,6,rank);
            //System.out.println(stdMove);
        }else{
            stdMove = Util.cvtMove(Integer.parseInt(Character.toString(moveStr.charAt(0))),Integer.parseInt(Character.toString(moveStr.charAt(1))),Integer.parseInt(Character.toString(moveStr.charAt(2))),Integer.parseInt(Character.toString(moveStr.charAt(3))));
        }
        if(moveParts.length == Constants.PROMOTION_MOVE_LENGTH && !moveStr.contains(Constants.EN_PASSANT_NOTATION)){
            stdMove += moveParts[moveParts.length - 1];
        }
        return stdMove;
    }

    public String parse(String stdMove){//algebra to move
        int lf = Constants.FILES.indexOf(stdMove.charAt(0));
        int lr = Constants.RANKS.indexOf(stdMove.charAt(1));
        int df = Constants.FILES.indexOf(stdMove.charAt(2));
        int dr = Constants.RANKS.indexOf(stdMove.charAt(3));
        String move=Util.cvtMove(lf,lr,df,dr,cb.board,cb.fenParts)+Constants.MOVE_SEPARATOR;
        switch(Character.toUpperCase(cb.board[lr][lf])){
            case Constants.WHITE_KING:
                switch(lf-df) {
                    case 2:
                        move = Constants.QUEEN_SIDE_CASTLING;
                        break;
                    case -2:
                        move = Constants.KING_SIDE_CASTLING;
                        break;
                }
                move+=Constants.MOVE_SEPARATOR+cb.fenParts[9]+Constants.MOVE_SEPARATOR+cb.fenParts[10];
                break;
            case Constants.WHITE_PAWN:
                if(dr == 0){
                    move+=Character.toUpperCase(stdMove.charAt(4));
                }else if(dr == 7){
                    move+=Character.toLowerCase(stdMove.charAt(4));
                }else if(lf-df!=0&&cb.board[dr][df] == Constants.EMPTY_SQUARE){
                    move+=Constants.EN_PASSANT_NOTATION;
                }

                break;
            default:
                move = move.substring(0,move.length()-1);
        }
        return move;
    }



    public boolean checkIfGameOver(){
        int numberOfMoves = getAllMoves().size();
        if (numberOfMoves == 0){
            if (cb.gs == GameState.CHECK){
                cb.gs = GameState.CHECKMATE;
            }else{
                cb.gs = GameState.STALEMATE;
            }
            return true;
        }else{
            return false;
        }

    }

    public String moveGenerationTestWOLog(int depth,boolean stdOutput) {//for debug purpose using stockfish
        ArrayList<String> moves = getAllMoves() ;
        StringBuilder output=new StringBuilder();
        int numPositions = 0;
        String fen = FenUtils.cat(cb.fenParts);
        for (String moveStr:moves) {
            makeMove(moveStr);
            int numMoves = recurseMoveGeneration(depth-1);
            numPositions += numMoves;
            undoMove(moveStr);
            if(!fen.equals(FenUtils.cat(cb.fenParts))){
                System.out.println("Original "+fen);
                System.out.println("Current  "+FenUtils.cat(cb.fenParts));
                System.out.println(cvt(moveStr));
            }
            if(stdOutput){
                String stdMove;
                if(moveStr.contains(Constants.KING_SIDE_CASTLING)){
                    int rank = cb.turn == Constants.WHITE?7:0;
                    stdMove = Util.cvtMove(4,rank,5,rank);
                }else if(moveStr.contains(Constants.QUEEN_SIDE_CASTLING)){
                    int rank = cb.turn == Constants.WHITE?7:0;
                    stdMove = Util.cvtMove(4,rank,2,rank);
                }else{
                    stdMove = Util.cvtMove(Integer.parseInt(Character.toString(moveStr.charAt(0))),Integer.parseInt(Character.toString(moveStr.charAt(1))),Integer.parseInt(Character.toString(moveStr.charAt(2))),Integer.parseInt(Character.toString(moveStr.charAt(3))));
                }
                output.append(stdMove).append(": ").append(numMoves).append("\n");
            }else {
                output.append(moveStr).append(": ").append(numMoves).append("\n");
            }
        }
        output.append("Nodes searched : ").append(numPositions).append( "\n");
        return output.toString();
    }

    public String moveGenerationTest(int depth,boolean stdOutput){
        ArrayList<String> moves = getAllMoves() ;
        int numPositions = 0;
        String fen = FenUtils.cat(cb.fenParts);
        StringBuilder output = new StringBuilder();
        for (String moveStr:moves) {

                //System.out.println(FenUtils.cat(cb.fenParts));
//                ChessBoard c = new ChessBoard(FenUtils.cat(cb.fenParts));
//                System.out.println(c.gs);
//                System.out.println(Util.getBoardVisual(cb.board));
//                System.out.println(Util.getBoardVisual(c.board));
//                if(cb.gs == GameState.NORMAL){
//                    System.out.println(Util.getBoardVisual(cb.board));
//                    cb.checkBoard();
//                    //System.out.println(cb.checkers);
//                    System.out.println(cb.gs);
//                }


            //debug code
//            if(!fen.equals(FenUtils.cat(cb.fenParts))){
//                System.out.println("Original: "+fen);
//                System.out.println("Current: "+FenUtils.cat(cb.fenParts));
//                System.out.println((cvt(moveStr)));
//                System.out.println("###");
//                System.out.println(cb.stats());
//            }
            //ends here

//            if(cvt(moveStr).equals("c2c1") ){
//                System.out.println(cb.stats());
//                System.out.println("###");
//            }

            makeMove(moveStr);
            int numMoves = recurseMoveGeneration(depth-1);
            numPositions += numMoves;
            undoMove(moveStr);
//            if(cvt(moveStr).equals("c2c1") ){
//                System.out.println(cb.stats());
//                System.out.println("###");
//            }
            if(stdOutput){
                String stdMove = cvt(moveStr);
                output.append(stdMove + ": " + numMoves).append("\n");
            }else {
                output.append(moveStr + ": " + numMoves).append("\n");
            }
        }
        output.append("Nodes searched: "+numPositions).append("\n");
        return output.toString();
    }


//
//    public void moveGenerationTest(int depth,boolean stdOutput){
//        ArrayList<String> moves = getAllMoves() ;
//        int numPositions = 0;
//        String fen = FenUtils.cat(cb.fenParts);
//        String output = "";
//        for (String moveStr:moves) {
//            makeMove(moveStr);
//            int numMoves = recurseMoveGeneration(depth-1);
//            numPositions += numMoves;
//            undoMove(moveStr);
//            //debug code
//            if(!fen.equals(FenUtils.cat(cb.fenParts))){
//                Util.writeToLog("Original "+fen);
//                Util.writeToLog("Current  "+FenUtils.cat(cb.fenParts));
//                Util.writeToLog(cvt(moveStr));
//            }
//            //ends here
//            if(stdOutput){
//                String stdMove;
//                if(moveStr.contains(Constants.KING_SIDE_CASTLING)){
//                    int rank = cb.turn == Constants.WHITE?7:0;
//                    stdMove = Util.cvtMove(4,rank,5,rank);
//                }else if(moveStr.contains(Constants.QUEEN_SIDE_CASTLING)){
//                    int rank = cb.turn == Constants.WHITE?7:0;
//                    stdMove = Util.cvtMove(4,rank,2,rank);
//                }else{
//                    stdMove = Util.cvtMove(Integer.parseInt(Character.toString(moveStr.charAt(0))),Integer.parseInt(Character.toString(moveStr.charAt(1))),Integer.parseInt(Character.toString(moveStr.charAt(2))),Integer.parseInt(Character.toString(moveStr.charAt(3))));
//                }
//                Util.writeToLog(stdMove + ": " + numMoves);
//            }else {
//                Util.writeToLog(moveStr + ": " + numMoves);
//            }
//        }
//        Util.writeToLog("Nodes searched : "+numPositions);
//
//    }

    private int recurseMoveGeneration(int depth){
        if (depth <= 0) {
            return 1;
        }
        ArrayList<String> moves = getAllMoves() ;
        int numPositions = 0;
        for (String moveStr:moves) {

            makeMove(moveStr);

            numPositions += recurseMoveGeneration(depth-1);
            undoMove(moveStr);

        }
        return numPositions;
    }


    public ArrayList<String> getAllMoves() {
        ArrayList<String> allMoves = new ArrayList<>();
        for(int i=0;i<8;i++){
            for(int j=0;j<8;j++){
                if(cb.board[i][j] != Constants.EMPTY_SQUARE && !Util.isEnemyPiece(cb.turn,cb.board[i][j])){
                    allMoves.addAll(generateMove(j,i));
                }
            }
        }
        return allMoves;
    }

    // Here are all the functions from previous move class
    public void makeMove(String move){
        if(move.contains(Constants.QUEEN_SIDE_CASTLING)){

            int rank = cb.turn == Constants.WHITE?7:0;
            cb.board[rank][2] = cb.board[rank][4];
            cb.board[rank][4] = Constants.EMPTY_SQUARE;
            cb.board[rank][3] = cb.board[rank][0];
            cb.board[rank][0] = Constants.EMPTY_SQUARE;
            cb.pieceLocations.add(2 + rank * 8);
            cb.pieceLocations.remove((Object)(4 + rank * 8));
            cb.pieceLocations.add(3 + rank * 8);
            cb.pieceLocations.remove((Object)(0 + rank * 8));
            if(cb.turn == Constants.WHITE){
                cb.whiteKingPosition[0] = 2;
            }else{
                cb.blackKingPosition[0] = 2;
            }
            cb.fenParts[rank] = FenUtils.getRank(cb.board[rank]);
            if(cb.turn  == Constants.WHITE){
                cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.WHITE_KING),"").replace(Character.toString(Constants.WHITE_QUEEN),"");
            }else{
                cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.BLACK_KING),"").replace(Character.toString(Constants.BLACK_QUEEN),"");
            }
            cb.turn  = cb.turn  == Constants.WHITE?Constants.BLACK:Constants.WHITE;
            cb.fenParts[8] = Character.toString(cb.turn);
            cb.fenParts[10] = "-";
            cb.fenParts[11] = Integer.toString(Integer.parseInt(cb.fenParts[11])+1);
        }else if(move.contains(Constants.KING_SIDE_CASTLING)){

            int rank = cb.turn == Constants.WHITE?7:0;
            cb.board[rank][6] = cb.board[rank][4];
            cb.board[rank][4] = Constants.EMPTY_SQUARE;
            cb.board[rank][5] = cb.board[rank][7];
            cb.board[rank][7] = Constants.EMPTY_SQUARE;
            cb.pieceLocations.add(6 + rank * 8);
            cb.pieceLocations.remove((Object)(4 + rank * 8));
            cb.pieceLocations.add(5 + rank * 8);
            cb.pieceLocations.remove((Object)(7 + rank * 8));
            if(cb.turn == Constants.WHITE){
                cb.whiteKingPosition[0] = 6;
            }else{
                cb.blackKingPosition[0] = 6;
            }
            cb.fenParts[rank] = FenUtils.getRank(cb.board[rank]);
            if(cb.turn  == Constants.WHITE){
                cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.WHITE_KING),"").replace(Character.toString(Constants.WHITE_QUEEN),"");
            }else{
                cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.BLACK_KING),"").replace(Character.toString(Constants.BLACK_QUEEN),"");
            }
            cb.turn  = cb.turn  == Constants.WHITE?Constants.BLACK:Constants.WHITE;
            cb.fenParts[8] = Character.toString(cb.turn);
            cb.fenParts[10] = "-";
            cb.fenParts[11] = Integer.toString(Integer.parseInt(cb.fenParts[11])+1);


        }else if(move.charAt(1) == move.charAt(3)){
            int rank = Integer.parseInt(String.valueOf(move.charAt(1)));
            int locFile = Integer.parseInt(String.valueOf(move.charAt(0)));
            int destFile = Integer.parseInt(String.valueOf(move.charAt(2)));
            if(cb.board[rank][locFile] == Constants.WHITE_KING){
                cb.whiteKingPosition[0] = destFile;
                cb.whiteKingPosition[1] = rank;
            }else if(cb.board[rank][locFile] == Constants.BLACK_KING){
                cb.blackKingPosition[0] = destFile;
                cb.blackKingPosition[1] = rank;
            }
            if(cb.board[rank][destFile]!=Constants.EMPTY_SQUARE){
                cb.fenParts[11] = "0";
            }else{
                cb.fenParts[11] = Integer.toString(Integer.parseInt(cb.fenParts[11])+1);
            }

            if(rank == 0 || rank == 7){
                switch(cb.board[rank][locFile]){
                    case Constants.WHITE_ROOK:
                        if(locFile==0 && cb.fenParts[9].contains(String.valueOf(Constants.WHITE_QUEEN))){
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.WHITE_QUEEN),"");
                        }else if(locFile==7 && cb.fenParts[9].contains(String.valueOf(Constants.WHITE_KING))){
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.WHITE_KING), "");
                        }
                        break;
                    case Constants.WHITE_KING:
                        if(cb.fenParts[9].contains(String.valueOf(Constants.WHITE_QUEEN)) || cb.fenParts[9].contains(String.valueOf(Constants.WHITE_KING))) {
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.WHITE_KING), "").replace(Character.toString(Constants.WHITE_QUEEN), "");
                        }
                        break;
                    case Constants.BLACK_ROOK:
                        if(locFile==0 && cb.fenParts[9].contains(String.valueOf(Constants.BLACK_QUEEN))){
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.BLACK_QUEEN),"");
                        }else if(locFile==7 && cb.fenParts[9].contains(String.valueOf(Constants.BLACK_KING))) {
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.BLACK_KING), "");
                        }
                        break;
                    case Constants.BLACK_KING:
                        if(cb.fenParts[9].contains(String.valueOf(Constants.BLACK_QUEEN)) || cb.fenParts[9].contains(String.valueOf(Constants.BLACK_KING))) {
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.BLACK_KING), "").replace(Character.toString(Constants.BLACK_QUEEN), "");
                        }
                        break;
                }
                switch(cb.board[rank][destFile]){
                    case Constants.WHITE_ROOK:
                        if (rank!=7){
                            break;
                        }
                        if(destFile==0 && cb.fenParts[9].contains(String.valueOf(Constants.WHITE_QUEEN))){
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.WHITE_QUEEN),"");
                        }else if(destFile==7 && cb.fenParts[9].contains(String.valueOf(Constants.WHITE_KING))){
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.WHITE_KING), "");
                        }
                        break;
                    case Constants.BLACK_ROOK:
                        if (rank != 0){
                            break;
                        }
                        if(destFile==0 && cb.fenParts[9].contains(String.valueOf(Constants.BLACK_QUEEN))){
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.BLACK_QUEEN),"");
                        }else if(destFile==7 && cb.fenParts[9].contains(String.valueOf(Constants.BLACK_KING))) {
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.BLACK_KING), "");
                        }
                        break;
                }
            }

            cb.board[rank][destFile] = cb.board[rank][locFile];
            cb.board[rank][locFile] = Constants.EMPTY_SQUARE;
            cb.pieceLocations.add(destFile + rank * 8);
            cb.pieceLocations.remove((Object)(locFile + rank * 8));

            cb.fenParts[rank] = FenUtils.getRank(cb.board[rank]);
            cb.fenParts[10] = "-";
            cb.turn  = cb.turn  == Constants.WHITE?Constants.BLACK:Constants.WHITE;
            cb.fenParts[8] = Character.toString(cb.turn);

        }else if(move.charAt(0) == move.charAt(2)){
            int file = Integer.parseInt(String.valueOf(move.charAt(0)));
            int locRank = Integer.parseInt(String.valueOf(move.charAt(1)));
            int destRank = Integer.parseInt(String.valueOf(move.charAt(3)));
            if(cb.board[locRank][file] == Constants.WHITE_KING){
                cb.whiteKingPosition[1] = destRank;
            }else if(cb.board[locRank][file] == Constants.BLACK_KING){
                cb.blackKingPosition[1] = destRank;
            }

            if(Character.toUpperCase(cb.board[locRank][file]) == Constants.WHITE_PAWN || cb.board[destRank][file]!=Constants.EMPTY_SQUARE){
                cb.fenParts[11] = "0";
            }else{
                cb.fenParts[11] = Integer.toString(Integer.parseInt(cb.fenParts[11])+1);
            }

            if(locRank == 0 || locRank == 7){
                switch(cb.board[locRank][file]){
                    case Constants.BLACK_ROOK:{
                        if(file==0 && cb.fenParts[9].contains(String.valueOf(Constants.BLACK_QUEEN))){
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.BLACK_QUEEN),"");
                        }else if(file==7 && cb.fenParts[9].contains(String.valueOf(Constants.BLACK_KING))) {
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.BLACK_KING), "");
                        }
                        break;
                    }
                    case Constants.WHITE_ROOK:{
                        if(file==0 && cb.fenParts[9].contains(String.valueOf(Constants.WHITE_QUEEN))){
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.WHITE_QUEEN),"");
                        }else if(file==7 && cb.fenParts[9].contains(String.valueOf(Constants.WHITE_KING))) {
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.WHITE_KING), "");
                        }
                        break;
                    }
                    case Constants.BLACK_KING:{
                        if(cb.fenParts[9].contains(String.valueOf(Constants.BLACK_QUEEN)) || cb.fenParts[9].contains(String.valueOf(Constants.BLACK_KING))) {
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.BLACK_KING), "").replace(Character.toString(Constants.BLACK_QUEEN), "");
                        }
                        break;
                    }
                    case Constants.WHITE_KING:{
                        if(cb.fenParts[9].contains(String.valueOf(Constants.WHITE_QUEEN)) || cb.fenParts[9].contains(String.valueOf(Constants.WHITE_KING))) {
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.WHITE_KING), "").replace(Character.toString(Constants.WHITE_QUEEN), "");
                        }
                        break;
                    }
                }
            }
            if(destRank == 0 || destRank == 7){
                switch(cb.board[destRank][file]){
                    case Constants.WHITE_ROOK:
                        if(destRank != 7){
                            break;
                        }
                        if(file==0 && cb.fenParts[9].contains(String.valueOf(Constants.WHITE_QUEEN))){
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.WHITE_QUEEN),"");
                        }else if(file==7 && cb.fenParts[9].contains(String.valueOf(Constants.WHITE_KING))){
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.WHITE_KING), "");
                        }
                        break;
                    case Constants.BLACK_ROOK:
                        if(destRank != 0){
                            break;
                        }
                        if(file==0 && cb.fenParts[9].contains(String.valueOf(Constants.BLACK_QUEEN))){
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.BLACK_QUEEN),"");
                        }else if(file==7 && cb.fenParts[9].contains(String.valueOf(Constants.BLACK_KING))) {
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.BLACK_KING), "");
                        }
                        break;
                }

                if(Character.toUpperCase(cb.board[locRank][file]) == Constants.WHITE_PAWN){
                    cb.board[locRank][file] = move.split(Constants.MOVE_SEPARATOR)[Constants.PROMOTION_MOVE_LENGTH-1].charAt(0);
                }

            }

            if(Character.toUpperCase(cb.board[locRank][file]) == Constants.WHITE_PAWN) {
                //double pawn push -> creates en-passant square
                if (locRank == 1 && cb.board[locRank][file] == Constants.BLACK_PAWN && destRank == 3) {
                    cb.fenParts[10] = Constants.FILES.charAt(file) + "6";
                } else if (locRank == 6 && cb.board[locRank][file] == Constants.WHITE_PAWN && destRank == 4) {
                    cb.fenParts[10] = Constants.FILES.charAt(file) + "3";
                } else {
                    cb.fenParts[10] = "-";
                }
            }else{
                cb.fenParts[10] = "-";
            }


            cb.board[destRank][file] = cb.board[locRank][file];
            cb.board[locRank][file] = Constants.EMPTY_SQUARE;
            cb.pieceLocations.add(file + destRank * 8);
            cb.pieceLocations.remove((Object)(file+locRank*8));
            cb.fenParts[locRank] = FenUtils.getRank(cb.board[locRank]);
            cb.fenParts[destRank] = FenUtils.getRank(cb.board[destRank]);
            cb.turn  = cb.turn  == Constants.WHITE?Constants.BLACK:Constants.WHITE;
            cb.fenParts[8] = Character.toString(cb.turn);

        }else if(move.contains(Constants.EN_PASSANT_NOTATION)){
            int locFile = Integer.parseInt(String.valueOf(move.charAt(0)));
            int destFile = Integer.parseInt(String.valueOf(move.charAt(2)));
            int locRank = Integer.parseInt(String.valueOf(move.charAt(1)));
            int destRank = Integer.parseInt(String.valueOf(move.charAt(3)));
            switch(cb.board[locRank][locFile]){
                case Constants.WHITE_PAWN:{
                    cb.board[locRank][destFile] = Constants.EMPTY_SQUARE;
                    cb.pieceLocations.remove((Object)(destFile+locRank*8));
                    break;
                }
                case Constants.BLACK_PAWN:{
                    cb.board[locRank][destFile] = Constants.EMPTY_SQUARE;
                    cb.pieceLocations.remove((Object)(destFile+locRank*8));
                }
            }
            cb.board[destRank][destFile] = cb.board[locRank][locFile];
            cb.board[locRank][locFile] = Constants.EMPTY_SQUARE;
            cb.pieceLocations.add(destFile + destRank * 8);
            cb.pieceLocations.remove((Object)(locFile+locRank*8));
            cb.fenParts[locRank] = FenUtils.getRank(cb.board[locRank]);
            cb.fenParts[destRank] = FenUtils.getRank(cb.board[destRank]);
            cb.turn  = cb.turn  == Constants.WHITE?Constants.BLACK:Constants.WHITE;
            cb.fenParts[10] = "-";
            cb.fenParts[8] = Character.toString(cb.turn);
            cb.fenParts[11] = "0";
        }

        else{
            int locFile = Integer.parseInt(String.valueOf(move.charAt(0)));
            int destFile = Integer.parseInt(String.valueOf(move.charAt(2)));
            int locRank = Integer.parseInt(String.valueOf(move.charAt(1)));
            int destRank = Integer.parseInt(String.valueOf(move.charAt(3)));
            if(cb.board[locRank][locFile] == Constants.WHITE_KING){
                cb.whiteKingPosition[0] = destFile;
                cb.whiteKingPosition[1] = destRank;
            }else if(cb.board[locRank][locFile] == Constants.BLACK_KING){
                cb.blackKingPosition[0] = destFile;
                cb.blackKingPosition[1] = destRank;
            }
            if(Character.toUpperCase(cb.board[locRank][locFile]) == Constants.WHITE_PAWN || cb.board[destRank][destFile]!=Constants.EMPTY_SQUARE){
                cb.fenParts[11] = "0";
            }else{
                cb.fenParts[11] = Integer.toString(Integer.parseInt(cb.fenParts[11])+1);
            }



            if(locRank == 0 || locRank == 7){
                switch(cb.board[locRank][locFile]){
                    case Constants.BLACK_KING:{
                        if(cb.fenParts[9].contains(String.valueOf(Constants.BLACK_QUEEN)) || cb.fenParts[9].contains(String.valueOf(Constants.BLACK_KING))) {
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.BLACK_KING), "").replace(Character.toString(Constants.BLACK_QUEEN), "");
                        }
                        break;
                    }
                    case Constants.WHITE_KING:{
                        if(cb.fenParts[9].contains(String.valueOf(Constants.WHITE_QUEEN)) || cb.fenParts[9].contains(String.valueOf(Constants.WHITE_KING))) {
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.WHITE_KING), "").replace(Character.toString(Constants.WHITE_QUEEN), "");
                        }
                        break;
                    }
                }
            }
            if(destRank == 0 || destRank == 7){
                switch(cb.board[destRank][destFile]){
                    case Constants.WHITE_ROOK:
                        if(destRank != 7){
                            break;
                        }
                        if(destFile==0 && cb.fenParts[9].contains(String.valueOf(Constants.WHITE_QUEEN))){
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.WHITE_QUEEN),"");
                        }else if(destFile==7 && cb.fenParts[9].contains(String.valueOf(Constants.WHITE_KING))){
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.WHITE_KING), "");
                        }
                        break;
                    case Constants.BLACK_ROOK:
                        if(destRank != 0){
                            break;
                        }
                        if(destFile==0 && cb.fenParts[9].contains(String.valueOf(Constants.BLACK_QUEEN))){
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.BLACK_QUEEN),"");
                        }else if(destFile==7 && cb.fenParts[9].contains(String.valueOf(Constants.BLACK_KING))) {
                            cb.fenParts[9] = cb.fenParts[9].replace(Character.toString(Constants.BLACK_KING), "");
                        }
                        break;
                }
                if(Character.toUpperCase(cb.board[locRank][locFile]) == Constants.WHITE_PAWN){
                    try {
                        cb.board[locRank][locFile] = move.split(Constants.MOVE_SEPARATOR)[Constants.PROMOTION_MOVE_LENGTH - 1].charAt(0);
                    }catch(Exception e){
                        System.out.println(move);
                    }
                }
            }


            cb.board[destRank][destFile] = cb.board[locRank][locFile];
            cb.board[locRank][locFile] = Constants.EMPTY_SQUARE;
            cb.pieceLocations.add(destFile + destRank * 8);
            cb.pieceLocations.remove((Object)(locFile+locRank*8));
            cb.fenParts[locRank] = FenUtils.getRank(cb.board[locRank]);
            cb.fenParts[destRank] = FenUtils.getRank(cb.board[destRank]);
            cb.turn  = cb.turn  == Constants.WHITE?Constants.BLACK:Constants.WHITE;
            cb.fenParts[10] = "-";
            cb.fenParts[8] = Character.toString(cb.turn);
        }
        if(cb.fenParts[9].equals(" ")||cb.fenParts[9].equals("")){
            cb.fenParts[9] = "-";
        }
        if(cb.turn == Constants.WHITE) {
            cb.fenParts[12] = Integer.toString(Integer.parseInt(cb.fenParts[12]) + 1);
        }

        cb.checkBoard();

    }
    public void undoMove(String move){
        if(move.contains(Constants.QUEEN_SIDE_CASTLING)){
            //System.out.println(cvt(move));
            cb.turn  = cb.turn  == Constants.WHITE?Constants.BLACK:Constants.WHITE;
            int rank = cb.turn == Constants.WHITE?7:0;
            cb.board[rank][4] = cb.board[rank][2];
            cb.board[rank][2] = Constants.EMPTY_SQUARE;
            cb.board[rank][0] = cb.board[rank][3];
            cb.board[rank][3] = Constants.EMPTY_SQUARE;
            cb.pieceLocations.add(4 + rank * 8);
            cb.pieceLocations.remove((Object)(2 + rank * 8));
            cb.pieceLocations.add(0 + rank * 8);
            cb.pieceLocations.remove((Object)(3 + rank * 8));
            if(cb.turn == Constants.WHITE){
                cb.whiteKingPosition[0] = 4;
            }else{
                cb.blackKingPosition[0] = 4;
            }
            cb.fenParts[rank] = FenUtils.getRank(cb.board[rank]);
            String[] moveParts = move.split(Constants.MOVE_SEPARATOR);
            cb.fenParts[9] = moveParts[1];
            cb.fenParts[10] = moveParts[2];
            cb.fenParts[8] = Character.toString(cb.turn);
            cb.fenParts[11] = Integer.toString(Integer.parseInt(moveParts[Constants.CASTLING_MOVE_LENGTH-1]));
        }else if(move.contains(Constants.KING_SIDE_CASTLING)){
            cb.turn  = cb.turn  == Constants.WHITE?Constants.BLACK:Constants.WHITE;
            int rank = cb.turn == Constants.WHITE?7:0;
            cb.board[rank][4] = cb.board[rank][6];
            cb.board[rank][6] = Constants.EMPTY_SQUARE;
            cb.board[rank][7] = cb.board[rank][5];
            cb.board[rank][5] = Constants.EMPTY_SQUARE;
            cb.pieceLocations.add(4 + rank * 8);
            cb.pieceLocations.remove((Object)(6 + rank * 8));
            cb.pieceLocations.add(7 + rank * 8);
            cb.pieceLocations.remove((Object)(5 + rank * 8));
            if(cb.turn == Constants.WHITE){
                cb.whiteKingPosition[0] = 4;
            }else{
                cb.blackKingPosition[0] = 4;
            }
            cb.fenParts[rank] = FenUtils.getRank(cb.board[rank]);

            String[] moveParts = move.split(Constants.MOVE_SEPARATOR);

            cb.fenParts[9] = moveParts[1];
            cb.fenParts[10] = moveParts[2];
            cb.fenParts[8] = Character.toString(cb.turn);
            cb.fenParts[11] = Integer.toString(Integer.parseInt(moveParts[Constants.CASTLING_MOVE_LENGTH-1]));
        }else if(move.charAt(1) == move.charAt(3)){
            int rank = Integer.parseInt(String.valueOf(move.charAt(1)));
            int locFile = Integer.parseInt(String.valueOf(move.charAt(2)));
            int destFile = Integer.parseInt(String.valueOf(move.charAt(0)));

            switch(cb.board[rank][locFile]){
                case Constants.WHITE_KING :
                    cb.whiteKingPosition[0] = destFile;
                    cb.whiteKingPosition[1] = rank;
                    break;
                case Constants.BLACK_KING:
                    cb.blackKingPosition[0] = destFile;
                    cb.blackKingPosition[1] = rank;
                    break;

            }

            String[] moveParts = move.split(Constants.MOVE_SEPARATOR);


            cb.fenParts[10] = moveParts[3];

            if(rank == 0 || rank == 7){
                switch(cb.board[rank][locFile]){
                    case Constants.WHITE_ROOK:

                    case Constants.WHITE_KING:

                    case Constants.BLACK_ROOK:

                    case Constants.BLACK_KING:
                        cb.fenParts[9] = moveParts[2];
                }

            }

            cb.board[rank][destFile] = cb.board[rank][locFile];
            cb.board[rank][locFile] = moveParts[1].charAt(0);
            cb.pieceLocations.add(destFile + rank * 8);
            if(moveParts[1].charAt(0) == Constants.EMPTY_SQUARE) {
                cb.pieceLocations.remove((Object)(locFile + rank * 8));
            }
            cb.fenParts[11] = Integer.toString(Integer.parseInt(moveParts[Constants.NORMAL_MOVE_LENGTH-1]));
            cb.fenParts[rank] = FenUtils.getRank(cb.board[rank]);
            cb.turn  = cb.turn  == Constants.WHITE?Constants.BLACK:Constants.WHITE;
            cb.fenParts[8] = Character.toString(cb.turn);

        }else if(move.charAt(0) == move.charAt(2)){
            int file = Integer.parseInt(String.valueOf(move.charAt(0)));
            int locRank = Integer.parseInt(String.valueOf(move.charAt(3)));
            int destRank = Integer.parseInt(String.valueOf(move.charAt(1)));
            String[] moveParts = move.split(Constants.MOVE_SEPARATOR);
            cb.turn  = cb.turn  == Constants.WHITE?Constants.BLACK:Constants.WHITE;
            switch(cb.board[locRank][file]){
                case Constants.WHITE_KING :
                    cb.whiteKingPosition[0] = file;
                    cb.whiteKingPosition[1] = destRank;
                    break;
                case Constants.BLACK_KING:
                    cb.blackKingPosition[0] = file;
                    cb.blackKingPosition[1] = destRank;
                    break;

            }

            cb.fenParts[10] = moveParts[3];

            if(locRank == 0 || locRank == 7){
                if(moveParts.length==Constants.PROMOTION_MOVE_LENGTH){
                    switch(cb.turn){
                        case Constants.WHITE:
                            cb.board[locRank][file] = Constants.WHITE_PAWN;
                            break;
                        case Constants.BLACK:
                            cb.board[locRank][file] = Constants.BLACK_PAWN;
                            break;
                    }
                }

            }
            //position startpos move d2d3 e7e6 c1f4 f8b4 d1d2 b8c6 b1c3
            //go perft 2
            if(moveParts.length==Constants.PROMOTION_MOVE_LENGTH){
                cb.fenParts[11] = moveParts[Constants.PROMOTION_MOVE_LENGTH-2];
            }else{
                cb.fenParts[11] = moveParts[Constants.NORMAL_MOVE_LENGTH-1];
            }
            cb.board[destRank][file] = cb.board[locRank][file];
            cb.board[locRank][file] = moveParts[1].charAt(0);
            cb.pieceLocations.add(file + destRank * 8);
            if(moveParts[1].charAt(0) == Constants.EMPTY_SQUARE) {
                cb.pieceLocations.remove((Object)(file+locRank*8));
            }

            cb.fenParts[locRank] = FenUtils.getRank(cb.board[locRank]);
            cb.fenParts[destRank] = FenUtils.getRank(cb.board[destRank]);
            cb.fenParts[8] = Character.toString(cb.turn);
            cb.fenParts[9] = moveParts[2];

        }
        else if(move.contains(Constants.EN_PASSANT_NOTATION)){
            int locFile = Integer.parseInt(String.valueOf(move.charAt(2)));
            int destFile = Integer.parseInt(String.valueOf(move.charAt(0)));
            int locRank = Integer.parseInt(String.valueOf(move.charAt(3)));
            int destRank = Integer.parseInt(String.valueOf(move.charAt(1)));
            switch(cb.board[locRank][locFile]){
                case Constants.WHITE_PAWN:{
                    cb.board[destRank][locFile] = Constants.BLACK_PAWN;
                    break;
                }
                case Constants.BLACK_PAWN:{
                    cb.board[destRank][locFile] = Constants.WHITE_PAWN;
                }
            }
            cb.pieceLocations.add((locFile+destRank*8));
            cb.board[destRank][destFile] = cb.board[locRank][locFile];
            cb.board[locRank][locFile] = Constants.EMPTY_SQUARE;
            cb.pieceLocations.add(destFile + destRank * 8);
            cb.pieceLocations.remove((Object)(locFile+locRank*8));
            cb.fenParts[locRank] = FenUtils.getRank(cb.board[locRank]);
            cb.fenParts[destRank] = FenUtils.getRank(cb.board[destRank]);
            cb.turn  = cb.turn  == Constants.WHITE?Constants.BLACK:Constants.WHITE;
            cb.fenParts[10] = move.split(Constants.MOVE_SEPARATOR)[3];
            cb.fenParts[11] = Integer.toString(Integer.parseInt(cb.fenParts[11])-1);
            cb.fenParts[8] = Character.toString(cb.turn);
            cb.fenParts[11] = Integer.toString(Integer.parseInt(move.split(Constants.MOVE_SEPARATOR)[Constants.EN_PASSANT_MOVE_LENGTH-2]));
        }else{
            int locFile = Integer.parseInt(String.valueOf(move.charAt(2)));
            int destFile = Integer.parseInt(String.valueOf(move.charAt(0)));
            int locRank = Integer.parseInt(String.valueOf(move.charAt(3)));
            int destRank = Integer.parseInt(String.valueOf(move.charAt(1)));
            cb.turn  = cb.turn  == Constants.WHITE?Constants.BLACK:Constants.WHITE;
            switch(cb.board[locRank][locFile]){
                case Constants.WHITE_KING :
                    cb.whiteKingPosition[0] = destFile;
                    cb.whiteKingPosition[1] = destRank;
                    break;
                case Constants.BLACK_KING:
                    cb.blackKingPosition[0] = destFile;
                    cb.blackKingPosition[1] = destRank;
                    break;

            }

            String[] moveParts = move.split(Constants.MOVE_SEPARATOR);

            cb.fenParts[10] = moveParts[3];

            if(locRank == 0 || locRank == 7){
                if(moveParts.length==Constants.PROMOTION_MOVE_LENGTH){
                    switch(cb.turn){
                        case Constants.WHITE:
                            cb.board[locRank][locFile] = Constants.WHITE_PAWN;
                            break;
                        case Constants.BLACK:
                            cb.board[locRank][locFile] = Constants.BLACK_PAWN;
                            break;
                    }
                }
            }
            if(moveParts.length==Constants.PROMOTION_MOVE_LENGTH){
                cb.fenParts[11] = moveParts[Constants.PROMOTION_MOVE_LENGTH-2];
            }else{
                cb.fenParts[11] = moveParts[Constants.NORMAL_MOVE_LENGTH-1];
            }
            cb.board[destRank][destFile] = cb.board[locRank][locFile];
            cb.board[locRank][locFile] = moveParts[1].charAt(0);
            cb.pieceLocations.add(destFile + destRank * 8);
            if(moveParts[1].charAt(0) == Constants.EMPTY_SQUARE) {
                cb.pieceLocations.remove((Object) (locFile + locRank * 8));
            }
            cb.fenParts[locRank] = FenUtils.getRank(cb.board[locRank]);
            cb.fenParts[destRank] = FenUtils.getRank(cb.board[destRank]);
            cb.fenParts[8] = Character.toString(cb.turn);
            cb.fenParts[9] = moveParts[2];

        }


        if(cb.turn == Constants.BLACK) {
            cb.fenParts[12] = Integer.toString(Integer.parseInt(cb.fenParts[12]) - 1);
        }
        cb.checkBoard();
    }

    //ends here





    public ArrayList<String> generateMove(int file, int rank){
        moves.clear();
        switch(Character.toUpperCase(cb.board[rank][file])){
            case Constants.WHITE_KING:
                return king(file,rank);
            case Constants.WHITE_PAWN:
                return pawn(file,rank);
            case Constants.WHITE_ROOK:
                return rook(file,rank);
            case Constants.WHITE_BISHOP:
                return bishop(file,rank);
            case Constants.WHITE_KNIGHT:
                return knight(file,rank);
            case Constants.WHITE_QUEEN:
                return queen(file,rank);
            default:
                return null;
        }
    }

    public ArrayList<String> king(final int file,final int rank){
        if(cb.gs == GameState.CHECK){
            ArrayList<int[]> checkDirections = new ArrayList<>();
            for(Integer checkerIndex:cb.checkers.keySet()){
                int checkerFile = checkerIndex%8;
                int checkerRank = checkerIndex/8;

                switch(Util.toUpper(cb.board[checkerRank][checkerFile])){
                    case Constants.WHITE_KNIGHT:
                        checkDirections.add(Constants.KNIGHT_DIRECTION[cb.checkers.get(checkerIndex)]);
                        break;
                    default:
                        checkDirections.add(Constants.ALL_DIRECTIONS[cb.checkers.get(checkerIndex)]);
                }
            }

            for(int[] direction:Constants.ALL_DIRECTIONS){
                int newFile = file+direction[0];
                int newRank = rank+direction[1];
                if(!Util.isValid(newFile,newRank)){
                    continue;
                }

                if(checkDirections.contains(direction) && !cb.checkers.containsKey(file+direction[0]+(rank+direction[1])*8)){
                    continue;
                }

                if(cb.board[newRank][newFile] != Constants.EMPTY_SQUARE&&Util.isAlly(cb.board[rank][file],cb.board[newRank][newFile])){
                    continue;
                }

//                if(!cb.attackedSquares.contains(newFile+newRank*8)){
//                    moves.add(Util.cvtMove(file,rank,newFile,newRank,cb.board,cb.fenParts));
//                }

                char king = cb.board[rank][file];

                cb.board[rank][file] = Constants.EMPTY_SQUARE;

                if(!cb.squareUnderAttack(newFile,newRank)){
                    moves.add(Util.cvtMove(file,rank,newFile,newRank,cb.board,cb.fenParts));
                }

                cb.board[rank][file] = king;

            }
            return moves;
        }
        for(int[] direction:Constants.ALL_DIRECTIONS){
            int newFile = file+direction[0];
            int newRank = rank+direction[1];
            if(!Util.isValid(newFile,newRank)){
                continue;
            }
            if(cb.board[newRank][newFile] != Constants.EMPTY_SQUARE&&Util.isAlly(cb.board[rank][file],cb.board[newRank][newFile])){
                continue;
            }
//            if(!cb.attackedSquares.contains(newFile+newRank*8)){
//                moves.add(Util.cvtMove(file,rank,newFile,newRank,cb.board,cb.fenParts));
//            }

            if(!cb.squareUnderAttack(newFile,newRank)){
//                if(cb.board[rank][file] == Constants.WHITE_KING) {
//                    System.out.println(Util.cvtMove(file,rank,newFile,newRank,cb.board,cb.fenParts));
//                }
                moves.add(Util.cvtMove(file,rank,newFile,newRank,cb.board,cb.fenParts));
            }
        }
        // castling
        boolean kingSide,queenSide;
        if(cb.turn == Constants.WHITE){
            kingSide = cb.fenParts[9].contains(Character.toString(Constants.WHITE_KING));
            queenSide = cb.fenParts[9].contains(Character.toString(Constants.WHITE_QUEEN));
        }else{
            kingSide = cb.fenParts[9].contains(Character.toString(Constants.BLACK_KING));
            queenSide = cb.fenParts[9].contains(Character.toString(Constants.BLACK_QUEEN));
        }

        kingSide = kingSide && cb.board[rank][5] == Constants.EMPTY_SQUARE && cb.board[rank][6] == Constants.EMPTY_SQUARE && !cb.squareUnderAttack(5,rank) && !cb.squareUnderAttack(6,rank);
//        if(rank == 0) {
//            System.out.println("queen side check");
//            System.out.println(cb.fenParts[9].contains(Character.toString(Constants.BLACK_QUEEN)));
//            System.out.println(cb.board[rank][1] == Constants.EMPTY_SQUARE);
//            System.out.println(cb.board[rank][2] == Constants.EMPTY_SQUARE);
//            System.out.println(cb.board[rank][3] == Constants.EMPTY_SQUARE);
//            System.out.println(!cb.squareUnderAttack(1,rank));
//            System.out.println(!cb.squareUnderAttack(2,rank));
//            System.out.println(!cb.squareUnderAttack(3,rank));
//        }
        queenSide = queenSide && cb.board[rank][3] == Constants.EMPTY_SQUARE && cb.board[rank][2] == Constants.EMPTY_SQUARE && cb.board[rank][1] == Constants.EMPTY_SQUARE && !cb.squareUnderAttack(3,rank) && !cb.squareUnderAttack(2,rank) ;
        if(kingSide){
            moves.add(Util.constructCastlingMove(Constants.KING_SIDE_CASTLING,cb.fenParts));
        }
        if(queenSide){
            moves.add(Util.constructCastlingMove(Constants.QUEEN_SIDE_CASTLING,cb.fenParts));
        }

//        if(kingSide){
//            for(int i=file+1;kingSide&&i<file+3;i++){
//                kingSide = cb.board[rank][i] == Constants.EMPTY_SQUARE && !cb.squareUnderAttack(i,rank);
//            }
//
//            if(kingSide){
//                moves.add(Util.constructCastlingMove(Constants.QUEEN_SIDE_CASTLING,cb.fenParts));
//            }
//        }
//        if(queenSide){
//            for(int i=file-1;queenSide&&i>file-3;i--){
//                queenSide = cb.board[rank][i] == Constants.EMPTY_SQUARE&& !cb.squareUnderAttack(i,rank);
//            }
//            if(queenSide){
//                moves.add(Util.constructCastlingMove(Constants.QUEEN_SIDE_CASTLING,cb.fenParts));
//            }
//        }
        return moves;
    }

    public ArrayList<String> pawn(int file,int rank){
        int pinnedIndex = file + rank * 8;
        boolean pinned = cb.pinnedPieces.containsKey(pinnedIndex);
        if(pinned){
            if(cb.gs == GameState.CHECK){
                return moves; // pinned pieces cannot resolve check
            }
            int[] pinDirection = Constants.ALL_DIRECTIONS[cb.pinnedPieces.get(pinnedIndex)];
            if(pinDirection[0] != 0 ){
                if(pinDirection[1] != 0){
                    // only possible move is to capture the pinner
                    if((Util.isUpperCase(cb.board[rank][file]) && pinDirection[1] == -1) || (!Util.isUpperCase(cb.board[rank][file]) && pinDirection[1] == 1)){
                        int df = file + pinDirection[0],dr = rank + pinDirection[1];
                        if(cb.board[dr][df] != Constants.EMPTY_SQUARE){
                            if(dr == 0 || dr == 7) {
                                switch (cb.turn) {
                                    case Constants.WHITE:
                                        moves.add(Util.cvtMove(file, rank, df, dr, cb.board, cb.fenParts) + Constants.MOVE_SEPARATOR + Constants.WHITE_QUEEN);
                                        moves.add(Util.cvtMove(file, rank, df, dr, cb.board, cb.fenParts) + Constants.MOVE_SEPARATOR + Constants.WHITE_KNIGHT);
                                        moves.add(Util.cvtMove(file, rank, df, dr, cb.board, cb.fenParts) + Constants.MOVE_SEPARATOR + Constants.WHITE_ROOK);
                                        moves.add(Util.cvtMove(file, rank, df, dr, cb.board, cb.fenParts) + Constants.MOVE_SEPARATOR + Constants.WHITE_BISHOP);
                                        break;
                                    case Constants.BLACK:
                                        moves.add(Util.cvtMove(file, rank, df, dr, cb.board, cb.fenParts) + Constants.MOVE_SEPARATOR + Constants.BLACK_QUEEN);
                                        moves.add(Util.cvtMove(file, rank, df, dr, cb.board, cb.fenParts) + Constants.MOVE_SEPARATOR + Constants.BLACK_KNIGHT);
                                        moves.add(Util.cvtMove(file, rank, df, dr, cb.board, cb.fenParts) + Constants.MOVE_SEPARATOR + Constants.BLACK_ROOK);
                                        moves.add(Util.cvtMove(file, rank, df, dr, cb.board, cb.fenParts) + Constants.MOVE_SEPARATOR + Constants.BLACK_BISHOP);
                                        break;
                                }
                            }else {
                                moves.add(Util.cvtMove(file, rank, df, dr, cb.board, cb.fenParts));
                            }
                        }
                    }


                    if(cb.fenParts[10].equals("-")){
                        // do nothing
                    }else{
                        int dr = cb.turn == Constants.WHITE?2:5;
                        if(Math.abs(file-Constants.FILES.indexOf(cb.fenParts[10].charAt(0))) == 1){
                            if((!Util.isUpperCase(cb.board[rank][file]) && rank == 4)||(Util.isUpperCase(cb.board[rank][file]) && rank == 3)){
                                moves.add(Util.cvtMove(file, rank, Constants.FILES.indexOf(cb.fenParts[10].charAt(0)), dr,cb.board,cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.EN_PASSANT_NOTATION);
                            }
                        }
                    }

                }
//                System.out.println("here");
                return moves;
            }else{
                // generate pushes, which is done below
            }
        }else if(cb.gs == GameState.CHECK){
            if(cb.checkers.size()>1){
                return moves; // a two-way check cannot be resolved without the king moving to a safe square
            }
            int checkerIndex=0;
            for(Integer i:cb.checkers.keySet()){
                checkerIndex = i;
            }
            int checkerFile = checkerIndex % 8;
            int checkerRank = checkerIndex / 8;

            //capture code
            if(Math.abs(file-checkerFile) == 1) {
                if ((cb.turn == Constants.WHITE &&rank-checkerRank==1) || (cb.turn == Constants.BLACK &&rank-checkerRank==-1)){
                    if(checkerRank == 0 || checkerRank == 7){
                        if(cb.turn==Constants.WHITE){
                            moves.add(Util.cvtMove(file, rank, checkerFile, checkerRank, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.WHITE_QUEEN);
                            moves.add(Util.cvtMove(file, rank, checkerFile, checkerRank, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.WHITE_ROOK);
                            moves.add(Util.cvtMove(file, rank, checkerFile, checkerRank, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.WHITE_KNIGHT);
                            moves.add(Util.cvtMove(file, rank, checkerFile, checkerRank, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.WHITE_BISHOP);
                        }else{
                            moves.add(Util.cvtMove(file, rank, checkerFile, checkerRank, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.BLACK_QUEEN);
                            moves.add(Util.cvtMove(file, rank, checkerFile, checkerRank, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.BLACK_ROOK);
                            moves.add(Util.cvtMove(file, rank, checkerFile, checkerRank, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.BLACK_KNIGHT);
                            moves.add(Util.cvtMove(file, rank, checkerFile, checkerRank, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.BLACK_BISHOP);
                        }
                    }else {
                        moves.add(Util.cvtMove(file, rank, checkerFile, checkerRank, cb.board, cb.fenParts));
                    }
                }
            }

            switch(Character.toUpperCase(cb.board[checkerRank][checkerFile])){
                case Constants.WHITE_KNIGHT:
                case Constants.WHITE_PAWN:
                    //no way to block the check
                    break;
                default:
                    int[] kingPosition = cb.kingPosition();
                    int[] checkDirection = Constants.ALL_DIRECTIONS[cb.checkers.get(checkerIndex)];

                    if(checkDirection[0] == 0){
                        //available move is capturing the checker
                    }else if(checkDirection[1] == 0){
                        if(Util.inBetween(checkerFile,kingPosition[0],file)) {
                            if (cb.turn == Constants.WHITE && rank>checkerRank) {
                                if ((rank == 6 && rank - checkerRank<3) || ((rank - checkerRank<2))){
                                    for(int i=rank-1;i>=checkerRank;i--){
                                        if(cb.board[i][file] != Constants.EMPTY_SQUARE){
                                            break;
                                        } else if(i==checkerRank){
                                            if(i==0){
                                                moves.add(Util.cvtMove(file, rank, file, i, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.WHITE_QUEEN);
                                                moves.add(Util.cvtMove(file, rank, file, i, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.WHITE_ROOK);
                                                moves.add(Util.cvtMove(file, rank, file, i, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.WHITE_KNIGHT);
                                                moves.add(Util.cvtMove(file, rank, file, i, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.WHITE_BISHOP);
                                            }else {
                                                moves.add(Util.cvtMove(file, rank, file, i, cb.board, cb.fenParts));
                                            }
                                            break;
                                        }
                                    }
                                }
                            } else if(cb.turn == Constants.BLACK && rank<checkerRank) {
                                if ((rank == 1 && checkerRank-rank  <3) || ((checkerRank-rank<2))){

                                    for(int i=rank+1;i<=checkerRank;i++){
                                        if(cb.board[i][file] != Constants.EMPTY_SQUARE){
                                            break;
                                        } else if(i==checkerRank){
                                            if(i==7){
                                                moves.add(Util.cvtMove(file, rank, file, i, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.BLACK_QUEEN);
                                                moves.add(Util.cvtMove(file, rank, file, i, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.BLACK_ROOK);
                                                moves.add(Util.cvtMove(file, rank, file, i, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.BLACK_KNIGHT);
                                                moves.add(Util.cvtMove(file, rank, file, i, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.BLACK_BISHOP);
                                            }else {
                                                moves.add(Util.cvtMove(file, rank, file, i, cb.board, cb.fenParts));
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }else{
                        if(Util.inBetween(checkerFile,kingPosition[0],file)){
                            int f=kingPosition[0];
                            int r=kingPosition[1];
                            int dstRank=0;
                            while((f+=checkDirection[0])!=checkerFile&&(r+=checkDirection[1])!=checkerRank){
                                if(f == file){
                                    dstRank = r;
                                    break;
                                }
                            }
                            if (cb.turn == Constants.WHITE && rank>dstRank) {
                                if ((rank == 6 && rank - dstRank<3) || ((rank - dstRank<2))){
                                    for(int i=rank-1;i>=dstRank;i--){
                                        if(cb.board[i][file] != Constants.EMPTY_SQUARE){
                                            break;
                                        } else if(i==dstRank){
                                                moves.add(Util.cvtMove(file, rank, file, i, cb.board, cb.fenParts));
                                            break;
                                        }
                                    }
                                }
                            } else if(cb.turn == Constants.BLACK && rank<dstRank) {
                                if ((rank == 1 && dstRank-rank  <3) || ((dstRank-rank<2))){
                                    for(int i=rank+1;i<=dstRank;i++){
                                        if(cb.board[i][file] != Constants.EMPTY_SQUARE){
                                            break;
                                        } else if(i==dstRank){
                                                moves.add(Util.cvtMove(file, rank, file, i, cb.board, cb.fenParts));
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }


//                    if(Util.inBetween(checkerFile,kingPosition[0],file)){
//                        int f=kingPosition[0];
//                        int r=kingPosition[1];
//                        while((f+=checkDirection[0])!=checkerFile&&(r+=checkDirection[1])!=checkerRank){
//                            boolean hasPotential = (cb.turn==Constants.WHITE&&rank>r&&((rank==6&&rank-r<=3)||(rank!=6&&rank-r<3)))||(cb.turn==Constants.BLACK&&rank<r&&((rank==1&&r-rank<=3)||(rank!=1&&r-rank<3)));
//                            if(!hasPotential){
//                                continue;
//                            }
//                            int limit;
//                            if(cb.turn==Constants.BLACK){
//                                limit = rank == 1?2:1;
//                                for(int currentRank=rank+1;currentRank<=rank+limit;currentRank++){
//                                    if(cb.board[currentRank][file]!=Constants.EMPTY_SQUARE){
//                                        break;
//                                    }
//                                    if(f==file&&currentRank==r){
//                                        if(r == 7){
//                                            moves.add(Util.cvtMove(file, rank, file, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.BLACK_QUEEN);
//                                            moves.add(Util.cvtMove(file, rank, file, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.BLACK_ROOK);
//                                            moves.add(Util.cvtMove(file, rank, file, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.BLACK_KNIGHT);
//                                            moves.add(Util.cvtMove(file, rank, file, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.BLACK_BISHOP);
//                                        }else{
//                                            moves.add(Util.cvtMove(file, rank, file, r, cb.board, cb.fenParts));
//                                        }
//                                        break;
//                                    }
//                                }
//                            }else{
//                                limit = rank == 6?2:1;
//
//                                for(int currentRank=rank-1;currentRank>=rank-limit;currentRank--){
//                                    if(cb.board[currentRank][file]!=Constants.EMPTY_SQUARE){
//                                        break;
//                                    }
//                                    if(f==file&&currentRank==r){
//                                        if(r == 0){
//                                            moves.add(Util.cvtMove(file, rank, file, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.WHITE_QUEEN);
//                                            moves.add(Util.cvtMove(file, rank, file, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.WHITE_ROOK);
//                                            moves.add(Util.cvtMove(file, rank, file, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.WHITE_KNIGHT);
//                                            moves.add(Util.cvtMove(file, rank, file, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.WHITE_BISHOP);
//                                        }else{
//                                            moves.add(Util.cvtMove(file, rank, file, r, cb.board, cb.fenParts));
//                                        }
//                                        break;
//                                    }
//                                }
//                            }
//                        }
//                    }

                    break;
            }

            return moves;
        }




        // generating pushes
        int f=file,r=rank;
        int startIndex = Util.isUpperCase(cb.board[rank][file])?3:2;
        int endIndex = (Util.isUpperCase(cb.board[rank][file]) && rank == 6) || (!Util.isUpperCase(cb.board[rank][file]) && rank == 1)?2:1;


        for(int i=0;i<endIndex;i++){// needs to be fixed
            //Util.writeToLog(i);
            f += Constants.ALL_DIRECTIONS[startIndex][0];
            r += Constants.ALL_DIRECTIONS[startIndex][1];
            if(Util.isValid(f,r)){

                if(cb.board[r][f] == Constants.EMPTY_SQUARE){
                    if(r == 0 || r == 7){
                        switch(cb.turn){
                            case Constants.WHITE:
                                moves.add(Util.cvtMove(file, rank, f, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.WHITE_QUEEN);
                                moves.add(Util.cvtMove(file, rank, f, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.WHITE_KNIGHT);
                                moves.add(Util.cvtMove(file, rank, f, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.WHITE_ROOK);
                                moves.add(Util.cvtMove(file, rank, f, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.WHITE_BISHOP);
                                break;
                            case Constants.BLACK:
                                moves.add(Util.cvtMove(file, rank, f, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.BLACK_QUEEN);
                                moves.add(Util.cvtMove(file, rank, f, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.BLACK_KNIGHT);
                                moves.add(Util.cvtMove(file, rank, f, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.BLACK_ROOK);
                                moves.add(Util.cvtMove(file, rank, f, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.BLACK_BISHOP);
                                break;
                        }

                    }else {
                        moves.add(Util.cvtMove(file,rank,f,r,cb.board,cb.fenParts));
                    }
                }else {
                    break;
                }

            }else{
                break;
            }


        }


        if(pinned){
            return moves; // a pawn pinned vertically cannot move diagonally
        }

        // generating diagonal moves
        startIndex = Util.isUpperCase(cb.board[rank][file])?6:4;
        endIndex = Util.isUpperCase(cb.board[rank][file])?Constants.ALL_DIRECTIONS.length-1:5;

        for(int i=startIndex;i<=endIndex;i++){
            f = file + Constants.ALL_DIRECTIONS[i][0];
            r = rank + Constants.ALL_DIRECTIONS[i][1];

            if(Util.isValid(f,r)){
                if(cb.board[r][f] != Constants.EMPTY_SQUARE && Util.isEnemyPiece(cb.turn,cb.board[r][f])){

                    if(r == 0 || r == 7){
                        switch(cb.turn){
                            case Constants.WHITE:
                                moves.add(Util.cvtMove(file, rank, f, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.WHITE_QUEEN);
                                moves.add(Util.cvtMove(file, rank, f, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.WHITE_KNIGHT);
                                moves.add(Util.cvtMove(file, rank, f, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.WHITE_ROOK);
                                moves.add(Util.cvtMove(file, rank, f, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.WHITE_BISHOP);
                                break;
                            case Constants.BLACK:
                                moves.add(Util.cvtMove(file, rank, f, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.BLACK_QUEEN);
                                moves.add(Util.cvtMove(file, rank, f, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.BLACK_KNIGHT);
                                moves.add(Util.cvtMove(file, rank, f, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.BLACK_ROOK);
                                moves.add(Util.cvtMove(file, rank, f, r, cb.board, cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.BLACK_BISHOP);
                                break;
                        }
                    }else {
                        moves.add(Util.cvtMove(file,rank,f,r,cb.board,cb.fenParts));
                    }
                }
            }
        }

        if(cb.fenParts[10].equals("-")){
            // do nothing
        }else{
            int dr = cb.turn == Constants.WHITE?2:5;
            if(Math.abs(file-Constants.FILES.indexOf(cb.fenParts[10].charAt(0))) == 1){
                int[] kingPosition = cb.kingPosition();
                int enPassantPawn = Constants.FILES.indexOf(cb.fenParts[10].charAt(0)),direction = Util.getSign(kingPosition[0],file);
                boolean enPassant = true;
                if((!Util.isUpperCase(cb.board[rank][file]) && rank == 4)||(Util.isUpperCase(cb.board[rank][file]) && rank == 3)){
                    if(kingPosition[1] == rank){
                        // possible occurrence https://lichess.org/editor/r2k3r/4p1pp/8/2K1Pp1q/8/8/PP1P1PP1/R7_w_k_-_0_1?color=white
                        // en passant reveals a check
                        // make en-passant move
                        boolean foundAnotherPiece = false;
                        for(int i=file+direction;i!=kingPosition[0];i+=direction){
                            foundAnotherPiece = cb.board[rank][i]!=Constants.EMPTY_SQUARE&&Character.toUpperCase(cb.board[rank][i]) != Constants.WHITE_KING;
                            if(foundAnotherPiece){
                                break;
                            }
                        }
                        if(!foundAnotherPiece){
                            //loop to the other side and see if there is an opponent rook or queen
                            for(int i=enPassantPawn-direction;i>0&&i<8;i-=direction){
                                if(cb.board[rank][i] != Constants.EMPTY_SQUARE){
                                    if(!Util.isAlly(cb.board[rank][file],cb.board[rank][i]) && (Character.toUpperCase(cb.board[rank][i]) == Constants.WHITE_ROOK || Character.toUpperCase(cb.board[rank][i]) == Constants.WHITE_QUEEN)){
                                        //en-passant reveals check
                                        enPassant = false;
                                    }
                                    break;
                                }
                            }
                        }
                        if(enPassant){
                            moves.add(Util.cvtMove(file, rank, Constants.FILES.indexOf(cb.fenParts[10].charAt(0)), dr,cb.board,cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.EN_PASSANT_NOTATION);
                        }
                    }else {
                        moves.add(Util.cvtMove(file, rank, Constants.FILES.indexOf(cb.fenParts[10].charAt(0)), dr,cb.board,cb.fenParts)+Constants.MOVE_SEPARATOR+Constants.EN_PASSANT_NOTATION);
                    }
                }
            }
        }

        return moves;
    }

    public ArrayList<String> queen(final int file,final int rank){//complete
        int pinnedIndex = file + rank * 8;
        if(cb.pinnedPieces.containsKey(pinnedIndex)){
            if(cb.gs == GameState.CHECK){
                return moves; // a pinned piece cannot resolve check
            }
            //Util.writeToLog();
            int[] pinDirection = Constants.ALL_DIRECTIONS[cb.pinnedPieces.get(pinnedIndex)];
            // can move along pinned squares
            boolean foundKing=false,foundEnemyPiece=false;
            int df=file,dr=rank;
            while(!foundEnemyPiece || !foundKing){
                if(!foundEnemyPiece){
                    df += pinDirection[0];
                    dr += pinDirection[1];
                    if(cb.board[dr][df] == Constants.EMPTY_SQUARE) {
                        moves.add(Util.cvtMove(file, rank, df, dr,cb.board,cb.fenParts));
                    }else{
                        foundEnemyPiece = true;
                        moves.add(Util.cvtMove(file, rank, df, dr,cb.board,cb.fenParts));
                        df = file;
                        dr = rank;
                    }
                }else {
                    df -= pinDirection[0];
                    dr -= pinDirection[1];
                    if (cb.board[dr][df] == Constants.EMPTY_SQUARE) {
                        moves.add(Util.cvtMove(file, rank, df, dr, cb.board, cb.fenParts));
                    } else {
                        foundKing = true;
                    }

//                    try {
//                        if (cb.board[dr][df] == Constants.EMPTY_SQUARE) {
//                            moves.add(Util.cvtMove(file, rank, df, dr, cb.board, cb.fenParts));
//                        } else {
//                            foundKing = true;
//                        }
//                    }catch (Exception e){
//                        Util.writeToLog(file);
//                        Util.writeToLog(rank);
//                        Util.writeToLog(cb.board[rank][file]);
//                    }
                }//position startpos move d2d3 e7e6 c1f4 f8b4 d1d2 b8c6 b1c3 a8b8 e1c1 g8h6 c3e4
            }
            return moves;
        }else if(cb.gs == GameState.CHECK){
            if(cb.checkers.size()>1){
                return moves;// a two-way check cannot be resolved without the king moving to a safe square
            }
            int checkerIndex=0;
            for(Integer i:cb.checkers.keySet()){
                checkerIndex = i;
            }
            int checkerFile = checkerIndex % 8;
            int checkerRank = checkerIndex / 8;

            if(cb.canSlide(file,rank,checkerFile,checkerRank)){
                moves.add(Util.cvtMove(file,rank,checkerFile,checkerRank,cb.board,cb.fenParts));
            }

            switch(Character.toUpperCase(cb.board[checkerRank][checkerFile])){
                case Constants.WHITE_PAWN:
                case Constants.WHITE_KNIGHT:
                    break;
                default:
                    int[] kingPosition = cb.kingPosition();
                    int[] checkDirection = Constants.ALL_DIRECTIONS[cb.checkers.get(checkerIndex)];

                    if(checkDirection[0]==0){
                        int r = kingPosition[1];
                        while ((r += checkDirection[1]) != checkerRank) {
                            if(cb.canSlide(file,rank,checkerFile,r)){
                                moves.add(Util.cvtMove(file,rank,checkerFile,r,cb.board,cb.fenParts));
                            }
                        }
                    }else if(checkDirection[1] == 0){
                        int f = kingPosition[0];
                        while ((f += checkDirection[0]) != checkerFile) {
                            if(cb.canSlide(file,rank,f,checkerRank)){
                                moves.add(Util.cvtMove(file,rank,f,checkerRank,cb.board,cb.fenParts));
                            }
                        }
                    }
                    else {
                        int f = kingPosition[0], r = kingPosition[1];
                        while ((f += checkDirection[0]) != checkerFile && (r += checkDirection[1]) != checkerRank) {
                            if(cb.canSlide(file,rank,f,r)){
                                moves.add(Util.cvtMove(file,rank,f,r,cb.board,cb.fenParts));
                            }
                        }
                    }
            }
            return moves;
        }

        // generate normal moves
        int df,dr;
        for(int[] direction:Constants.ALL_DIRECTIONS){
            df = file + direction[0];
            dr = rank + direction[1];
            while(Util.isValid(df,dr)){
                if(cb.board[dr][df] == Constants.EMPTY_SQUARE){
                    moves.add(Util.cvtMove(file,rank,df,dr,cb.board,cb.fenParts));
                }else{
                    if(Util.isAlly(cb.board[rank][file],cb.board[dr][df])){

                    }else{
                        moves.add(Util.cvtMove(file,rank,df,dr,cb.board,cb.fenParts));
                    }
                    break;
                }
                df += direction[0];
                dr += direction[1];
            }
        }
        return moves;
    }

    public ArrayList<String> rook(final int file,final int rank){//complete
        int pinnedIndex = file + rank * 8;
        if(cb.pinnedPieces.containsKey(pinnedIndex)){
            if(cb.gs == GameState.CHECK){
                return moves;
            }
            int[] pinDirection = Constants.ALL_DIRECTIONS[cb.pinnedPieces.get(pinnedIndex)];
            if(pinDirection[0] != 0 && pinDirection[1] != 0){
                // a rook pinned by bishop or diagonally by queen cannot move
            }else{
                // can move along pinned squares
                boolean foundKing=false,foundEnemyPiece=false;
                int df=file,dr=rank;
                while(!foundEnemyPiece || !foundKing){
                    if(!foundEnemyPiece){
                        df += pinDirection[0];
                        dr += pinDirection[1];
                        if(cb.board[dr][df] == Constants.EMPTY_SQUARE) {
                            moves.add(Util.cvtMove(file, rank, df, dr,cb.board,cb.fenParts));
                        }else{
                            foundEnemyPiece = true;
                            moves.add(Util.cvtMove(file, rank, df, dr,cb.board,cb.fenParts));
                            df = file;
                            dr = rank;
                        }
                    }else {
                        df -= pinDirection[0];
                        dr -= pinDirection[1];
                        if(cb.board[dr][df] == Constants.EMPTY_SQUARE) {
                            moves.add(Util.cvtMove(file, rank, df, dr,cb.board,cb.fenParts));
                        }else{
                            foundKing = true;
                        }
                    }
                }
            }
            return moves;
        }else if(cb.gs==GameState.CHECK){
            if(cb.checkers.size()>1){
                return moves;
            }
            int checkerIndex=0;
            for(Integer i:cb.checkers.keySet()){
                checkerIndex = i;
            }
            int checkerFile = checkerIndex % 8;
            int checkerRank = checkerIndex / 8;

            if(file == checkerFile || rank == checkerRank){
                if(cb.canSlide(file,rank,checkerFile,checkerRank)){
                    moves.add(Util.cvtMove(file,rank,checkerFile,checkerRank,cb.board,cb.fenParts));
                }
            }

            switch(Character.toUpperCase(cb.board[checkerRank][checkerFile])){
                case Constants.WHITE_PAWN:
                case Constants.WHITE_KNIGHT:
                    break;
                default:
                    int[] kingPosition = cb.kingPosition();
                    int[] checkDirection = Constants.ALL_DIRECTIONS[cb.checkers.get(checkerIndex)];
                    if(checkDirection[0] == 0){
                        if(Util.inBetween(kingPosition[1],checkerRank,rank)){
                            // means the rook is in between the checker and the king, therefore it might be able to block the check
                            if(cb.canSlide(file,rank,kingPosition[0],rank)){
                                moves.add(Util.cvtMove(file,rank,kingPosition[0],rank,cb.board,cb.fenParts));
                            }
                        }
                    }else if(checkDirection[1] == 0){
                        if(Util.inBetween(kingPosition[0],checkerFile,file)){
                            // means the rook is in between the checker and the king, therefore it might be able to block the check
                            if(cb.canSlide(file,rank,file,kingPosition[1])){
                                moves.add(Util.cvtMove(file,rank,file,kingPosition[1],cb.board,cb.fenParts));
                            }
                        }
                    }else{


                        int f=kingPosition[0],r=kingPosition[1];
                        while((f+=checkDirection[0])!=checkerFile&&(r+=checkDirection[1])!=checkerRank){

                            if(f == file || r == rank){
                                //System.out.println(file+" "+rank+" "+f+" "+r);
                                if(cb.canSlide(file,rank,f,r)){
                                    moves.add(Util.cvtMove(file,rank,f,r,cb.board,cb.fenParts));
                                }
                                //break;
                            }
                        }
                    }
            }
            return moves;
        }

        // generate normal moves
        int df,dr;
        for(int i=0;i<4;i++){
            df = file + Constants.ALL_DIRECTIONS[i][0];
            dr = rank + Constants.ALL_DIRECTIONS[i][1];
            while(Util.isValid(df,dr)){
                if(cb.board[dr][df] == Constants.EMPTY_SQUARE){
                    moves.add(Util.cvtMove(file,rank,df,dr,cb.board,cb.fenParts));
                }else{
                    if(Util.isAlly(cb.board[rank][file],cb.board[dr][df])){

                    }else{
                        moves.add(Util.cvtMove(file,rank,df,dr,cb.board,cb.fenParts));
                    }
                    break;
                }
                df += Constants.ALL_DIRECTIONS[i][0];
                dr += Constants.ALL_DIRECTIONS[i][1];
            }
        }
        return moves;
    }

    public ArrayList<String> knight(final int file,final int rank){//complete
        if(cb.pinnedPieces.containsKey(file + rank * 8)){
            //System.out.println("file "+file);
            //System.out.println("rank "+rank);
            return moves; // a pinned knight cannot move
        }else if(cb.gs == GameState.CHECK){
            if(cb.checkers.size()>1){
                return moves;
            }
            int checkerIndex=0;
            for(Integer i:cb.checkers.keySet()){
                checkerIndex = i;
            }
            int checkerFile = checkerIndex % 8;
            int checkerRank = checkerIndex / 8;

            //capture code
            if(file!=checkerFile&&rank!=checkerRank){
                int[] direction = Util.getDirection(file,rank,checkerFile,checkerRank);
                for(int i=0;i<2;i++){
                    if(file+Constants.KNIGHT_DIRECTION[i][0]*direction[0] == checkerFile && rank+Constants.KNIGHT_DIRECTION[i][1]*direction[1] == checkerRank){
                        moves.add(Util.cvtMove(file,rank,checkerFile,checkerRank,cb.board,cb.fenParts));
                        break;
                    }
                }

            }

            switch(Character.toUpperCase(cb.board[checkerRank][checkerFile])){
                case Constants.WHITE_PAWN:
                case Constants.WHITE_KNIGHT:
                    break;
                default:
                    int[] checkDirection = Constants.ALL_DIRECTIONS[cb.checkers.get(checkerIndex)];
                    int[] kingPosition = cb.kingPosition();
                    if(checkDirection[0] == 0){
                        if(file == checkerFile || Math.abs(file-checkerFile)>2){
                            break;
                        }
                        int r=kingPosition[1];
                        while(moves.size()<2&&(r+=checkDirection[1])!=checkerRank){
                            if(r==rank){
                                continue;
                            }
                            int[] direction = Util.getDirection(file,rank,checkerFile,r);
                            for(int i=0;i<2;i++){
                                if(file+Constants.KNIGHT_DIRECTION[i][0]*direction[0] == checkerFile && rank+Constants.KNIGHT_DIRECTION[i][1]*direction[1] == r){
                                    moves.add(Util.cvtMove(file,rank,checkerFile,r,cb.board,cb.fenParts));
                                    break;
                                }
                            }

                        }
                    }else if(checkDirection[1] == 0){
                        if(rank == checkerRank || Math.abs(rank-checkerRank)>2){
                            break;
                        }
                        int f=kingPosition[0];
                        while(moves.size()<2&&(f+=checkDirection[0])!=checkerFile){
                            if(f==file){
                                continue;
                            }
                            int[] direction = Util.getDirection(file,rank,f,checkerRank);
                            for(int i=0;i<2;i++){
                                if(file+Constants.KNIGHT_DIRECTION[i][0]*direction[0] == f && rank+Constants.KNIGHT_DIRECTION[i][1]*direction[1] == checkerRank){
                                    moves.add(Util.cvtMove(file,rank,f,checkerRank,cb.board,cb.fenParts));
                                    break;
                                }
                            }

                        }
                    }else{
                        int f=kingPosition[0],r=kingPosition[1];
                        while(moves.size()<2&&(f+=checkDirection[0])!=checkerFile&&(r+=checkDirection[1])!=checkerRank){
                            if(f==file || r==rank){
                                continue;
                            }
                            int[] direction = Util.getDirection(file,rank,f,r);
                            for(int i=0;i<2;i++){
                                if(file+Constants.KNIGHT_DIRECTION[i][0]*direction[0] == f && rank+Constants.KNIGHT_DIRECTION[i][1]*direction[1] == r){
                                    moves.add(Util.cvtMove(file,rank,f,r,cb.board,cb.fenParts));
                                    break;
                                }
                            }

                        }
                    }
            }
            return moves;
        }
        int df,dr;
        for(int[] direction:Constants.KNIGHT_DIRECTION){
            df = file + direction[0];
            dr = rank + direction[1];
            if(Util.isValid(df,dr) && !Util.isAlly(cb.board[dr][df],cb.board[rank][file])){
                moves.add(Util.cvtMove(file,rank,df,dr,cb.board,cb.fenParts));
            }
        }

        return moves;
    }

    public ArrayList<String> bishop(final int file,final int rank){//complete!
        int pinnedIndex = file + rank * 8;
        if(cb.pinnedPieces.containsKey(pinnedIndex)){
            if(cb.gs == GameState.CHECK){
                return moves;
            }
            int[] pinDirection = Constants.ALL_DIRECTIONS[cb.pinnedPieces.get(pinnedIndex)];
            if(pinDirection[0] == 0 || pinDirection[1] == 0){
                // a bishop pinned by rook or horizontally by queen cannot move
            }else{
                // can move along pinned squares
                boolean foundKing=false,foundEnemyPiece=false;
                int df=file,dr=rank;
                while(!foundEnemyPiece || !foundKing){
                    if(!foundEnemyPiece){
                        df += pinDirection[0];
                        dr += pinDirection[1];
                        if(cb.board[dr][df] == Constants.EMPTY_SQUARE) {
                            moves.add(Util.cvtMove(file, rank, df, dr,cb.board,cb.fenParts));
                        }else{
                            foundEnemyPiece = true;
                            moves.add(Util.cvtMove(file, rank, df, dr,cb.board,cb.fenParts));
                            df = file;
                            dr = rank;
                        }
                    }else {
                        df -= pinDirection[0];
                        dr -= pinDirection[1];
                        if(cb.board[dr][df] == Constants.EMPTY_SQUARE) {
                            moves.add(Util.cvtMove(file, rank, df, dr,cb.board,cb.fenParts));
                        }else{
                            foundKing = true;
                        }
                    }
                }
            }
            return moves;
        }else if(cb.gs == GameState.CHECK){
            if(cb.checkers.size()>1){
                return moves;
            }
            int checkerIndex=0;
            for(Integer i:cb.checkers.keySet()){
                checkerIndex = i;
            }
            int checkerFile = checkerIndex % 8;
            int checkerRank = checkerIndex / 8;


            boolean onSameColoredSquare =  (file+rank)%2==(checkerFile+checkerRank)%2;

            if((file!=checkerFile) && (rank!=checkerRank) && onSameColoredSquare&&cb.canSlide(file,rank,checkerFile,checkerRank)){
                moves.add(Util.cvtMove(file,rank,checkerFile,checkerRank,cb.board,cb.fenParts));
            }


            switch(Character.toUpperCase(cb.board[checkerRank][checkerFile])){
                case Constants.WHITE_PAWN:
                case Constants.WHITE_KNIGHT:
                    break;
                default:
                    int[] checkDirection = Constants.ALL_DIRECTIONS[cb.checkers.get(checkerIndex)];
                    if(checkDirection[0]==0){
                        if(file==checkerFile){
                            break;
                        }
                        int[] kingPosition = cb.kingPosition();
                        int r = kingPosition[1];
                        while((r+=checkDirection[1])!=checkerRank){
                            if(rank!=r&&(file+rank)%2==(checkerFile+r)%2){
                                if(cb.canSlide(file,rank,checkerFile,r)){
                                    moves.add(Util.cvtMove(file,rank,checkerFile,r,cb.board,cb.fenParts));
                                }
                            }
                        }

                    }else if(checkDirection[1]==0){
                        if(rank==checkerRank){
                            break;
                        }
                        int[] kingPosition = cb.kingPosition();
                        int f = kingPosition[0];
                        while((f+=checkDirection[0])!=checkerFile){
                            if(file!=f&&(file+rank)%2==(f+checkerRank)%2){
                                if(cb.canSlide(file,rank,f,checkerRank)){
                                    moves.add(Util.cvtMove(file,rank,f,checkerRank,cb.board,cb.fenParts));
                                }
                            }
                        }
                    }else{
                        int[] kingPosition = cb.kingPosition();
                        int f = kingPosition[0],r = kingPosition[1];
                        while((f+=checkDirection[0])!=checkerFile&&(r+=checkDirection[1])!=checkerRank){
                           if((file!=f) && (rank!=r)&&(file+rank)%2==(f+r)%2){
                                if(cb.canSlide(file,rank,f,r)){
                                    moves.add(Util.cvtMove(file,rank,f,r,cb.board,cb.fenParts));
                                }
                            }

                        }

//                        if(!onSameColoredSquare || file==checkerFile||rank==checkerRank){
//                            break;
//                        }
//                        int[] kingPosition = cb.kingPosition();
//                        int f = kingPosition[0],r = kingPosition[1];
//                        while((f+=checkDirection[0])!=checkerFile&&(r+=checkDirection[1])!=checkerRank){
//                            Util.writeToLog(f);
//                            Util.writeToLog(r);
//                            if((file!=f) && (rank!=r)&&(file+rank)%2==(f+r)%2){
//
//                                if(cb.canSlide(file,rank,f,r)){
//                                    moves.add(Util.cvtMove(file,rank,f,r,cb.board,cb.fenParts));
//                                }
//                            }
//                        }
                    }


            }




            return moves;
        }

        //normal moves
        int df,dr;
        for(int i = 4; i<Constants.ALL_DIRECTIONS.length; i++){
            df = file + Constants.ALL_DIRECTIONS[i][0];
            dr = rank + Constants.ALL_DIRECTIONS[i][1];
            while(Util.isValid(df,dr)){
                if(cb.board[dr][df] == Constants.EMPTY_SQUARE){
                    moves.add(Util.cvtMove(file,rank,df,dr,cb.board,cb.fenParts));
                }else{
                    if(Util.isAlly(cb.board[rank][file],cb.board[dr][df])){

                    }else{
                        moves.add(Util.cvtMove(file,rank,df,dr,cb.board,cb.fenParts));
                    }
                    break;
                }
                df += Constants.ALL_DIRECTIONS[i][0];
                dr += Constants.ALL_DIRECTIONS[i][1];
            }
        }
        return moves;
    }


}
