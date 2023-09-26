package engine;

import server.board.ChessBoard;
import server.evaluation.Evaluation;
import server.move.Move;
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

    private static final int MAX_DEPTH = 5;
    public ChessBoard cb;
    public MoveManager mm;

    private Evaluation ev;

    private Move engineMove=null;


    private boolean searchCancelled = false,searching = false,stopped = false;


    private ArrayList<HashMap<String,String>> book;

    private boolean useBook = true;

    private boolean useTranspositionTable = true;


//    static final int MAX_PLY_TO_USE_BOOK = 8;
    static final int DEFAULT_SEARCH_DEPTH = 3;

    int bookMoveType = Constants.BOOK_RANDOM;

    long searchStartTime = 0;

    private int depth = DEFAULT_SEARCH_DEPTH;


    public HashMap<Long, Float> transpositionTable;

    public static final int MAX_TABLE_ENTRIES = 120000;


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

    public Move cvtToMove(String algebraic){
        return mm.parse(algebraic);
    }

    public String cvtToAlgebraic(Move move){
        return move.toString();
    }

    public String moveGenerationTest(int depth){
        return mm.moveGenerationTest(depth);
    }

    public Move getEngineMove(){
        return engineMove;
    }

    public float evaluate(){
        return ev.evaluate();
    }
    public void setDepth(int value){
        if (value>0 && value<=MAX_DEPTH){
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

    public void setUseTT(boolean b){
        useTranspositionTable = b;
    }

    public ArrayList<Move> getLegalMoves(){
        return mm.getAllMoves();
    }

    public Engine(String fen){
        cb = new ChessBoard(fen,true);
        mm = new MoveManager(cb);
        ev = new Evaluation(cb);
        transpositionTable = new HashMap<>();
    }

    public Engine(){
        cb = new ChessBoard();
        mm = new MoveManager(cb);
        ev = new Evaluation(cb);
        transpositionTable = new HashMap<>();
    }

    public void loadBook(){
        if(book != null || !useBook){
            return;
        }
        File directory = new File("openings");
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


    private Move readBook(){
        if(book == null || !useBook){
            return null;
        }
        ArrayList<Move> availableMoves = new ArrayList<>();
        String fen = FenUtils.generate(cb);
        System.out.println("Reading book...");
        for(HashMap<String,String> game:book){
            String moveText = game.get("Moves");
            ChessBoard tcb = new ChessBoard();
            MoveManager tmm = new MoveManager(tcb);
            for(String seg:moveText.split(" ")) {
                if(seg.isEmpty() || Character.isDigit(seg.charAt(0)) || seg.charAt(0) == '{' ||seg.contains("%") || seg.contains("clk") || seg.contains("eval")){
                    continue;
                }
                Move move = PGNUtils.parse(seg,tmm);
                if(move == null){
                    continue;
                }
                if(FenUtils.generate(tcb).equals(fen)){
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
            Move mostOccurred = null;
            int mostOccurrence = 0;
            for (Move move : availableMoves) {
                int frequency = Collections.frequency(availableMoves, move);
                if (frequency > mostOccurrence) {
                    mostOccurred = move;
                    mostOccurrence = frequency;
                }
            }
            return mostOccurred;
        }

        //return the best move based on evaluation
        Move bestMove=availableMoves.get(0);
        float score,bestScore = -Constants.HIGHEST_VALUE;
        for(Move move:availableMoves){
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


    public void beginSearch(int movetime){
        if(searching){
            return;
        }
        Thread engineThread = new Thread(this);
        engineMove = null;
        searching = true;
        searchCancelled = false;
        stopped = false;
        searchStartTime = System.currentTimeMillis();
        analysisMode();
        engineThread.start();
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
        engineMove = null;
        searching = true;
        stopped = false;
        searchCancelled = false;
        thread.start();
        return thread;
    }


    @Override
    public void run(){
        if(useBook ){// Integer.parseInt(cb.fenParts[12])<MAX_PLY_TO_USE_BOOK
            loadBook();
            Move move = readBook();
            if(move != null){
                engineMove = move;
                System.out.println("bestmove "+engineMove.toString()+ " score book");
                searching = false;
                searchCancelled = false;
                searchStartTime = 0;
                setDepth(DEFAULT_SEARCH_DEPTH);
                return;
            }else{
                useBook = false;
            }
        }


        searchStartTime = System.currentTimeMillis();


        iterativeDeepening();

        System.out.println("Time taken " + (System.currentTimeMillis() - searchStartTime) + " ms");

        if(!stopped || depth == Constants.HIGHEST_VALUE){
            System.out.println("bestmove " + engineMove.toString());
        }
        setDepth(DEFAULT_SEARCH_DEPTH);
        searching = false;
        searchCancelled = false;
        stopped = false;
//        System.out.println("Commencing search at depth: "+depth);
//        ArrayList<String> moves = mm.getAllMoves();
//        float score,bestScore = -Constants.BEST_SCORE;
//        orderMove(moves);
//        for(String move:moves){
//            if(searchCancelled){
//                break;
//            }
//            mm.makeMove(move);
//            score = -negamax(depth, -Constants.BEST_SCORE,Constants.BEST_SCORE);
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


    private void iterativeDeepening(){//uses slightly different approach to order moves
        System.out.println("Commencing iterative deepening with eval3 search at depth: "+depth);
        float score;
        ArrayList<Move> moves = mm.getAllMoves();
        float[] scores = new float[moves.size()];
        boolean foundMate = false;
        for(int i=1;!foundMate && !searchCancelled && i<=depth;i++){
            System.out.println("Iteration "+i);
            for(int j=0;j<moves.size();j++){
                Move move = moves.get(j);
                mm.makeMove(move);
                score = -negamax(i,-Constants.HIGHEST_VALUE,Constants.HIGHEST_VALUE);
                mm.undoMove(move);
                if(searchCancelled){
                    System.out.println("Stopping search");
                    break;
                }
                System.out.println(move+" score "+score);

                scores[j] = score;
                for(int k=j-1;k>=0;k--){
                    if(scores[k]<score){
                        scores[j] = scores[k];
                        scores[k] = score;
                        moves.remove(move);
                        moves.add(k,move);
                    }

                }

                if(score == Constants.HIGHEST_VALUE){
                    System.out.println("Found mate in "+i);
                    foundMate = true;
                    break;
                }

            }

        }
        engineMove = moves.get(0);



    }

    public void orderMove(ArrayList<Move>moves){
        float[] scores = new float[moves.size()];
        for(int i=0;i<moves.size();i++) {
            float score = 0;
            Move move = moves.get(i);
            if (move.isKingSideCastling || move.isQueenSideCastling) {
                score += Constants.CASTLING_SCORE;
            } else {
                if (move.isEnPassant) {
                    score += Constants.EN_PASSANT_SCORE;
                } else {
                    if (move.capturedPiece != Constants.EMPTY_SQUARE) {
                        float valueDiff = Util.getPieceValue(move.capturedPiece) - Util.getPieceValue(cb.board[move.locRank][move.locFile]);
                        score += Constants.CAPTURE_SCORE * valueDiff;
                    }
                    if (move.promotionPiece != Constants.EMPTY_SQUARE) {
                        score += Constants.PROMOTION_SCORE + Util.getPieceValue(move.promotionPiece);
                    }
                }
            }
            scores[i] = score;
            for(int j=i-1;j>=0;j--){
                if(scores[j]<score){
                    scores[i] = scores[j];
                    scores[j] = score;
                    moves.remove(move);
                    moves.add(j,move);
                }
            }


        }


    }




    private float negamax(int depth,float alpha,float beta){
        if(searchCancelled){
            return alpha;
        }
        ArrayList<Move> moves = mm.getAllMoves();
        if(moves.isEmpty()){
            if(cb.gs == GameState.CHECK){
                return -Constants.HIGHEST_VALUE;
            }else{
                return 0;
            }
        }else if(cb.halfMoveClock == 100){
            return 0;
        }else if(depth == 0){
            if(useTranspositionTable) {
                long key = cb.generateZobristKey();
                if (transpositionTable.containsKey(key)) {
                    return transpositionTable.get(key);
                }
                float eval = searchAllCaptures(alpha,beta);
                savePosition(key, eval);
                return eval;
            }else {
                return searchAllCaptures(alpha,beta);
            }
//            return ev.evaluate();
        }

        orderMove(moves);
        float score;
        for(Move move : moves){
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



    public void undoMove(Move move){
        mm.undoMove(move);
    }

    public void makeMove(Move move){
        mm.makeMove(move);

        if(!useTranspositionTable){
            return;
        }
        if(!move.isKingSideCastling && !move.isQueenSideCastling){
            if(move.capturedPiece != Constants.EMPTY_SQUARE || move.isEnPassant){
                transpositionTable.clear();
            }
        }
        System.out.println( "positions stored "+transpositionTable.size());
    }



    private void savePosition(long zkey, float eval) {


        if(transpositionTable.size() < MAX_TABLE_ENTRIES){
            transpositionTable.put(zkey,eval);
        }else{
            for(long key: transpositionTable.keySet()){
                transpositionTable.remove(key);
                break;
            }
            transpositionTable.put(zkey,eval);
        }


    }

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
        if(searchCancelled){
            return alpha;
        }
        long zHash = cb.generateZobristKey();
//        float eval;
//        if(transpositionTable.containsKey(zHash)){
//            eval = transpositionTable.get(zHash);
//        }else {
//            eval = ev.evaluate3();
//            savePosition(zHash,eval);
//        }

        float eval = ev.evaluate3();

        if(eval >= beta){
            return beta;
        }

        alpha = Math.max(alpha,eval);


        ArrayList<Move> moves = mm.getAllCaptureMoves();

        for(Move move : moves){
//            try{
//                mm.verify(move);
//            }catch(InvalidMoveException e){
//                e.printStackTrace();
//                System.out.println();
//                System.out.println(cb.stats());
//                System.out.println();
//                System.exit(0);
//            }
                mm.makeMove(move);
                eval = -searchAllCaptures(-beta,-alpha);
                mm.undoMove(move);
                if(eval>=beta){
                    return beta;
                }
                alpha = Math.max(alpha,eval);

        }
        return alpha;
    }


    public boolean getUseTT() {
        return useTranspositionTable;
    }

    public void analysisMode() {
        depth = Constants.HIGHEST_VALUE;
    }
}
