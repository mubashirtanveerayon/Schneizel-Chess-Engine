package server.board;

import server.util.*;

import java.util.*;

public class ChessBoard {

    public char[][] board;
    public char turn;
    public String[] fenParts;
    public int[] whiteKingPosition,blackKingPosition;
    public HashMap<Integer,Integer> pinnedPieces; // 1st element holds the 1D index of the pinned piece, 2nd element holds the index of Constants.HORIZONTAL_AND_DIAGONAL_DIRECTIONS it is being pinned from
    public HashMap<Integer,Integer> checkers; // 1D index of the opponent piece(the piece attacking the king)
                                                // index of direction to checker

    public GameState gs;

    public Set<Integer> pieceLocations /*, attackedSquares*/;// holds 1D index of piece locations in a 2D array

    public ChessBoard(){
        fenParts = FenUtils.split(Constants.STARTING_FEN);
        gs =  GameState.NORMAL;
        initialize();
    }

    public ChessBoard(String fen){
        fenParts = FenUtils.split(fen);
        initialize();
    }

    public ChessBoard(String fen,boolean checkFen) {
        if(checkFen) {
            try {
                FenUtils.isValid(fen);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        fenParts = FenUtils.split(fen);
        initialize();
    }

    private void initialize(){
        //initialization of variables
        board = FenUtils.parse(fenParts);
        turn = fenParts[8].charAt(0);
        pinnedPieces = new HashMap<>();
        pieceLocations = new HashSet<>();
        //attackedSquares = new HashSet<>();
        whiteKingPosition = new int[2];
        blackKingPosition = new int[2];
        checkers = new HashMap<>();
        for(int i=0;i<8;i++){
            for(int j=0;j<8;j++){
                if(board[i][j] == Constants.WHITE_KING){
                    whiteKingPosition[0] = j;
                    whiteKingPosition[1] = i;
                }else if(board[i][j] == Constants.BLACK_KING){
                    blackKingPosition[0] = j;
                    blackKingPosition[1] = i;
                }
                if(board[i][j] != Constants.EMPTY_SQUARE){
                    pieceLocations.add(j+i*8);
                }
            }
        }

        checkBoard();

    }

    public void resetStats(){
        pinnedPieces.clear();
        //attackedSquares.clear();
        checkers.clear();
        gs = GameState.NORMAL;
    }

    public void _checksAndPinnedPieces(){
        resetStats();
        int[] kingPosition = kingPosition();
        int file,rank,pinnedPieceIndex=0;
        boolean foundAlly,foundOpponentPiece;
        for(int i = 0; i<Constants.ALL_DIRECTIONS.length; i++){
            file = kingPosition[0]+Constants.ALL_DIRECTIONS[i][0];
            rank = kingPosition[1]+Constants.ALL_DIRECTIONS[i][1];
            foundOpponentPiece = false;
            foundAlly = false;
            while(Util.isValid(file,rank)){
                if(board[rank][file] == Constants.EMPTY_SQUARE){
                    file += Constants.ALL_DIRECTIONS[i][0];
                    rank += Constants.ALL_DIRECTIONS[i][1];
                    continue;
                }
                if(!Util.isEnemyPiece(turn,board[rank][file])){
                    if(foundAlly){
                        break;
                    }else {
                        foundAlly = true;
                        pinnedPieceIndex = file + rank * 8;
                        pinnedPieces.put(pinnedPieceIndex,i);
                    }
                }


                else if(Character.toUpperCase(board[rank][file]) != Constants.WHITE_PAWN && Character.toUpperCase(board[rank][file]) != Constants.WHITE_KNIGHT){
                    foundOpponentPiece = true;
                    if (!foundAlly) {
                        gs = GameState.CHECK;
                        checkers.put(file + rank * 8,i);
                    }
                    break;
                }

                else if(Character.toUpperCase(board[rank][file]) != Constants.WHITE_KNIGHT){
                    // found opponent pawn
                    if(Math.abs(kingPosition[0] - file) == 1){
                        if(turn == Constants.WHITE){
                            if(kingPosition[1] > rank){
                                gs = GameState.CHECK;
                                checkers.put(file + rank * 8,i);
                            }
                        }else{
                            if(kingPosition[1] < rank){
                                gs = GameState.CHECK;
                                checkers.put(file + rank * 8,i);
                            }
                        }
                    }
                    break;
                }else{
                    break;
                }
                file += Constants.ALL_DIRECTIONS[i][0];
                rank += Constants.ALL_DIRECTIONS[i][1];
            }
            if(!foundOpponentPiece && foundAlly){
                pinnedPieces.remove(pinnedPieceIndex);
            }
        }

        for(int i =0;i<Constants.KNIGHT_DIRECTION.length;i++){
            file = kingPosition[0]+Constants.KNIGHT_DIRECTION[i][0];
            rank = kingPosition[1]+Constants.KNIGHT_DIRECTION[i][1];
            if(Util.isValid(file,rank)){
                switch(board[rank][file]){
                    case Constants.BLACK_KNIGHT:
                        if(turn == Constants.WHITE){
                            gs = GameState.CHECK;
                            checkers.put(file + rank * 8,i);
                        }
                    case Constants.WHITE_KNIGHT:
                        if(turn == Constants.BLACK){
                            gs = GameState.CHECK;
                            checkers.put(file + rank * 8,i);
                        }
                }

            }
        }

    }


    public String stats(){
        StringBuilder stats = new StringBuilder("Pinned pieces: \n");
        for (int pieceIndex : pinnedPieces.keySet()){
            char piece = board[pieceIndex/8][pieceIndex%8];
            int[] direction = Constants.ALL_DIRECTIONS[pinnedPieces.get(pieceIndex)];
            stats.append(Util.cvtCoord(pieceIndex)).append(" ").append(piece).append(" direction to pinner: [").append(direction[0]).append(", ").append(direction[1]).append("]") .append("\n");
        }

        stats.append("Checkers: \n");
        for (int pieceIndex : checkers.keySet()){
            char piece = board[pieceIndex/8][pieceIndex%8];
            int[] direction = Character.toUpperCase(piece) == Constants.WHITE_KNIGHT?Constants.KNIGHT_DIRECTION[checkers.get(pieceIndex)]:Constants.ALL_DIRECTIONS[checkers.get(pieceIndex)];
            stats.append(Util.cvtCoord(pieceIndex)).append(" ").append(piece).append(" direction to checker: [").append(direction[0]).append(", ").append(direction[1]).append("]") .append("\n");
        }

        stats.append("Pieces at squares:\n");
        for (int pieceIndex:pieceLocations){
            stats.append(Util.cvtCoord(pieceIndex)).append(" ").append(board[pieceIndex/8][pieceIndex%8]).append("\n");
        }

        stats.append("White king position: ").append(Constants.FILES.charAt(whiteKingPosition[0])).append(8-whiteKingPosition[1]).append(" [").append(whiteKingPosition[0]).append(", ").append(whiteKingPosition[1]).append("]").append("\n");
        stats.append("Black king position: ").append(Constants.FILES.charAt(blackKingPosition[0])).append(8-blackKingPosition[1]).append(" [").append(blackKingPosition[0]).append(", ").append(blackKingPosition[1]).append("]").append("\n");

        stats.append("Turn: ").append(turn).append("\n");

        stats.append(Util.getBoardVisualStd(board)).append("\n");

        stats.append("Fen: ").append(FenUtils.cat(fenParts));


        return stats.toString();
    }


//    public long generateZobristKey(boolean positionOnly){
//        long key = 0;
//        for(int pieceIndex:pieceLocations){
//            switch(board[pieceIndex/8][pieceIndex%8]){
//                case Constants.WHITE_PAWN:
//                    key ^= Constants.zobristArray[1][0][pieceIndex];
//                    break;
//                case Constants.BLACK_PAWN:
//                    key ^= Constants.zobristArray[0][0][pieceIndex];
//                    break;
//                case Constants.WHITE_QUEEN:
//                    key ^= Constants.zobristArray[1][1][pieceIndex];
//                    break;
//                case Constants.BLACK_QUEEN:
//                    key ^= Constants.zobristArray[0][1][pieceIndex];
//                    break;
//                case Constants.WHITE_ROOK:
//                    key ^= Constants.zobristArray[1][2][pieceIndex];
//                    break;
//                case Constants.BLACK_ROOK:
//                    key ^= Constants.zobristArray[0][2][pieceIndex];
//                    break;
//                case Constants.WHITE_BISHOP:
//                    key ^= Constants.zobristArray[1][3][pieceIndex];
//                    break;
//                case Constants.BLACK_BISHOP:
//                    key ^= Constants.zobristArray[0][3][pieceIndex];
//                    break;
//                case Constants.WHITE_KNIGHT:
//                    key ^= Constants.zobristArray[1][4][pieceIndex];
//                    break;
//                case Constants.BLACK_KNIGHT:
//                    key ^= Constants.zobristArray[0][4][pieceIndex];
//                    break;
//                case Constants.WHITE_KING:
//                    key ^= Constants.zobristArray[1][5][pieceIndex];
//                    break;
//                case Constants.BLACK_KING:
//                    key ^= Constants.zobristArray[0][5][pieceIndex];
//                    break;
//            }
//        }
//        if (turn == Constants.WHITE){
//            key ^= Constants.zobristTurnToMove;
//        }
//        if(positionOnly){
//            return key;
//        }
//        if(!fenParts[9].equals("-")){
//            for(char c:fenParts[9].toCharArray()){
//                key ^= (long)Util.getPieceValue(c);
//            }
//        }
//        if(!fenParts[10].equals("-")){
//            key ^= Util.getSquareIndex(fenParts[10]);
//        }
//        return key;
//    }
    

    public void checkBoard(){
        resetStats();
        
        int[] kingPos = kingPosition();
        int file,rank ,pinnedPieceIndex=0;
        boolean foundAlly,foundEnemyPiece;

        for(int i=0;i<Constants.ALL_DIRECTIONS.length;i++){
            foundAlly = false;
            foundEnemyPiece = false;
            file = kingPos[0]+Constants.ALL_DIRECTIONS[i][0];
            rank = kingPos[1]+Constants.ALL_DIRECTIONS[i][1];
            while(!foundEnemyPiece&&Util.isValid(file, rank)){
                if(board[rank][file] != Constants.EMPTY_SQUARE){

                    if(Util.isEnemyPiece(turn,board[rank][file])){

                        foundEnemyPiece = true;
                        switch(Character.toUpperCase(board[rank][file])){
                            case Constants.WHITE_KNIGHT:
                            case Constants.WHITE_KING:
                                if(foundAlly){
                                    pinnedPieces.remove(pinnedPieceIndex);
                                }
                                break;
                            case Constants.WHITE_PAWN:
                                if(foundAlly){
                                    pinnedPieces.remove(pinnedPieceIndex);
                                }
                                boolean inCheck = ((turn == Constants.WHITE && rank+1 == kingPos[1]) || (turn == Constants.BLACK && rank-1 == kingPos[1])) && Math.abs(kingPos[0]-file) == 1;
                                if (inCheck){
                                    gs = GameState.CHECK;
                                    checkers.put(file + rank * 8,i);
                                }
                                break;
                            case Constants.WHITE_QUEEN:{
                                if(!foundAlly){
                                    gs = GameState.CHECK;
                                    checkers.put(file + rank * 8,i);
                                }
                                break;
                            }
                            case Constants.WHITE_ROOK:{
                                if(Constants.ALL_DIRECTIONS[i][0] == 0  || Constants.ALL_DIRECTIONS[i][1] == 0){
                                    if(!foundAlly){
                                        gs = GameState.CHECK;
                                        checkers.put(file + rank * 8,i);
                                    }
                                }else{
                                    if(foundAlly){
                                        pinnedPieces.remove(pinnedPieceIndex);

                                    }
                                }
                                break;
                            }
                            case Constants.WHITE_BISHOP:{
                                if(Constants.ALL_DIRECTIONS[i][0] != 0  && Constants.ALL_DIRECTIONS[i][1] != 0){
                                    if(!foundAlly){
                                        gs = GameState.CHECK;
                                        checkers.put(file + rank * 8,i);
                                    }
                                }else{
                                    if(foundAlly){
                                        pinnedPieces.remove(pinnedPieceIndex);

                                    }
                                }
                                break;
                            }


                        }
                    }else{
                        if(foundAlly){
                            break;
                        }else{
                            foundAlly = true;
                            pinnedPieceIndex = file + rank * 8;
                            pinnedPieces.put(pinnedPieceIndex,i);
                        }
                    }
                }
                file+=Constants.ALL_DIRECTIONS[i][0];
                rank+=Constants.ALL_DIRECTIONS[i][1];

            }
            if(foundAlly && !foundEnemyPiece){
                pinnedPieces.remove(pinnedPieceIndex);

            }

        }


        for(int i =0;i<Constants.KNIGHT_DIRECTION.length;i++){
            file = kingPos[0]+Constants.KNIGHT_DIRECTION[i][0];
            rank = kingPos[1]+Constants.KNIGHT_DIRECTION[i][1];
            if(Util.isValid(file,rank)){
                switch(board[rank][file]){
                    case Constants.BLACK_KNIGHT:
                        if(turn == Constants.WHITE){
                            gs = GameState.CHECK;
                            checkers.put(file + rank * 8,i);
                        }
                        break;
                    case Constants.WHITE_KNIGHT:
                        if(turn == Constants.BLACK){
                            gs = GameState.CHECK;
                            checkers.put(file + rank * 8,i);

                        }

                        break;
                }

            }
        }
    }





//rnbq1bnr/ppppkppp/3Np3/8/8/8/PPPPPPPP/R1BQKBNR b KQ - 3 3

//    public void checkForAttackedSquares(){
//        for(Integer coord:pieceLocations){
//            int file = coord%8;
//            int rank = coord/8;
//            if(!Util.isEnemyPiece(turn,board[rank][file])){
//                continue;
//            }
//            switch(Character.toUpperCase(board[rank][file])){
//                case Constants.WHITE_PAWN:
//                    int dstFile = file-1,dstRank = rank-1;
//                    if(Util.isValid(dstFile,dstRank)){
//                        attackedSquares.add(dstFile+dstRank*8);
//                    }
//                    dstFile = file+1;
//                    if(Util.isValid(dstFile,dstRank)){
//                        attackedSquares.add(dstFile+dstRank*8);
//                    }
//                    break;
//                case Constants.WHITE_KNIGHT:
//                    for(int[] dir:Constants.KNIGHT_DIRECTION){
//                        int f = file+dir[0];
//                        int r = rank+dir[1];
//                        if(Util.isValid(f,r)){
//                            attackedSquares.add(f+r*8);
//                        }
//                    }
//                    break;
//                case Constants.WHITE_KING:
//                    for(int[] dir:Constants.ALL_DIRECTIONS){
//                        int f = file+dir[0];
//                        int r = rank+dir[1];
//                        if(Util.isValid(f,r)){
//                            attackedSquares.add(f+r*8);
//                        }
//                    }
//                    break;
//                default:
//                    int startIndex=0,endIndex=Constants.ALL_DIRECTIONS.length-1;
//                    if(Character.toUpperCase(board[rank][file]) == Constants.WHITE_BISHOP){
//                        startIndex = 4;
//                    }else if(Character.toUpperCase(board[rank][file]) == Constants.WHITE_ROOK){
//                        endIndex = 3;
//                    }
//                    for(int i=startIndex;i<=endIndex;i++){
//                        int f=file,r=rank;
//                        while(Util.isValid(f+=Constants.ALL_DIRECTIONS[i][0],r+=Constants.ALL_DIRECTIONS[i][1])&&board[r][f] == Constants.EMPTY_SQUARE){
//                            attackedSquares.add(f+r*8);
//                        }
//                    }
//            }
//        }
//    }
//

    public boolean canSlide(int fromF, int fromR, int toF, int toR){
        int[] direction = Util.getDirection(fromF,fromR,toF,toR);
        //System.out.println(fromF+" "+fromR+" "+toF+" "+toR);
        if(direction[0] != 0 && direction[1] != 0 && Math.abs(fromF-toF) != Math.abs(fromR-toR)){
            return false;
        }

        int f=fromF+direction[0],r = fromR+direction[1];
        while(Util.isValid(f,r)){
            if(f == toF && r == toR){
                return true;
            }else if(board[r][f] == Constants.EMPTY_SQUARE) {
                f += direction[0];
                r += direction[1];
            }else{
                break;
            }
        }
        return false;
    }


    public boolean squareUnderAttack(final int file,final int rank){
        for(int[] dir:Constants.ALL_DIRECTIONS){
            int f = file,r = rank;
            for(int i=0;Util.isValid(f+=dir[0],r+=dir[1]);i++){
                if(board[r][f] == Constants.EMPTY_SQUARE){
                    continue;
                }else if(Util.isEnemyPiece(turn,board[r][f])){
                    switch(Util.toUpper(board[r][f])){
                        case Constants.WHITE_QUEEN:{
                            return true;
                        }
                        case Constants.WHITE_ROOK:{
                            if(dir[0] == 0 || dir[1] == 0){
                                return true;
                            }
                            break;
                        }
                        case Constants.WHITE_BISHOP:{
                            if(dir[0] == 0 || dir[1] == 0){
                                break;
                            }else{
                                return true;
                            }
                        }
                        case Constants.WHITE_KING:{
                            if(i==0){
                                return true;
                            }
                            break;
                        }
                        case Constants.WHITE_PAWN:{
                            if(i==0 && dir[0] != 0 && dir[1] != 0){
                                if((turn == Constants.BLACK && rank<r) || (turn == Constants.WHITE && rank>r)){
                                    return true;
                                }
                            }
                            break;
                        }
                    }
                }
                break;
            }

        }

        for(int[] dir:Constants.KNIGHT_DIRECTION){
            int f = file+dir[0],r = rank+dir[1];
            if(Util.isValid(f,r) && board[r][f] != Constants.EMPTY_SQUARE && Util.isEnemyPiece(turn,board[r][f])){
                if(Character.toUpperCase(board[r][f]) == Constants.WHITE_KNIGHT){
                    return true;
                }
            }
        }

        return false;
    }


public boolean _isSquareSafe(int file,int rank){
        for(int[] dir:Constants.ALL_DIRECTIONS){
            int f=file,r=rank;
            for(int i=0;Util.isValid(f+=dir[0],r+=dir[1]);i++){
                if(board[r][f] != Constants.EMPTY_SQUARE){
                    if(Util.isEnemyPiece(turn,board[r][f])){
                        char piece = Character.toUpperCase(board[r][f]);
                        switch(piece){
                            case Constants.WHITE_PAWN:{
                                if(i==0){
                                    if(turn == Constants.WHITE){
                                        if(Math.abs(file-f) == 1 && (rank-r) == 1){
                                            return false;
                                        }
                                    }else{
                                        if(Math.abs(file-f) == 1 && (rank-r) == -1){
                                            return false;
                                        }
                                    }
                                }
                                break;
                            }
                            case Constants.WHITE_KING:{
                                if(i==0) {
                                    return false;
                                }
                                break;
                            }
                            case Constants.WHITE_QUEEN:{
                                return false;
                            }
                            case Constants.WHITE_ROOK:{
                                if(dir[0] == 0 || dir[1] == 0){
                                    return false;
                                }
                                break;
                            }
                            case Constants.WHITE_BISHOP:{
                                if(!(dir[0] == 0 || dir[1] == 0)){
                                    return false;
                                }
                                break;
                            }
                        }
                    }

                    break;
                }
            }
        }
        for(int[] dir:Constants.KNIGHT_DIRECTION){
            int f = file+dir[0],r = rank+dir[1];
            if(Util.isValid(f,r) && board[r][f] != Constants.EMPTY_SQUARE && Util.isEnemyPiece(turn,board[r][f])){
                if(Character.toUpperCase(board[r][f]) == Constants.WHITE_KNIGHT){
                    return false;
                }
            }
        }
        return true;
    }

    public int[] kingPosition(){
        return turn == Constants.WHITE?whiteKingPosition:blackKingPosition;
    }


    public int countPieces(boolean white) {

        int pieces = 0;
        for(int index:pieceLocations){
            if((white && Character.isUpperCase(board[index/8][index%8])) || (!white && !Character.isUpperCase(board[index/8][index%8]))){
                pieces += 1;
            }
        }
        return pieces;
    }

}
