package engine;

import server.board.ChessBoard;
import server.evaluation.Evaluation;
import server.move.MoveManager;
import server.util.PGNUtils;
import server.util.Constants;
import server.util.FenUtils;
import server.util.GameState;
import server.util.Util;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

public class Engine implements Runnable{

    public ChessBoard cb;
    public MoveManager mm;

    private Evaluation ev;

    private String engineMove="";


    private boolean searchCancelled = false,searching = false,stopped = false;


    private ArrayList<HashMap<String,String>> book;

    private boolean useBook = true;

    private boolean useTranspositionTable = false;


    static final int MAX_PLY_TO_USE_BOOK = 8;
    static final int DEFAULT_SEARCH_DEPTH = 4;

    int bookMoveType = Constants.BOOK_RANDOM;

    long searchStartTime = 0;

    private int depth = 4;


//    public HashMap<String, Float> transpositionTable;

    public static final int MAX_TABLE_ENTRIES = 100000;


    public boolean isSearching(){
        return searching;
    }

    public void cancelSearch(){
        searchCancelled = true;
    }

    public void stopSearch(){
        stopped = true;
        searchCancelled = true;
    }

    public String cvtToMove(String algebraic){
        return mm.parse(algebraic);
    }

    public String cvtToAlgebraic(String move){
        return mm.cvt(move);
    }

    public String moveGenerationTest(int depth){
        return mm.moveGenerationTest(depth,true);
    }

    public String getEngineMove(){
        return engineMove;
    }

    public float evaluate(){
        return ev.evaluate();
    }
    public void setDepth(int value){
        if (value>0 && value<6){
            depth = value;
        }
    }

    public int getDepth(){
        return depth;
    }


    public boolean getUseBook(){
        return useBook;
    }

    public void setUseBook(boolean b){
        useBook = b;
    }

    public ArrayList<String> getLegalMoves(){
        return mm.getAllMoves();
    }

    public Engine(String fen){
        cb = new ChessBoard(fen,true);
        mm = new MoveManager(cb);
        ev = new Evaluation(cb);
//        transpositionTable = new HashMap<>();
    }

    public Engine(){
        cb = new ChessBoard();
        mm = new MoveManager(cb);
        ev = new Evaluation(cb);
//        transpositionTable = new HashMap<>();
    }

