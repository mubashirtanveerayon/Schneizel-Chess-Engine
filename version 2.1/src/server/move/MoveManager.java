package server.move;

import server.board.ChessBoard;
import server.exception.InvalidMoveException;
import server.util.Constants;
import server.util.FenUtils;
import server.util.GameState;
import server.util.Util;

import java.util.ArrayList;

public class MoveManager {

    public ChessBoard cb;
    ArrayList<Move> moves;


    public MoveManager(ChessBoard cb){
        this.cb = cb;
        moves = new ArrayList<>();
    }


    public Move parse(String stdMove){//algebra to move
        int lf = Constants.FILES.indexOf(stdMove.charAt(0));
        int lr = Constants.RANKS.indexOf(stdMove.charAt(1));
        int df = Constants.FILES.indexOf(stdMove.charAt(2));
        int dr = Constants.RANKS.indexOf(stdMove.charAt(3));

        Move move = new Move(lf,lr,df,dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);

        switch(Character.toUpperCase(cb.board[lr][lf])){
            case Constants.WHITE_KING:
                switch(lf-df) {
                    case 2:
                        move.isQueenSideCastling = true;
                        break;
                    case -2:
                        move.isKingSideCastling = true;
                        break;
                }

                break;
            case Constants.WHITE_PAWN:
                if(dr == 0){
                    move.promotionPiece = Character.toUpperCase(stdMove.charAt(4));
                }else if(dr == 7){
                    move.promotionPiece = Character.toLowerCase(stdMove.charAt(4));
                }else if(lf-df!=0&&cb.board[dr][df] == Constants.EMPTY_SQUARE){
                    move.isEnPassant = true;
                }

                break;

        }
        return move;
    }


    public String moveGenerationTest(int depth){
        ArrayList<Move> moves = getAllMoves() ;
        int numPositions = 0;
//        String fen = FenUtils.generate(cb);
        StringBuilder output = new StringBuilder();
        for (Move move:moves) {

                //System.out.println(FenUtils.generate(cb));
//                ChessBoard c = new ChessBoard(FenUtils.generate(cb));
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
//            if(!fen.equals(FenUtils.generate(cb))){
//                System.out.println("Original: "+fen);
//                System.out.println("Current: "+FenUtils.generate(cb));
//                System.out.println(move);
//                System.out.println("###");
//                System.out.println(cb.stats());
//            }
            //ends here

//            if(cvt(moveStr).equals("c2c1") ){
//                System.out.println(cb.stats());
//                System.out.println("###");
//            }

            makeMove(move);
            int numMoves = recurseMoveGeneration(depth-1);
            numPositions += numMoves;
            undoMove(move);
//            if(cvt(moveStr).equals("c2c1") ){
//                System.out.println(cb.stats());
//                System.out.println("###");
//            }

            output.append(move.toString() + ": " + numMoves).append("\n");
        }
        output.append("Nodes searched: "+numPositions).append("\n");
        return output.toString();
    }



    private int recurseMoveGeneration(int depth){
        if (depth <= 0) {
            return 1;
        }
        ArrayList<Move> moves = getAllMoves() ;
        int numPositions = 0;
        for (Move move:moves) {

            makeMove(move);

            numPositions += recurseMoveGeneration(depth-1);
            undoMove(move);

        }
        return numPositions;
    }


    public ArrayList<Move> getAllMoves() {
        ArrayList<Move> allMoves = new ArrayList<>();
        for(int i=0;i<8;i++){
            for(int j=0;j<8;j++){
                if(cb.board[i][j] != Constants.EMPTY_SQUARE && !Util.isEnemyPiece(cb.whiteToMove,cb.board[i][j])){
                    allMoves.addAll(generateMove(j,i));
                }
            }
        }
        return allMoves;
    }


    public ArrayList<Move> getAllCaptureMoves() {
        ArrayList<Move> allMoves = new ArrayList<>();
        for(int i=0;i<8;i++){
            for(int j=0;j<8;j++){
                if(cb.board[i][j] != Constants.EMPTY_SQUARE && !Util.isEnemyPiece(cb.whiteToMove,cb.board[i][j])){
                    allMoves.addAll(generateCaptureMove(j,i));
                }
            }
        }
        return allMoves;
    }


    public void verify(Move move) throws InvalidMoveException{
        for(Move m:getAllMoves()){
            if(m.equals(move)){
                return;
            }
        }
        throw new InvalidMoveException(move);
    }




