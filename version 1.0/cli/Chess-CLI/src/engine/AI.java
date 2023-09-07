package engine;

import util.*;

import java.util.ArrayList;

public class AI {

    Engine engine;

    public AI(Engine engine)
    {
        this.engine = engine;
    }

    public int[] BestMove(){
        ArrayList<int[]> moves = engine.copy().getLegalMoves();
        int size = moves.size();
        int movesperthread = size/(Constants.SEARCH_DEPTH);
        BestMove[] bestMoves = new BestMove[Constants.SEARCH_DEPTH];
        for(int i=0;i<bestMoves.length;i++){
            if(i==bestMoves.length-1){
                bestMoves[i] = new BestMove(engine.copy(),moves,moves.size());
            }else{
                bestMoves[i] = new BestMove(engine.copy(),moves,movesperthread);
            }
            bestMoves[i].start();
        }
        boolean complete = false;
        while(!complete){
            int count = 0;
            for(int i=0;i<bestMoves.length;i++){
                if(!bestMoves[i].isAlive()){
                    count++;
                }
            }
            complete = count == bestMoves.length;
            //System.out.print("");
        }
        int[] best=null;
        int leastDepth = -1;
        float bestScore = engine.whiteToMove?Float.POSITIVE_INFINITY:Float.NEGATIVE_INFINITY;
        for(int i=0;i<bestMoves.length;i++){
            if(engine.whiteToMove){
                if(bestMoves[i].finalscore<bestScore){
                    bestScore = bestMoves[i].finalscore;
                    best = bestMoves[i].bestMove;
                }else if(bestMoves[i].finalscore == bestScore){
                    if(bestMoves[i].leastDepthReached>leastDepth){
                        bestScore = bestMoves[i].finalscore;
                        best = bestMoves[i].bestMove;
                        leastDepth = bestMoves[i].leastDepthReached;
                    }
                }
            }else{
                if(bestMoves[i].finalscore>bestScore){
                    bestScore = bestMoves[i].finalscore;
                    best = bestMoves[i].bestMove;
                }else if(bestMoves[i].finalscore == bestScore){
                    if(bestMoves[i].leastDepthReached>leastDepth){
                        bestScore = bestMoves[i].finalscore;
                        best = bestMoves[i].bestMove;
                        leastDepth = bestMoves[i].leastDepthReached;
                    }
                }
            }
        }
        if(best == null){
            ArrayList<int[]> legalmoves = engine.getLegalMoves();
            if(legalmoves.isEmpty()){
                return null;
            }else{
                int index = (int)(Math.random()*legalmoves.size());
                return legalmoves.get(index);
            }
        }else{
            return best;
        }
    }

    public String getOutput(){
        String moves="";

        ArrayList<int[]> legalmoves = engine.getLegalMoves();
        for(int[] move:legalmoves){
            moves+=Util.cvtMove(move)+" ";
        }


        return moves;
    }
//
//    private void append(String text){
//        try {
//            fw.write(readFileContent(dst) + text);
//            fw.close();
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//    }
//
//    public static String readFileContent(File file) throws Exception{
//        FileInputStream fileInputStream;
//        String content;
//        fileInputStream = new FileInputStream(file);
//        byte[] value = new byte[(int) file.length()];
//        fileInputStream.read(value);
//        fileInputStream.close();
//        content = new String(value, StandardCharsets.UTF_8);
//        return content;
//    }


}