    public void loadBook(){
        if(book != null || !useBook){
            return;
        }
        File directory = new File("data");
        ArrayList<String> pgnPaths = new ArrayList<>();
        if(directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                if (file.getName().endsWith(".pgn")) {
                   pgnPaths.add(file.getPath());
                }
            }
        }
        if(pgnPaths.isEmpty()) {
            System.out.println("Could not find any book");
            useBook = false;
        }else{
            File pgnFile = new File(pgnPaths.get(new Random().nextInt(pgnPaths.size())));
            book = PGNUtils.parsePGNFile(pgnFile.getPath(),PGNUtils.ALL_GAMES);
            System.out.println("Book loaded: "+pgnFile.getName());
        }
    }


    private String readBook(){
        if(book == null || !useBook){
            return null;
        }
        ArrayList<String> availableMoves = new ArrayList<>();
        //long boardKey = cb.generateZobristKey();
        String fen = FenUtils.cat(cb.fenParts);
        System.out.println("Reading book...");
        for(HashMap<String,String> game:book){
            String moveText = game.get("Moves");
            ChessBoard tcb = new ChessBoard();
            MoveManager tmm = new MoveManager(tcb);
            for(String seg:moveText.split(" ")) {
                String move = PGNUtils.parse(seg,tmm);
                if(move == null){
                    continue;
                }
                if(FenUtils.cat(tcb.fenParts).equals(fen)){//FenUtils.cat(tcb.fenParts).equals(FenUtils.cat(cb.fenParts))//tcb.generateZobristKey() == boardKey
                    availableMoves.add(move);
                    break;
                }else{
                    tmm.makeMove(move);
                }

            }
        }

        System.out.println(availableMoves.size() + " available moves");

        if(availableMoves.isEmpty()){
            return null;
        }

        if(bookMoveType == Constants.BOOK_RANDOM) {
            //return a random move from available moves
            return availableMoves.get(new Random().nextInt(availableMoves.size()));
        }

        if(bookMoveType == Constants.BOOK_MOSTLY_PLAYED) {

            //return the move that has the most occurrence
            String mostOccurred = null;
            int mostOccurrence = 0;
            for (String move : availableMoves) {
                int frequency = Collections.frequency(availableMoves, move);
                if (frequency > mostOccurrence) {
                    mostOccurred = move;
                    mostOccurrence = frequency;
                }
            }
            return mostOccurred;
        }

        //return the best move based on evaluation
        String bestMove=availableMoves.get(0);
        float score,bestScore = Float.NEGATIVE_INFINITY;
        for(String move:availableMoves){
            mm.makeMove(move);
            score = -ev.evaluate();
            mm.undoMove(move);
            if(score > bestScore){
                bestScore = score;
                bestMove = move;
            }
        }
        return bestMove;



    }

    @Override
    public void run(){
        System.out.println("Calculating...");
        if(useBook && Integer.parseInt(cb.fenParts[12])< MAX_PLY_TO_USE_BOOK){
            loadBook();
            String move = readBook();
            if(move != null){
                engineMove = move;
                System.out.println("bestmove "+mm.cvt(engineMove)+ " score book");
                System.out.println("Time taken "+(System.currentTimeMillis() - searchStartTime)+" ms");
                searching = false;
                searchCancelled = false;
                searchStartTime = 0;
                setDepth(DEFAULT_SEARCH_DEPTH);
                return;
            }else{
                useBook = false;
            }
        }

        iterativeDeepening2();
//        System.out.println("Commencing search at depth: "+depth);
//        ArrayList<String> moves = mm.getAllMoves();
//        float score,bestScore = Float.NEGATIVE_INFINITY;
//        orderMove(moves);
//        for(String move:moves){
//            if(searchCancelled){
//                break;
//            }
//            mm.makeMove(move);
//            score = -negamax(depth, Float.NEGATIVE_INFINITY,Float.POSITIVE_INFINITY);
//            mm.undoMove(move);
//            if(score>bestScore){
//                bestScore = score;
//                engineMove = move;
//            }
//            System.out.println(mm.cvt(move)+" score "+score);
//        }
//        searching = false;
//        setDepth(DEFAULT_SEARCH_DEPTH);
//
//        if(searchCancelled){
//            engineMove = "";
//            System.out.println("Search was cancelled");
//        }else{
//            if(engineMove.isEmpty()){
//                engineMove = moves.get(0);
//            }
//            System.out.println("bestmove "+mm.cvt(engineMove)+ " score "+bestScore);
//            System.out.println("Time taken "+(System.currentTimeMillis() - searchStartTime)+" ms");
//        }
//
//        searchCancelled = false;
//        searchStartTime = 0;
//        System.out.println("Cutoffs "+ cutOffs);
//
    }


    private void iterativeDeepening(){
        System.out.println("Commencing iterative deepening search at depth: "+depth);
        float score;
        ArrayList<String> moves = mm.getAllMoves();
        HashMap<String,Float> movesWithScore = new HashMap<>();
        for(String move:moves){
            movesWithScore.put(move,0f);
        }
        //boolean foundMate = false;
        for(int i=1;!searchCancelled && i<=depth;i++){
            System.out.println("Iteration "+i);
            for(String move:movesWithScore.keySet()){
                if(searchCancelled){
                    System.out.println("Search was cancelled");
                    break;
                }
                mm.makeMove(move);
                score = -negamax(i,Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
                mm.undoMove(move);
                movesWithScore.put(move,-score);
                System.out.println(mm.cvt(move)+" score "+score);
            }
            if(!searchCancelled) {
                movesWithScore = Util.sortHashMap(movesWithScore);
            }
//            for(Float eval:movesWithScore.values()){
//                if(String.valueOf(eval).equals("-Infinity")){
//                    System.out.println("Found mate in "+i);
//                    foundMate = true;
//                }
//                break;
//            }
        }



        System.out.println("Time taken "+(System.currentTimeMillis() - searchStartTime)+" ms");
        for(String bestMove:movesWithScore.keySet()){
            engineMove = bestMove;
            break;
        }
        if(!stopped)System.out.println("bestmove "+mm.cvt(engineMove)+" score "+-movesWithScore.get(engineMove));
        setDepth(DEFAULT_SEARCH_DEPTH);
        searching = false;
        searchCancelled = false;
        stopped = false;


//        ArrayList<String> moves = mm.getAllMoves();
//        float score,bestScore = Float.NEGATIVE_INFINITY;
//        orderMove(moves);
//        for(String move:moves){
//            if(searchCancelled){
//                break;
//            }
//            mm.makeMove(move);
//            score = -negamax(depth, Float.NEGATIVE_INFINITY,Float.POSITIVE_INFINITY);
//            mm.undoMove(move);
//            if(score>bestScore){
//                bestScore = score;
//                engineMove = move;
//            }
//            System.out.println(mm.cvt(move)+" score "+score);
//        }


    }

    private void iterativeDeepening2(){//uses slightly different approach to order moves
        System.out.println("Commencing iterative deepening2 with new eval search at depth: "+depth);
        float score;
        ArrayList<String> moves = mm.getAllMoves();

        float[] scores = new float[moves.size()];
        boolean foundMate = false;
        for(int i=1;!foundMate && !searchCancelled && i<=depth;i++){
            System.out.println("Iteration "+i);
            for(int j=0;j<moves.size();j++){
                String move = moves.get(j);
                mm.makeMove(move);
                score = -negamax(i,Float.NEGATIVE_INFINITY,Float.POSITIVE_INFINITY);
                mm.undoMove(move);
                System.out.println(mm.cvt(move)+" score "+score);
                if(searchCancelled){
                    break;
                }
                scores[j] = score;
                for(int k=j-1;k>=0;k--){
                    if(scores[k]<score){
                        scores[j] = scores[k];
                        scores[k] = score;
                        moves.remove(move);
                        moves.add(k,move);
                    }
                }
                if(String.valueOf(score).equals("Infinity")){
                    System.out.println("Found mate in "+i);
                    foundMate = true;
                    break;
                }
            }

        }

        System.out.println("Time taken " + (System.currentTimeMillis() - searchStartTime) + " ms");
        engineMove = moves.get(0);
        if(!stopped)System.out.println("bestmove " + mm.cvt(engineMove) + " score " + scores[0]);
        setDepth(DEFAULT_SEARCH_DEPTH);
        searching = false;
        searchCancelled = false;
        stopped = false;


//        ArrayList<String> moves = mm.getAllMoves();
//        float score,bestScore = Float.NEGATIVE_INFINITY;
//        orderMove(moves);
//        for(String move:moves){
//            if(searchCancelled){
//                break;
//            }
//            mm.makeMove(move);
//            score = -negamax(depth, Float.NEGATIVE_INFINITY,Float.POSITIVE_INFINITY);
//            mm.undoMove(move);
//            if(score>bestScore){
//                bestScore = score;
//                engineMove = move;
//            }
//            System.out.println(mm.cvt(move)+" score "+score);
//        }


    }




    private float negamax(int depth,float alpha,float beta){
        if(searchCancelled){
            return alpha;
        }
        ArrayList<String> moves = mm.getAllMoves();
        if(moves.isEmpty()){
            if(cb.gs == GameState.CHECK){
                return Float.NEGATIVE_INFINITY;
            }else{
                return 0;
            }
        }else if(Integer.parseInt(cb.fenParts[11]) == 100){
            return 0;
        }else if(depth == 0){
//            if(useTranspositionTable) {
//                String fen = FenUtils.cat(cb.fenParts, true);
//                if (transpositionTable.containsKey(fen)) {
//                    return transpositionTable.get(fen);
//                }
//                float eval = ev.evaluate();
//                savePosition(fen, eval);
//                return eval;
//            }else {
//                return searchAllCaptures(alpha,beta);
//            }
            return ev.evaluate();
        }

        //orderMove(moves);
        float score;
        for(String move : moves){
            mm.makeMove(move);
            score = -negamax(depth-1,-beta,-alpha);
            mm.undoMove(move);
            if(score>=beta){
                return beta;
            }
            alpha = Math.max(score,alpha);
        }
        return alpha;
    }


    public void beginSearch(int movetime){
        if(searching){
            return;
        }
        Thread engineThread = new Thread(this);
        engineMove = "";
        searching = true;
        searchCancelled = false;
        stopped = false;
        engineThread.start();
        searchStartTime = System.currentTimeMillis();
        Thread timeThread = new Thread(){
            @Override
            public void run(){
                try {
                    Thread.sleep(movetime);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if(engineThread.isAlive()) {
                    searchCancelled = true;
                }
            }
        };
        timeThread.start();
    }

    public Thread beginSearch(){
        if(searching){
            return null;
        }
        Thread thread = new Thread(this);
        engineMove = "";
        searching = true;
        stopped = false;
        searchCancelled = false;
        thread.start();
        searchStartTime = System.currentTimeMillis();
        return thread;
    }

    public void undoMove(String move){
        mm.undoMove(move);
    }

    public void makeMove(String move){
        mm.makeMove(move);

//        if(!useTranspositionTable){
//            return;
//        }
//        if(!move.contains(Constants.KING_SIDE_CASTLING) ){
//            if(move.charAt(5) != Constants.EMPTY_SQUARE || move.contains(Constants.EN_PASSANT_NOTATION)){
//                transpositionTable.clear();
//            }
//        }
//        System.out.println( "positions stored "+transpositionTable.size());
    }

    public void orderMove(ArrayList<String>moves){
        HashMap<String,Float> movesWithScore = new HashMap<>();
        float score;

        String[] moveParts;
        for(String move:moves){
            score = 0;
            moveParts = move.split(Constants.MOVE_SEPARATOR);
            if(move.contains(Constants.KING_SIDE_CASTLING)){
                score += Constants.CASTLING_SCORE;
            } else {
                if (move.contains(Constants.EN_PASSANT_NOTATION)) {
                    score += Constants.EN_PASSANT_SCORE;
                }else {
                    if (moveParts[1].charAt(0) != Constants.EMPTY_SQUARE) {
                        float valueDiff = Util.getPieceValue(moveParts[1].charAt(0)) - Util.getPieceValue(cb.board[Integer.parseInt(Character.toString(move.charAt(1)))][Integer.parseInt(Character.toString(move.charAt(0)))]);
                        score += Constants.CAPTURE_SCORE * valueDiff;
                    }
                    if (moveParts.length == Constants.PROMOTION_MOVE_LENGTH) {
                        score += Constants.PROMOTION_SCORE + Util.getPieceValue(moveParts[moveParts.length - 1].charAt(0));
                    }
                }
            }


            score = -score;
            movesWithScore.put(move,score);
        }



        movesWithScore = Util.sortHashMap(movesWithScore);

        moves.clear();

        moves.addAll(movesWithScore.keySet());


    }


//    private void savePosition(String fen, float eval) {
//
//
//        if(transpositionTable.size() < MAX_SIZE_TTABLE){
//            transpositionTable.put(fen,eval);
//        }else{
//            for(String key: transpositionTable.keySet()){
//                transpositionTable.remove(key);
//                break;
//            }
//            transpositionTable.put(fen,eval);
//        }
//
//
//    }

//    private void savePosition(String key, float eval) {
//
//
//        if(transpositionTable.size() < MAX_TABLE_ENTRIES){
//            transpositionTable.put(key,eval);
//        }else{
//            for(String k: transpositionTable.keySet()){
//                transpositionTable.remove(k);
//                break;
//            }
//            transpositionTable.put(key,eval);
//        }


//    }



    float searchAllCaptures(float alpha,float beta){
        float eval = ev.evaluate();
        if(eval >= beta){
            return beta;
        }

        alpha = Math.max(alpha,eval);

        if(depth == 0){
            return alpha;
        }

        ArrayList<String> moves = mm.getAllMoves();

        for(String move : moves){
            if(!move.contains(Constants.KING_SIDE_CASTLING) && move.charAt(5) != Constants.EMPTY_SQUARE){
                mm.makeMove(move);
                eval = -searchAllCaptures(-beta,-alpha);
                mm.undoMove(move);
                if(eval>=beta){
                    return beta;
                }
                alpha = Math.max(alpha,eval);
            }
        }
        return alpha;
    }




}