    // Here are all the functions from previous move class
    public void makeMove(Move move){
        if(move.isQueenSideCastling){
            int rank = cb.whiteToMove?7:0;
            cb.board[rank][2] = cb.board[rank][4];
            cb.board[rank][4] = Constants.EMPTY_SQUARE;
            cb.board[rank][3] = cb.board[rank][0];
            cb.board[rank][0] = Constants.EMPTY_SQUARE;
            cb.pieceLocations.add(2 + rank * 8);
            cb.pieceLocations.remove((Object)(4 + rank * 8));
            cb.pieceLocations.add(3 + rank * 8);
            cb.pieceLocations.remove((Object)(0 + rank * 8));
            if(cb.whiteToMove){
                cb.whiteKingPosition[0] = 2;
            }else{
                cb.blackKingPosition[0] = 2;
            }
            if(cb.whiteToMove){
                cb.castlingFEN = cb.castlingFEN.replace(Character.toString(Constants.WHITE_KING),"").replace(Character.toString(Constants.WHITE_QUEEN),"");
            }else{
                cb.castlingFEN = cb.castlingFEN.replace(Character.toString(Constants.BLACK_KING),"").replace(Character.toString(Constants.BLACK_QUEEN),"");
            }
            cb.whiteToMove = !cb.whiteToMove;
            cb.enPassantSquare = "-";
            cb.halfMoveClock += 1;
        }else if(move.isKingSideCastling){

            int rank = cb.whiteToMove?7:0;
            cb.board[rank][6] = cb.board[rank][4];
            cb.board[rank][4] = Constants.EMPTY_SQUARE;
            cb.board[rank][5] = cb.board[rank][7];
            cb.board[rank][7] = Constants.EMPTY_SQUARE;
            cb.pieceLocations.add(6 + rank * 8);
            cb.pieceLocations.remove((Object)(4 + rank * 8));
            cb.pieceLocations.add(5 + rank * 8);
            cb.pieceLocations.remove((Object)(7 + rank * 8));
            if(cb.whiteToMove){
                cb.whiteKingPosition[0] = 6;
            }else{
                cb.blackKingPosition[0] = 6;
            }
            if(cb.whiteToMove){
                cb.castlingFEN = cb.castlingFEN.replace(Character.toString(Constants.WHITE_KING),"").replace(Character.toString(Constants.WHITE_QUEEN),"");
            }else{
                cb.castlingFEN = cb.castlingFEN.replace(Character.toString(Constants.BLACK_KING),"").replace(Character.toString(Constants.BLACK_QUEEN),"");
            }
            cb.whiteToMove = !cb.whiteToMove;
            cb.enPassantSquare = "-";
            cb.halfMoveClock += 1;

        }else if(move.locRank == move.destRank){
            int rank = move.locRank;
            int locFile = move.locFile;
            int destFile = move.destFile;
            if(cb.board[rank][locFile] == Constants.WHITE_KING){
                cb.whiteKingPosition[0] = destFile;
                cb.whiteKingPosition[1] = rank;
            }else if(cb.board[rank][locFile] == Constants.BLACK_KING){
                cb.blackKingPosition[0] = destFile;
                cb.blackKingPosition[1] = rank;
            }
            if(cb.board[rank][destFile]!=Constants.EMPTY_SQUARE){
                cb.halfMoveClock=0;
            }else{
                cb.halfMoveClock += 1;
            }

            if(rank == 0 || rank == 7){
                switch(cb.board[rank][locFile]){
                    case Constants.WHITE_ROOK:
                        if(locFile==0 && cb.castlingFEN.contains(String.valueOf(Constants.WHITE_QUEEN))){
                            cb.castlingFEN = cb.castlingFEN.replace(Character.toString(Constants.WHITE_QUEEN),"");
                        }else if(locFile==7 && cb.castlingFEN.contains(String.valueOf(Constants.WHITE_KING))){
                            cb.castlingFEN = cb.castlingFEN.replace(Character.toString(Constants.WHITE_KING), "");
                        }
                        break;
                    case Constants.WHITE_KING:
                        if(cb.castlingFEN.contains(String.valueOf(Constants.WHITE_QUEEN)) || cb.castlingFEN.contains(String.valueOf(Constants.WHITE_KING))) {
                            cb.castlingFEN = cb.castlingFEN.replace(Character.toString(Constants.WHITE_KING), "").replace(Character.toString(Constants.WHITE_QUEEN), "");
                        }
                        break;
                    case Constants.BLACK_ROOK:
                        if(locFile==0 && cb.castlingFEN.contains(String.valueOf(Constants.BLACK_QUEEN))){
                            cb.castlingFEN = cb.castlingFEN.replace(Character.toString(Constants.BLACK_QUEEN),"");
                        }else if(locFile==7 && cb.castlingFEN.contains(String.valueOf(Constants.BLACK_KING))) {
                            cb.castlingFEN = cb.castlingFEN.replace(Character.toString(Constants.BLACK_KING), "");
                        }
                        break;
                    case Constants.BLACK_KING:
                        if(cb.castlingFEN.contains(String.valueOf(Constants.BLACK_QUEEN)) || cb.castlingFEN.contains(String.valueOf(Constants.BLACK_KING))) {
                            cb.castlingFEN = cb.castlingFEN.replace(Character.toString(Constants.BLACK_KING), "").replace(Character.toString(Constants.BLACK_QUEEN), "");
                        }
                        break;
                }
                switch(cb.board[rank][destFile]){
                    case Constants.WHITE_ROOK:
                        if (rank!=7){
                            break;
                        }
                        if(destFile==0 && cb.castlingFEN.contains(String.valueOf(Constants.WHITE_QUEEN))){
                            cb.castlingFEN = cb.castlingFEN.replace(Character.toString(Constants.WHITE_QUEEN),"");
                        }else if(destFile==7 && cb.castlingFEN.contains(String.valueOf(Constants.WHITE_KING))){
                            cb.castlingFEN = cb.castlingFEN.replace(Character.toString(Constants.WHITE_KING), "");
                        }
                        break;
                    case Constants.BLACK_ROOK:
                        if (rank != 0){
                            break;
                        }
                        if(destFile==0 && cb.castlingFEN.contains(String.valueOf(Constants.BLACK_QUEEN))){
                            cb.castlingFEN = cb.castlingFEN.replace(Character.toString(Constants.BLACK_QUEEN),"");
                        }else if(destFile==7 && cb.castlingFEN.contains(String.valueOf(Constants.BLACK_KING))) {
                            cb.castlingFEN = cb.castlingFEN.replace(Character.toString(Constants.BLACK_KING), "");
                        }
                        break;
                }
            }

            cb.board[rank][destFile] = cb.board[rank][locFile];
            cb.board[rank][locFile] = Constants.EMPTY_SQUARE;
            cb.pieceLocations.add(destFile + rank * 8);
            cb.pieceLocations.remove((Object)(locFile + rank * 8));

            cb.enPassantSquare = "-";
            cb.whiteToMove = !cb.whiteToMove;
        }else if(move.locFile == move.destFile){
            int file = move.locFile;
            int locRank = move.locRank;
            int destRank = move.destRank;

            if(cb.board[locRank][file] == Constants.WHITE_KING){
                cb.whiteKingPosition[1] = destRank;
            }else if(cb.board[locRank][file] == Constants.BLACK_KING){
                cb.blackKingPosition[1] = destRank;
            }

            if(Character.toUpperCase(cb.board[locRank][file]) == Constants.WHITE_PAWN || cb.board[destRank][file]!=Constants.EMPTY_SQUARE){
                cb.halfMoveClock=0;
            }else{
                cb.halfMoveClock += 1;
            }

            if(locRank == 0 || locRank == 7){
                switch(cb.board[locRank][file]){
                    case Constants.BLACK_ROOK:{
                        if(file==0 && cb.castlingFEN.contains(String.valueOf(Constants.BLACK_QUEEN))){
                            cb.castlingFEN = cb.castlingFEN.replace(Character.toString(Constants.BLACK_QUEEN),"");
                        }else if(file==7 && cb.castlingFEN.contains(String.valueOf(Constants.BLACK_KING))) {
                            cb.castlingFEN = cb.castlingFEN.replace(Character.toString(Constants.BLACK_KING), "");
                        }
                        break;
                    }
                    case Constants.WHITE_ROOK:{
                        if(file==0 && cb.castlingFEN.contains(String.valueOf(Constants.WHITE_QUEEN))){
                            cb.castlingFEN = cb.castlingFEN.replace(Character.toString(Constants.WHITE_QUEEN),"");
                        }else if(file==7 && cb.castlingFEN.contains(String.valueOf(Constants.WHITE_KING))) {
                            cb.castlingFEN = cb.castlingFEN.replace(Character.toString(Constants.WHITE_KING), "");
                        }
                        break;
                    }
                    case Constants.BLACK_KING:{
                        if(cb.castlingFEN.contains(String.valueOf(Constants.BLACK_QUEEN)) || cb.castlingFEN.contains(String.valueOf(Constants.BLACK_KING))) {
                            cb.castlingFEN = cb.castlingFEN.replace(Character.toString(Constants.BLACK_KING), "").replace(Character.toString(Constants.BLACK_QUEEN), "");
                        }
                        break;
                    }
                    case Constants.WHITE_KING:{
                        if(cb.castlingFEN.contains(String.valueOf(Constants.WHITE_QUEEN)) || cb.castlingFEN.contains(String.valueOf(Constants.WHITE_KING))) {
                            cb.castlingFEN = cb.castlingFEN.replace(Character.toString(Constants.WHITE_KING), "").replace(Character.toString(Constants.WHITE_QUEEN), "");
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
                        if(file==0 && cb.castlingFEN.contains(String.valueOf(Constants.WHITE_QUEEN))){
                            cb.castlingFEN = cb.castlingFEN.replace(Character.toString(Constants.WHITE_QUEEN),"");
                        }else if(file==7 && cb.castlingFEN.contains(String.valueOf(Constants.WHITE_KING))){
                            cb.castlingFEN = cb.castlingFEN.replace(Character.toString(Constants.WHITE_KING), "");
                        }
                        break;
                    case Constants.BLACK_ROOK:
                        if(destRank != 0){
                            break;
                        }
                        if(file==0 && cb.castlingFEN.contains(String.valueOf(Constants.BLACK_QUEEN))){
                            cb.castlingFEN = cb.castlingFEN.replace(Character.toString(Constants.BLACK_QUEEN),"");
                        }else if(file==7 && cb.castlingFEN.contains(String.valueOf(Constants.BLACK_KING))) {
                            cb.castlingFEN = cb.castlingFEN.replace(Character.toString(Constants.BLACK_KING), "");
                        }
                        break;
                }

                if(move.promotionPiece != Constants.EMPTY_SQUARE){
                    cb.board[locRank][file] = move.promotionPiece;
                }

            }

            if(Character.toUpperCase(cb.board[locRank][file]) == Constants.WHITE_PAWN) {
                //double pawn push -> creates en-passant square
                if (locRank == 1 && cb.board[locRank][file] == Constants.BLACK_PAWN && destRank == 3) {
                    cb.enPassantSquare = Constants.FILES.charAt(file) + "6";
                } else if (locRank == 6 && cb.board[locRank][file] == Constants.WHITE_PAWN && destRank == 4) {
                    cb.enPassantSquare = Constants.FILES.charAt(file) + "3";
                } else {
                    cb.enPassantSquare = "-";
                }
            }else{
                cb.enPassantSquare = "-";
            }


            cb.board[destRank][file] = cb.board[locRank][file];
            cb.board[locRank][file] = Constants.EMPTY_SQUARE;
            cb.pieceLocations.add(file + destRank * 8);
            cb.pieceLocations.remove((Object)(file+locRank*8));
            cb.whiteToMove = !cb.whiteToMove;

        }else if(move.isEnPassant){


            int locFile = move.locFile;
            int destFile = move.destFile;
            int locRank = move.locRank;
            int destRank = move.destRank;
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
            cb.whiteToMove = !cb.whiteToMove;
            cb.enPassantSquare = "-";
            cb.halfMoveClock=0;

        }

        else{
            int locFile = move.locFile;
            int destFile = move.destFile;
            int locRank = move.locRank;
            int destRank = move.destRank;
            if(cb.board[locRank][locFile] == Constants.WHITE_KING){
                cb.whiteKingPosition[0] = destFile;
                cb.whiteKingPosition[1] = destRank;
            }else if(cb.board[locRank][locFile] == Constants.BLACK_KING){
                cb.blackKingPosition[0] = destFile;
                cb.blackKingPosition[1] = destRank;
            }
            if(Character.toUpperCase(cb.board[locRank][locFile]) == Constants.WHITE_PAWN || cb.board[destRank][destFile]!=Constants.EMPTY_SQUARE){
                cb.halfMoveClock=0;
            }else{
                cb.halfMoveClock += 1;
            }



            if(locRank == 0 || locRank == 7){
                switch(cb.board[locRank][locFile]){
                    case Constants.BLACK_KING:{
                        if(cb.castlingFEN.contains(String.valueOf(Constants.BLACK_QUEEN)) || cb.castlingFEN.contains(String.valueOf(Constants.BLACK_KING))) {
                            cb.castlingFEN = cb.castlingFEN.replace(Character.toString(Constants.BLACK_KING), "").replace(Character.toString(Constants.BLACK_QUEEN), "");
                        }
                        break;
                    }
                    case Constants.WHITE_KING:{
                        if(cb.castlingFEN.contains(String.valueOf(Constants.WHITE_QUEEN)) || cb.castlingFEN.contains(String.valueOf(Constants.WHITE_KING))) {
                            cb.castlingFEN = cb.castlingFEN.replace(Character.toString(Constants.WHITE_KING), "").replace(Character.toString(Constants.WHITE_QUEEN), "");
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
                        if(destFile==0 && cb.castlingFEN.contains(String.valueOf(Constants.WHITE_QUEEN))){
                            cb.castlingFEN = cb.castlingFEN.replace(Character.toString(Constants.WHITE_QUEEN),"");
                        }else if(destFile==7 && cb.castlingFEN.contains(String.valueOf(Constants.WHITE_KING))){
                            cb.castlingFEN = cb.castlingFEN.replace(Character.toString(Constants.WHITE_KING), "");
                        }
                        break;
                    case Constants.BLACK_ROOK:
                        if(destRank != 0){
                            break;
                        }
                        if(destFile==0 && cb.castlingFEN.contains(String.valueOf(Constants.BLACK_QUEEN))){
                            cb.castlingFEN = cb.castlingFEN.replace(Character.toString(Constants.BLACK_QUEEN),"");
                        }else if(destFile==7 && cb.castlingFEN.contains(String.valueOf(Constants.BLACK_KING))) {
                            cb.castlingFEN = cb.castlingFEN.replace(Character.toString(Constants.BLACK_KING), "");
                        }
                        break;
                }
                if(Character.toUpperCase(cb.board[locRank][locFile]) == Constants.WHITE_PAWN){
                    cb.board[locRank][locFile] = move.promotionPiece;
                }
            }


            cb.board[destRank][destFile] = cb.board[locRank][locFile];
            cb.board[locRank][locFile] = Constants.EMPTY_SQUARE;
            cb.pieceLocations.add(destFile + destRank * 8);
            cb.pieceLocations.remove((Object)(locFile+locRank*8));
            cb.whiteToMove = !cb.whiteToMove;
            cb.enPassantSquare = "-";
        }
        if(cb.castlingFEN.equals(" ")||cb.castlingFEN.equals("")){
            cb.castlingFEN = "-";
        }
        if(cb.whiteToMove) {
            cb.fullMoveClock += 1;
        }

        cb.checkBoard();

    }
    public void undoMove(Move move){
        if(move.isQueenSideCastling){
            //System.out.println(cvt(move));
            cb.whiteToMove = !cb.whiteToMove;
            int rank = cb.whiteToMove?7:0;
            cb.board[rank][4] = cb.board[rank][2];
            cb.board[rank][2] = Constants.EMPTY_SQUARE;
            cb.board[rank][0] = cb.board[rank][3];
            cb.board[rank][3] = Constants.EMPTY_SQUARE;
            cb.pieceLocations.add(4 + rank * 8);
            cb.pieceLocations.remove((Object)(2 + rank * 8));
            cb.pieceLocations.add(0 + rank * 8);
            cb.pieceLocations.remove((Object)(3 + rank * 8));
            if(cb.whiteToMove){
                cb.whiteKingPosition[0] = 4;
            }else{
                cb.blackKingPosition[0] = 4;
            }
        }else if(move.isKingSideCastling){
            cb.whiteToMove = !cb.whiteToMove;
            int rank = cb.whiteToMove?7:0;
            cb.board[rank][4] = cb.board[rank][6];
            cb.board[rank][6] = Constants.EMPTY_SQUARE;
            cb.board[rank][7] = cb.board[rank][5];
            cb.board[rank][5] = Constants.EMPTY_SQUARE;
            cb.pieceLocations.add(4 + rank * 8);
            cb.pieceLocations.remove((Object)(6 + rank * 8));
            cb.pieceLocations.add(7 + rank * 8);
            cb.pieceLocations.remove((Object)(5 + rank * 8));
            if(cb.whiteToMove){
                cb.whiteKingPosition[0] = 4;
            }else{
                cb.blackKingPosition[0] = 4;
            }
        }else if(move.locRank == move.destRank){
            int rank = move.locRank;
            int locFile = move.destFile;
            int destFile = move.locFile;

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





            if(rank == 0 || rank == 7){
                switch(cb.board[rank][locFile]){
                    case Constants.WHITE_ROOK:

                    case Constants.WHITE_KING:

                    case Constants.BLACK_ROOK:

                    case Constants.BLACK_KING:
                        cb.castlingFEN = move.castlingFEN;
                }

            }

            cb.board[rank][destFile] = cb.board[rank][locFile];
            cb.board[rank][locFile] = move.capturedPiece;
            cb.pieceLocations.add(destFile + rank * 8);
            if(move.capturedPiece == Constants.EMPTY_SQUARE) {
                cb.pieceLocations.remove((Object)(locFile + rank * 8));
            }
            cb.whiteToMove = !cb.whiteToMove;

        }else if(move.locFile == move.destFile){
            int file = move.locFile;
            int locRank = move.destRank;
            int destRank = move.locRank;
            cb.whiteToMove = !cb.whiteToMove;
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


            if(locRank == 0 || locRank == 7){
                if(move.promotionPiece != Constants.EMPTY_SQUARE){
                    if(cb.whiteToMove){
                        cb.board[locRank][file] = Constants.WHITE_PAWN;
                    }else{
                        cb.board[locRank][file] = Constants.BLACK_PAWN;
                    }
                }

            }
            //position startpos move d2d3 e7e6 c1f4 f8b4 d1d2 b8c6 b1c3
            //go perft 2
            cb.halfMoveClock = move.halfMoveClock;
            cb.board[destRank][file] = cb.board[locRank][file];
            cb.board[locRank][file] = move.capturedPiece;
            cb.pieceLocations.add(file + destRank * 8);
            if(move.capturedPiece == Constants.EMPTY_SQUARE) {
                cb.pieceLocations.remove((Object)(file+locRank*8));
            }


        }
        else if(move.isEnPassant){
            int locFile = move.destFile;
            int destFile = move.locFile;
            int locRank = move.destRank;
            int destRank = move.locRank;
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
            cb.whiteToMove = !cb.whiteToMove;
        }else{
            int locFile = move.destFile;
            int destFile = move.locFile;
            int locRank = move.destRank;
            int destRank = move.locRank;
            cb.whiteToMove  = !cb.whiteToMove;
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



            if(locRank == 0 || locRank == 7){
                if(move.promotionPiece != Constants.EMPTY_SQUARE){
                    if(cb.whiteToMove){
                        cb.board[locRank][locFile] = Constants.WHITE_PAWN;
                    }else{
                        cb.board[locRank][locFile] = Constants.BLACK_PAWN;
                    }
                }
            }
            cb.halfMoveClock = move.halfMoveClock;

            cb.board[destRank][destFile] = cb.board[locRank][locFile];
            cb.board[locRank][locFile] = move.capturedPiece;
            cb.pieceLocations.add(destFile + destRank * 8);
            if(move.capturedPiece == Constants.EMPTY_SQUARE) {
                cb.pieceLocations.remove((Object) (locFile + locRank * 8));
            }


        }
        cb.castlingFEN = move.castlingFEN;
        cb.enPassantSquare = move.enPassantSquare;
        cb.halfMoveClock = move.halfMoveClock;

        if(!cb.whiteToMove) {
            cb.fullMoveClock -= 1;
        }
        cb.checkBoard();
    }

    //ends here


    public ArrayList<Move> generateCaptureMove(int file, int rank){
        moves.clear();
        switch(Character.toUpperCase(cb.board[rank][file])){
            case Constants.WHITE_KING:
                return kingCaptures(file,rank);
            case Constants.WHITE_PAWN:
                return pawnCaptures(file,rank);
            case Constants.WHITE_ROOK:
                return rookCaptures(file,rank);
            case Constants.WHITE_BISHOP:
                return bishopCaptures(file,rank);
            case Constants.WHITE_KNIGHT:
                return knightCaptures(file,rank);
            case Constants.WHITE_QUEEN:
                return queenCaptures(file,rank);
            default:
                return null;
        }
    }





    public ArrayList<Move> generateMove(int file, int rank){
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



    public ArrayList<Move> kingCaptures(final int file,final int rank){
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
            char king = cb.board[rank][file];
            for(int[] direction:Constants.ALL_DIRECTIONS) {
                int newFile = file + direction[0];
                int newRank = rank + direction[1];
                if (!Util.isValid(newFile, newRank)) {
                    continue;
                }

                if (checkDirections.contains(direction) && !cb.checkers.containsKey(file + direction[0] + (rank + direction[1]) * 8)) {
                    continue;
                }

                if (cb.board[newRank][newFile] != Constants.EMPTY_SQUARE && Util.isEnemyPiece(cb.whiteToMove, cb.board[newRank][newFile])) {
                    cb.board[rank][file] = Constants.EMPTY_SQUARE;
                    if (!cb.squareUnderAttack(newFile, newRank)) {
                        moves.add(new Move(file, rank, newFile, newRank, cb.board, cb.castlingFEN, cb.enPassantSquare, cb.halfMoveClock));
                    }
                    cb.board[rank][file] = king;
                }


            }
            return moves;
        }




        for(int[] direction:Constants.ALL_DIRECTIONS){
            int newFile = file+direction[0];
            int newRank = rank+direction[1];
            if(!Util.isValid(newFile,newRank)){
                continue;
            }
            if(cb.board[newRank][newFile] == Constants.EMPTY_SQUARE){
                continue;
            }


            if(Util.isEnemyPiece(cb.whiteToMove,cb.board[newRank][newFile]) && !cb.squareUnderAttack(newFile,newRank)){
                moves.add(new Move(file,rank,newFile,newRank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
            }
        }
        return moves;
    }

    public ArrayList<Move> king(final int file,final int rank){
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

            char king = cb.board[rank][file];
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
//                    moves.add(Util.cvtMove(file,rank,newFile,newRank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
//                }



                cb.board[rank][file] = Constants.EMPTY_SQUARE;

                if(!cb.squareUnderAttack(newFile,newRank)){
                    Move move = new Move(file,rank,newFile,newRank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                    moves.add(move);
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
//                moves.add(Util.cvtMove(file,rank,newFile,newRank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
//            }

            if(!cb.squareUnderAttack(newFile,newRank)){
                Move move = new Move(file,rank,newFile,newRank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                moves.add(move);
            }
        }
        // castling
        boolean kingSide,queenSide;
        if(cb.whiteToMove){
            kingSide = cb.castlingFEN.contains(Character.toString(Constants.WHITE_KING));
            queenSide = cb.castlingFEN.contains(Character.toString(Constants.WHITE_QUEEN));
        }else{
            kingSide = cb.castlingFEN.contains(Character.toString(Constants.BLACK_KING));
            queenSide = cb.castlingFEN.contains(Character.toString(Constants.BLACK_QUEEN));
        }

        kingSide = kingSide && cb.board[rank][5] == Constants.EMPTY_SQUARE && cb.board[rank][6] == Constants.EMPTY_SQUARE && !cb.squareUnderAttack(5,rank) && !cb.squareUnderAttack(6,rank);
        queenSide = queenSide && cb.board[rank][3] == Constants.EMPTY_SQUARE && cb.board[rank][2] == Constants.EMPTY_SQUARE && cb.board[rank][1] == Constants.EMPTY_SQUARE && !cb.squareUnderAttack(3,rank) && !cb.squareUnderAttack(2,rank) ;
        if(kingSide){
            Move move = new Move(file,rank,6,rank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
            move.isKingSideCastling = true;
            moves.add(move);
        }
        if(queenSide){
            Move move = new Move(file,rank,2,rank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
            move.isQueenSideCastling = true;
            moves.add(move);
        }
        return moves;
    }



    public ArrayList<Move> pawnCaptures(final int file,final int rank){
        int pinnedIndex = file + rank * 8;
        boolean pinned = cb.pinnedPieces.containsKey(pinnedIndex);
        if(pinned){
            if(cb.gs == GameState.CHECK){
                return moves;
            }
            int[] pinDirection = Constants.ALL_DIRECTIONS[cb.pinnedPieces.get(pinnedIndex)];
            if(pinDirection[0] != 0 && pinDirection[1] != 0){
                if((Util.isUpperCase(cb.board[rank][file]) && pinDirection[1] == -1) || (!Util.isUpperCase(cb.board[rank][file]) && pinDirection[1] == 1)){
                    int df = file + pinDirection[0],dr = rank + pinDirection[1];
                    if(cb.board[dr][df] != Constants.EMPTY_SQUARE){
                        if(dr == 0 || dr == 7) {
                            if(cb.whiteToMove){
                                Move moveQ = new Move(file,rank,df,dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                                moveQ.promotionPiece = Constants.WHITE_QUEEN;
                                moves.add(moveQ);
                                Move moveN = new Move(file,rank,df,dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                                moveN.promotionPiece = Constants.WHITE_KNIGHT;
                                moves.add(moveN);
                                Move moveR = new Move(file,rank,df,dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                                moveR.promotionPiece = Constants.WHITE_ROOK;
                                moves.add(moveR);
                                Move moveB = new Move(file,rank,df,dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                                moveB.promotionPiece = Constants.WHITE_BISHOP;
                                moves.add(moveB);
                            }else{
                                Move moveQ = new Move(file,rank,df,dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                                moveQ.promotionPiece = Constants.BLACK_QUEEN;
                                moves.add(moveQ);
                                Move moveN = new Move(file,rank,df,dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                                moveN.promotionPiece = Constants.BLACK_KNIGHT;
                                moves.add(moveN);
                                Move moveR = new Move(file,rank,df,dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                                moveR.promotionPiece = Constants.BLACK_ROOK;
                                moves.add(moveR);
                                Move moveB = new Move(file,rank,df,dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                                moveB.promotionPiece = Constants.BLACK_BISHOP;
                                moves.add(moveB);
                            }
                        }else {
                            moves.add(new Move(file,rank,df,dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
                        }
                    }
                    if(cb.enPassantSquare.equals("-")){
                        // do nothing
                    }else{
                        dr = cb.whiteToMove?2:5;
                        if((file-Constants.FILES.indexOf(cb.enPassantSquare.charAt(0))) == -pinDirection[0]){
                            if((!Util.isUpperCase(cb.board[rank][file]) && rank == 4)||(Util.isUpperCase(cb.board[rank][file]) && rank == 3)){
                                Move move = new Move(file, rank, Constants.FILES.indexOf(cb.enPassantSquare.charAt(0)), dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                                move.isEnPassant = true;
                                moves.add(move);
                            }
                        }
                    }
                }
            }
            return moves;
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
                if ((cb.whiteToMove &&rank-checkerRank==1) || (!cb.whiteToMove &&rank-checkerRank==-1)){
                    if(checkerRank == 0 || checkerRank == 7){
                        if(cb.whiteToMove){
                            Move moveQ = new Move(file,rank,checkerFile,checkerRank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveQ.promotionPiece = Constants.WHITE_QUEEN;
                            moves.add(moveQ);
                            Move moveN = new Move(file,rank,checkerFile,checkerRank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveN.promotionPiece = Constants.WHITE_KNIGHT;
                            moves.add(moveN);
                            Move moveR = new Move(file,rank,checkerFile,checkerRank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveR.promotionPiece = Constants.WHITE_ROOK;
                            moves.add(moveR);
                            Move moveB = new Move(file,rank,checkerFile,checkerRank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveB.promotionPiece = Constants.WHITE_BISHOP;
                            moves.add(moveB);
                        }else{
                            Move moveQ = new Move(file,rank,checkerFile,checkerRank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveQ.promotionPiece = Constants.BLACK_QUEEN;
                            moves.add(moveQ);
                            Move moveN = new Move(file,rank,checkerFile,checkerRank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveN.promotionPiece = Constants.BLACK_KNIGHT;
                            moves.add(moveN);
                            Move moveR = new Move(file,rank,checkerFile,checkerRank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveR.promotionPiece = Constants.BLACK_ROOK;
                            moves.add(moveR);
                            Move moveB = new Move(file,rank,checkerFile,checkerRank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveB.promotionPiece = Constants.BLACK_BISHOP;
                            moves.add(moveB);
                        }
                    }else {
                        moves.add(new Move(file,rank,checkerFile,checkerRank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
                    }
                }
            }



            return moves;
        }



        int startIndex = Util.isUpperCase(cb.board[rank][file])?6:4;
        int endIndex = Util.isUpperCase(cb.board[rank][file])?Constants.ALL_DIRECTIONS.length-1:5;
        int f,r;
        for(int i=startIndex;i<=endIndex;i++){
            f = file + Constants.ALL_DIRECTIONS[i][0];
            r = rank + Constants.ALL_DIRECTIONS[i][1];

            if(Util.isValid(f,r)){
                if(cb.board[r][f] != Constants.EMPTY_SQUARE && Util.isEnemyPiece(cb.whiteToMove,cb.board[r][f])){

                    if(r == 0 || r == 7){
                        if(cb.whiteToMove){
                            Move moveQ = new Move(file,rank,f,r,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveQ.promotionPiece = Constants.WHITE_QUEEN;
                            moves.add(moveQ);
                            Move moveN = new Move(file,rank,f,r,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveN.promotionPiece = Constants.WHITE_KNIGHT;
                            moves.add(moveN);
                            Move moveR = new Move(file,rank,f,r,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveR.promotionPiece = Constants.WHITE_ROOK;
                            moves.add(moveR);
                            Move moveB = new Move(file,rank,f,r,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveB.promotionPiece = Constants.WHITE_BISHOP;
                            moves.add(moveB);
                        }else{
                            Move moveQ = new Move(file,rank,f,r,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveQ.promotionPiece = Constants.BLACK_QUEEN;
                            moves.add(moveQ);
                            Move moveN = new Move(file,rank,f,r,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveN.promotionPiece = Constants.BLACK_KNIGHT;
                            moves.add(moveN);
                            Move moveR = new Move(file,rank,f,r,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveR.promotionPiece = Constants.BLACK_ROOK;
                            moves.add(moveR);
                            Move moveB = new Move(file,rank,f,r,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveB.promotionPiece = Constants.BLACK_BISHOP;
                            moves.add(moveB);
                        }
                    }else {
                        moves.add(new Move(file,rank,f,r,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
                    }
                }
            }
        }

        if(cb.enPassantSquare.equals("-")){
            // do nothing
        }else{
            int dr = cb.whiteToMove?2:5;
            if(Math.abs(file-Constants.FILES.indexOf(cb.enPassantSquare.charAt(0))) == 1){
                int[] kingPosition = cb.kingPosition();
                int enPassantPawn = Constants.FILES.indexOf(cb.enPassantSquare.charAt(0)),direction = Util.getSign(kingPosition[0],file);
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
                            Move move = new Move(file, rank, Constants.FILES.indexOf(cb.enPassantSquare.charAt(0)), dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            move.isEnPassant = true;
                            moves.add(move);
                        }
                    }else {
                        Move move = new Move(file, rank, Constants.FILES.indexOf(cb.enPassantSquare.charAt(0)), dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                        move.isEnPassant = true;
                        moves.add(move);
                    }
                }
            }
        }



        return moves;
    }


    public ArrayList<Move> pawn(int file,int rank){
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
                                if(cb.whiteToMove){
                                    Move moveQ = new Move(file,rank,df,dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                                    moveQ.promotionPiece = Constants.WHITE_QUEEN;
                                    moves.add(moveQ);
                                    Move moveN = new Move(file,rank,df,dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                                    moveN.promotionPiece = Constants.WHITE_KNIGHT;
                                    moves.add(moveN);
                                    Move moveR = new Move(file,rank,df,dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                                    moveR.promotionPiece = Constants.WHITE_ROOK;
                                    moves.add(moveR);
                                    Move moveB = new Move(file,rank,df,dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                                    moveB.promotionPiece = Constants.WHITE_BISHOP;
                                    moves.add(moveB);
                                }else{
                                    Move moveQ = new Move(file,rank,df,dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                                    moveQ.promotionPiece = Constants.BLACK_QUEEN;
                                    moves.add(moveQ);
                                    Move moveN = new Move(file,rank,df,dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                                    moveN.promotionPiece = Constants.BLACK_KNIGHT;
                                    moves.add(moveN);
                                    Move moveR = new Move(file,rank,df,dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                                    moveR.promotionPiece = Constants.BLACK_ROOK;
                                    moves.add(moveR);
                                    Move moveB = new Move(file,rank,df,dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                                    moveB.promotionPiece = Constants.BLACK_BISHOP;
                                    moves.add(moveB);
                                }
                            }else {
                                moves.add(new Move(file,rank,df,dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
                            }
                        }
                    }


                    if(cb.enPassantSquare.equals("-")){
                        // do nothing
                    }else{
                        int dr = cb.whiteToMove?2:5;
                        if((file-Constants.FILES.indexOf(cb.enPassantSquare.charAt(0))) == -pinDirection[0]){
                            if((!Util.isUpperCase(cb.board[rank][file]) && rank == 4)||(Util.isUpperCase(cb.board[rank][file]) && rank == 3)){
                                Move move = new Move(file, rank, Constants.FILES.indexOf(cb.enPassantSquare.charAt(0)), dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                                move.isEnPassant = true;
                                moves.add(move);
                            }
                        }
                    }

                }
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
                if ((cb.whiteToMove &&rank-checkerRank==1) || (!cb.whiteToMove &&rank-checkerRank==-1)){
                    if(checkerRank == 0 || checkerRank == 7){
                        if(cb.whiteToMove){
                            Move moveQ = new Move(file,rank,checkerFile,checkerRank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveQ.promotionPiece = Constants.WHITE_QUEEN;
                            moves.add(moveQ);
                            Move moveN = new Move(file,rank,checkerFile,checkerRank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveN.promotionPiece = Constants.WHITE_KNIGHT;
                            moves.add(moveN);
                            Move moveR = new Move(file,rank,checkerFile,checkerRank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveR.promotionPiece = Constants.WHITE_ROOK;
                            moves.add(moveR);
                            Move moveB = new Move(file,rank,checkerFile,checkerRank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveB.promotionPiece = Constants.WHITE_BISHOP;
                            moves.add(moveB);
                        }else{
                            Move moveQ = new Move(file,rank,checkerFile,checkerRank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveQ.promotionPiece = Constants.BLACK_QUEEN;
                            moves.add(moveQ);
                            Move moveN = new Move(file,rank,checkerFile,checkerRank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveN.promotionPiece = Constants.BLACK_KNIGHT;
                            moves.add(moveN);
                            Move moveR = new Move(file,rank,checkerFile,checkerRank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveR.promotionPiece = Constants.BLACK_ROOK;
                            moves.add(moveR);
                            Move moveB = new Move(file,rank,checkerFile,checkerRank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveB.promotionPiece = Constants.BLACK_BISHOP;
                            moves.add(moveB);
                        }
                    }else {
                        moves.add(new Move(file,rank,checkerFile,checkerRank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
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
                            if (cb.whiteToMove && rank>checkerRank) {
                                if ((rank == 6 && rank - checkerRank<3) || ((rank - checkerRank<2))){
                                    for(int i=rank-1;i>=checkerRank;i--){
                                        if(cb.board[i][file] != Constants.EMPTY_SQUARE){
                                            break;
                                        } else if(i==checkerRank){
                                            if(i==0){
                                                Move moveQ = new Move(file,rank,file,i,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                                                moveQ.promotionPiece = Constants.WHITE_QUEEN;
                                                moves.add(moveQ);
                                                Move moveN = new Move(file,rank,file,i,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                                                moveN.promotionPiece = Constants.WHITE_KNIGHT;
                                                moves.add(moveN);
                                                Move moveR = new Move(file,rank,file,i,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                                                moveR.promotionPiece = Constants.WHITE_ROOK;
                                                moves.add(moveR);
                                                Move moveB = new Move(file,rank,file,i,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                                                moveB.promotionPiece = Constants.WHITE_BISHOP;
                                                moves.add(moveB);
                                            }else {
                                                moves.add(new Move(file, rank, file, i, cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
                                            }
                                            break;
                                        }
                                    }
                                }
                            } else if(!cb.whiteToMove && rank<checkerRank) {
                                if ((rank == 1 && checkerRank-rank  <3) || ((checkerRank-rank<2))){

                                    for(int i=rank+1;i<=checkerRank;i++){
                                        if(cb.board[i][file] != Constants.EMPTY_SQUARE){
                                            break;
                                        } else if(i==checkerRank){
                                            if(i==7){
                                                Move moveQ = new Move(file,rank,file,i,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                                                moveQ.promotionPiece = Constants.BLACK_QUEEN;
                                                moves.add(moveQ);
                                                Move moveN = new Move(file,rank,file,i,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                                                moveN.promotionPiece = Constants.BLACK_KNIGHT;
                                                moves.add(moveN);
                                                Move moveR = new Move(file,rank,file,i,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                                                moveR.promotionPiece = Constants.BLACK_ROOK;
                                                moves.add(moveR);
                                                Move moveB = new Move(file,rank,file,i,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                                                moveB.promotionPiece = Constants.BLACK_BISHOP;
                                                moves.add(moveB);
                                            }else {
                                                moves.add(new Move(file, rank, file, i, cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
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
                            if (cb.whiteToMove && rank>dstRank) {
                                if ((rank == 6 && rank - dstRank<3) || ((rank - dstRank<2))){
                                    for(int i=rank-1;i>=dstRank;i--){
                                        if(cb.board[i][file] != Constants.EMPTY_SQUARE){
                                            break;
                                        } else if(i==dstRank){
                                                moves.add(new Move(file, rank, file, i, cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
                                            break;
                                        }
                                    }
                                }
                            } else if(!cb.whiteToMove && rank<dstRank) {
                                if ((rank == 1 && dstRank-rank  <3) || ((dstRank-rank<2))){
                                    for(int i=rank+1;i<=dstRank;i++){
                                        if(cb.board[i][file] != Constants.EMPTY_SQUARE){
                                            break;
                                        } else if(i==dstRank){
                                                moves.add(new Move(file, rank, file, i, cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }



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
                        if(cb.whiteToMove){
                            Move moveQ = new Move(file,rank,f,r,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveQ.promotionPiece = Constants.WHITE_QUEEN;
                            moves.add(moveQ);
                            Move moveN = new Move(file,rank,f,r,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveN.promotionPiece = Constants.WHITE_KNIGHT;
                            moves.add(moveN);
                            Move moveR = new Move(file,rank,f,r,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveR.promotionPiece = Constants.WHITE_ROOK;
                            moves.add(moveR);
                            Move moveB = new Move(file,rank,f,r,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveB.promotionPiece = Constants.WHITE_BISHOP;
                            moves.add(moveB);
                        }else{
                            Move moveQ = new Move(file,rank,f,r,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveQ.promotionPiece = Constants.BLACK_QUEEN;
                            moves.add(moveQ);
                            Move moveN = new Move(file,rank,f,r,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveN.promotionPiece = Constants.BLACK_KNIGHT;
                            moves.add(moveN);
                            Move moveR = new Move(file,rank,f,r,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveR.promotionPiece = Constants.BLACK_ROOK;
                            moves.add(moveR);
                            Move moveB = new Move(file,rank,f,r,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveB.promotionPiece = Constants.BLACK_BISHOP;
                            moves.add(moveB);
                        }

                    }else {
                        moves.add(new Move(file,rank,f,r,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
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
                if(cb.board[r][f] != Constants.EMPTY_SQUARE && Util.isEnemyPiece(cb.whiteToMove,cb.board[r][f])){

                    if(r == 0 || r == 7){
                        if(cb.whiteToMove){
                            Move moveQ = new Move(file,rank,f,r,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveQ.promotionPiece = Constants.WHITE_QUEEN;
                            moves.add(moveQ);
                            Move moveN = new Move(file,rank,f,r,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveN.promotionPiece = Constants.WHITE_KNIGHT;
                            moves.add(moveN);
                            Move moveR = new Move(file,rank,f,r,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveR.promotionPiece = Constants.WHITE_ROOK;
                            moves.add(moveR);
                            Move moveB = new Move(file,rank,f,r,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveB.promotionPiece = Constants.WHITE_BISHOP;
                            moves.add(moveB);
                        }else{
                            Move moveQ = new Move(file,rank,f,r,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveQ.promotionPiece = Constants.BLACK_QUEEN;
                            moves.add(moveQ);
                            Move moveN = new Move(file,rank,f,r,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveN.promotionPiece = Constants.BLACK_KNIGHT;
                            moves.add(moveN);
                            Move moveR = new Move(file,rank,f,r,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveR.promotionPiece = Constants.BLACK_ROOK;
                            moves.add(moveR);
                            Move moveB = new Move(file,rank,f,r,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            moveB.promotionPiece = Constants.BLACK_BISHOP;
                            moves.add(moveB);
                        }
                    }else {
                        moves.add(new Move(file,rank,f,r,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
                    }
                }
            }
        }

        if(cb.enPassantSquare.equals("-")){
            // do nothing
        }else{
            int dr = cb.whiteToMove?2:5;
            if(Math.abs(file-Constants.FILES.indexOf(cb.enPassantSquare.charAt(0))) == 1){
                int[] kingPosition = cb.kingPosition();
                int enPassantPawn = Constants.FILES.indexOf(cb.enPassantSquare.charAt(0)),direction = Util.getSign(kingPosition[0],file);
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
                            Move move = new Move(file, rank, Constants.FILES.indexOf(cb.enPassantSquare.charAt(0)), dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                            move.isEnPassant = true;
                            moves.add(move);
                        }
                    }else {
                        Move move = new Move(file, rank, Constants.FILES.indexOf(cb.enPassantSquare.charAt(0)), dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock);
                        move.isEnPassant = true;
                        moves.add(move);
                    }
                }
            }
        }

        return moves;
    }


    public ArrayList<Move> queenCaptures(final int file,final int rank){
        int pinnedIndex = file + rank * 8;
        if(cb.pinnedPieces.containsKey(pinnedIndex)) {
            if (cb.gs == GameState.CHECK) {
                return moves; // a pinned piece cannot resolve check
            }
            int[] pinDirection = Constants.ALL_DIRECTIONS[cb.pinnedPieces.get(pinnedIndex)];
            int tf = file+pinDirection[0],tr = rank+pinDirection[1];
            while(cb.board[tr][tf] == Constants.EMPTY_SQUARE ){
                tf += pinDirection[0];
                tr += pinDirection[1];
            }
            moves.add(new Move(file,rank,tf,tr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
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
                moves.add(new Move(file,rank,checkerFile,checkerRank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
            }


            return moves;
        }

        int df,dr;
        for(int[] direction:Constants.ALL_DIRECTIONS){
            df = file + direction[0];
            dr = rank + direction[1];
            while(Util.isValid(df,dr)){
                if(cb.board[dr][df] == Constants.EMPTY_SQUARE){

                }else{
                    if(Util.isAlly(cb.board[rank][file],cb.board[dr][df])){

                    }else{
                        moves.add(new Move(file,rank,df,dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
                    }
                    break;
                }
                df += direction[0];
                dr += direction[1];
            }
        }
        
        return moves;
    }

    public ArrayList<Move> queen(final int file,final int rank){//complete
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
                        moves.add(new Move(file, rank, df, dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
                    }else{
                        foundEnemyPiece = true;
                        moves.add(new Move(file, rank, df, dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
                        df = file;
                        dr = rank;
                    }
                }else {
                    df -= pinDirection[0];
                    dr -= pinDirection[1];
                    if (cb.board[dr][df] == Constants.EMPTY_SQUARE) {
                        moves.add(new Move(file, rank, df, dr, cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
                    } else {
                        foundKing = true;
                    }

                }
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
                moves.add(new Move(file,rank,checkerFile,checkerRank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
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
                                moves.add(new Move(file,rank,checkerFile,r,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
                            }
                        }
                    }else if(checkDirection[1] == 0){
                        int f = kingPosition[0];
                        while ((f += checkDirection[0]) != checkerFile) {
                            if(cb.canSlide(file,rank,f,checkerRank)){
                                moves.add(new Move(file,rank,f,checkerRank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
                            }
                        }
                    }
                    else {
                        int f = kingPosition[0], r = kingPosition[1];
                        while ((f += checkDirection[0]) != checkerFile && (r += checkDirection[1]) != checkerRank) {
                            if(cb.canSlide(file,rank,f,r)){
                                moves.add(new Move(file,rank,f,r,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
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
                    moves.add(new Move(file,rank,df,dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
                }else{
                    if(Util.isAlly(cb.board[rank][file],cb.board[dr][df])){

                    }else{
                        moves.add(new Move(file,rank,df,dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
                    }
                    break;
                }
                df += direction[0];
                dr += direction[1];
            }
        }
        return moves;
    }


    public ArrayList<Move> rookCaptures(final int file,final int rank){
        int pieceIndex = file + rank * 8;
        if(cb.pinnedPieces.containsKey(pieceIndex)){
            if(cb.gs == GameState.CHECK){
                return moves;
            }
            int[] pinDirection = Constants.ALL_DIRECTIONS[cb.pinnedPieces.get(pieceIndex)];
            if(pinDirection[0] != 0 && pinDirection[1] != 0){
                // a rook pinned by bishop or diagonally by queen cannot move
            }else{
                int tf = file+pinDirection[0],tr = rank+pinDirection[1];
                while(cb.board[tr][tf] == Constants.EMPTY_SQUARE ){
                    tf += pinDirection[0];
                    tr += pinDirection[1];
                }
                moves.add(new Move(file,rank,tf,tr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
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
                    moves.add(new Move(file,rank,checkerFile,checkerRank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
                }
            }

            return moves;
        }


        int df,dr;
        for(int i=0;i<4;i++){
            df = file + Constants.ALL_DIRECTIONS[i][0];
            dr = rank + Constants.ALL_DIRECTIONS[i][1];
            while(Util.isValid(df,dr)){
                if(cb.board[dr][df] == Constants.EMPTY_SQUARE){

                }else{
                    if(Util.isAlly(cb.board[rank][file],cb.board[dr][df])){

                    }else{
                        moves.add(new Move(file,rank,df,dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
                    }
                    break;
                }
                df += Constants.ALL_DIRECTIONS[i][0];
                dr += Constants.ALL_DIRECTIONS[i][1];
            }
        }



        return moves;
    }

    public ArrayList<Move> rook(final int file,final int rank){//complete
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
                            moves.add(new Move(file, rank, df, dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
                        }else{
                            foundEnemyPiece = true;
                            moves.add(new Move(file, rank, df, dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
                            df = file;
                            dr = rank;
                        }
                    }else {
                        df -= pinDirection[0];
                        dr -= pinDirection[1];
                        if(cb.board[dr][df] == Constants.EMPTY_SQUARE) {
                            moves.add(new Move(file, rank, df, dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
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
                    moves.add(new Move(file,rank,checkerFile,checkerRank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
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
                                moves.add(new Move(file,rank,kingPosition[0],rank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
                            }
                        }
                    }else if(checkDirection[1] == 0){
                        if(Util.inBetween(kingPosition[0],checkerFile,file)){
                            // means the rook is in between the checker and the king, therefore it might be able to block the check
                            if(cb.canSlide(file,rank,file,kingPosition[1])){
                                moves.add(new Move(file,rank,file,kingPosition[1],cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
                            }
                        }
                    }else{


                        int f=kingPosition[0],r=kingPosition[1];
                        while((f+=checkDirection[0])!=checkerFile&&(r+=checkDirection[1])!=checkerRank){

                            if(f == file || r == rank){
                                //System.out.println(file+" "+rank+" "+f+" "+r);
                                if(cb.canSlide(file,rank,f,r)){
                                    moves.add(new Move(file,rank,f,r,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
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
                    moves.add(new Move(file,rank,df,dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
                }else{
                    if(Util.isAlly(cb.board[rank][file],cb.board[dr][df])){

                    }else{
                        moves.add(new Move(file,rank,df,dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
                    }
                    break;
                }
                df += Constants.ALL_DIRECTIONS[i][0];
                dr += Constants.ALL_DIRECTIONS[i][1];
            }
        }
        return moves;
    }


    public ArrayList<Move> knightCaptures(final int file,final int rank){
        if(cb.pinnedPieces.containsKey(file + rank * 8)){
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

            //capture code
            if(file!=checkerFile&&rank!=checkerRank){
                int[] direction = Util.getDirection(file,rank,checkerFile,checkerRank);
                for(int i=0;i<2;i++){
                    if(file+Constants.KNIGHT_DIRECTION[i][0]*direction[0] == checkerFile && rank+Constants.KNIGHT_DIRECTION[i][1]*direction[1] == checkerRank){
                        moves.add(new Move(file,rank,checkerFile,checkerRank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
                        break;
                    }
                }
            }
            return moves;
        }
        int df,dr;
        for(int[] direction:Constants.KNIGHT_DIRECTION){
            df = file + direction[0];
            dr = rank + direction[1];
            if(Util.isValid(df,dr) && cb.board[dr][df] !=Constants.EMPTY_SQUARE && Util.isEnemyPiece(cb.whiteToMove,cb.board[dr][df])){
                moves.add(new Move(file,rank,df,dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
            }
        }

        return moves;
    }

    public ArrayList<Move> knight(final int file,final int rank){//complete
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
                        moves.add(new Move(file,rank,checkerFile,checkerRank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
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
                                    moves.add(new Move(file,rank,checkerFile,r,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
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
                                    moves.add(new Move(file,rank,f,checkerRank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
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
                                    moves.add(new Move(file,rank,f,r,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
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
                moves.add(new Move(file,rank,df,dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
            }
        }

        return moves;
    }


    public ArrayList<Move> bishopCaptures(final int file,final int rank) {//complete!
        int pinnedIndex = file + rank * 8;
        if (cb.pinnedPieces.containsKey(pinnedIndex)) {
            if (cb.gs == GameState.CHECK) {
                return moves;
            }
            int[] pinDirection = Constants.ALL_DIRECTIONS[cb.pinnedPieces.get(pinnedIndex)];
            if (pinDirection[0] == 0 || pinDirection[1] == 0) {
                // a bishop pinned by rook or horizontally by queen cannot move
            }else{
                int f = file+pinDirection[0],r = rank + pinDirection[1];
                while(cb.board[r][f] == Constants.EMPTY_SQUARE){
                    f += pinDirection[0];
                    r += pinDirection[1];
                }
                moves.add(new Move(file,rank,f,r,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
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


            boolean onSameColoredSquare = (file+rank)%2==(checkerFile+checkerRank)%2;

            if((file!=checkerFile) && (rank!=checkerRank) && onSameColoredSquare&&cb.canSlide(file,rank,checkerFile,checkerRank)){
                moves.add(new Move(file,rank,checkerFile,checkerRank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
            }
            return moves;
        }
        int df,dr;
        for(int i = 4; i<Constants.ALL_DIRECTIONS.length; i++){
            df = file + Constants.ALL_DIRECTIONS[i][0];
            dr = rank + Constants.ALL_DIRECTIONS[i][1];
            while(Util.isValid(df,dr)){
                if(cb.board[dr][df] == Constants.EMPTY_SQUARE){

                }else{
                    if(Util.isAlly(cb.board[rank][file],cb.board[dr][df])){

                    }else{
                        moves.add(new Move(file,rank,df,dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
                    }
                    break;
                }
                df += Constants.ALL_DIRECTIONS[i][0];
                dr += Constants.ALL_DIRECTIONS[i][1];
            }
        }



        return moves;
    }

    public ArrayList<Move> bishop(final int file,final int rank){//complete!
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
                            moves.add(new Move(file, rank, df, dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
                        }else{
                            foundEnemyPiece = true;
                            moves.add(new Move(file, rank, df, dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
                            df = file;
                            dr = rank;
                        }
                    }else {
                        df -= pinDirection[0];
                        dr -= pinDirection[1];
                        if(cb.board[dr][df] == Constants.EMPTY_SQUARE) {
                            moves.add(new Move(file, rank, df, dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
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
                moves.add(new Move(file,rank,checkerFile,checkerRank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
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
                                    moves.add(new Move(file,rank,checkerFile,r,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
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
                                    moves.add(new Move(file,rank,f,checkerRank,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
                                }
                            }
                        }
                    }else{
                        int[] kingPosition = cb.kingPosition();
                        int f = kingPosition[0],r = kingPosition[1];
                        while((f+=checkDirection[0])!=checkerFile&&(r+=checkDirection[1])!=checkerRank){
                            if((file!=f) && (rank!=r)&&(file+rank)%2==(f+r)%2){
                                if(cb.canSlide(file,rank,f,r)){
                                    moves.add(new Move(file,rank,f,r,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
                                }
                            }

                        }

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
                    moves.add(new Move(file,rank,df,dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
                }else{
                    if(Util.isAlly(cb.board[rank][file],cb.board[dr][df])){

                    }else{
                        moves.add(new Move(file,rank,df,dr,cb.board,cb.castlingFEN,cb.enPassantSquare,cb.halfMoveClock));
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
