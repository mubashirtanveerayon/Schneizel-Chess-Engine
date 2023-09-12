package server.uci;

import engine.Engine;
import server.util.FenUtils;
import server.util.GameState;
import server.util.Util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class UCI {

    private boolean running = false,initialized = false, saveToLog = false;;


    Scanner sc;
//    ChessBoard cb;
//    MoveManager mm;
    Engine engine;

    String input="";

    ArrayList<String> movesMade;

    public void toggle(){
        if (!running){
            sc = new Scanner(System.in);
            engine = new Engine();
            running = true;
            movesMade = new ArrayList<>();
        }
    }


    public void run() {

        if(initialized || !running){
            return;
        }else {
            initialized = true;
        }

        print("Schneizel chess engine v2.0");

        boolean flip=false;
        String output="";
        while(running ){
            input = sc.nextLine();
            String[] partsBySpace = input.split(" ");
            switch(partsBySpace[0].toLowerCase()){
                case "stop":
                    engine.stopped = true;
                    engine.searchCancelled = true;
                    break;
                case "uci":
                    output = "id name Schneizel 2\nid author Ayon\nuciok";
                    print(output);
                    break;
                case "isready":
                    output = "readyok";
                    print(output);
                    break;
                case "go":
                    if (partsBySpace.length > 1){

                        if(partsBySpace[1].equals("perft")){
                            int depth = Integer.parseInt(partsBySpace[2]);
                            long currentTime = System.nanoTime();
                            output = engine. mm.moveGenerationTest(depth, true);

                            if(saveToLog){
                                try{
                                    FileWriter fw = new FileWriter("debug/output.txt");
                                    BufferedWriter bw = new BufferedWriter(fw);
                                    bw.write(output);
                                    //fw.close();
                                    bw.close();
                                }catch(Exception e){
                                    e.printStackTrace();
                                }
                            }

                            output+="\nTime taken: " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - currentTime) + " ms";



                            print(output);
                        }else if(partsBySpace[1].equals("depth")){
                            int depth = Integer.parseInt(partsBySpace[2]);
                            engine.setDepth(depth);
                            engine.beginSearch();
                        }else if(partsBySpace[1].equals("movetime")){
                            engine.beginSearch(Integer.parseInt(partsBySpace[2]));
                        }else{
                            engine.beginSearch();
                        }
                    }else{
                        engine.beginSearch();
                    }

                    break;
                case "ucinewgame":
                    input = "position startpos";
                    partsBySpace = input.split(" ");
                    movesMade.clear();
                case "position":
                    switch(partsBySpace[1].toLowerCase()){
                        case "fen":
                            engine = new Engine(input.split("fen ")[1]);
                            movesMade.clear();
                            break;
                        case "startpos":
                            if(partsBySpace.length<3){
                                engine = new Engine();
                                movesMade.clear();
                                break;
                            }
                        case "thispos":
                            if(partsBySpace[2].equalsIgnoreCase("moves")){
                                if(partsBySpace[1].equalsIgnoreCase("thispos")){
                                     for(int i=3;i<partsBySpace.length;i++){
                                        engine.make(engine.mm.parse(partsBySpace[i]));
                                        movesMade.add(partsBySpace[i]);
                                    }
                                }else{
                                    boolean sameGame = true;
                                    for(int i=0;sameGame && i<movesMade.size();i++){
                                        sameGame = movesMade.get(i).equals(partsBySpace[i+3]);
                                    }

                                    int startIndex = movesMade.size()+3;
                                    if(!sameGame){
                                        movesMade.clear();
                                        engine = new Engine();
                                        startIndex = 3;
                                    }
                                    for(int i=startIndex;i<partsBySpace.length;i++){
                                        engine.make(engine.mm.parse(partsBySpace[i]));
                                        movesMade.add(partsBySpace[i]);
                                    }
                                }
                            }
                            break;
                            //bs uci, they could add another command for resuming the game with the final move
                    }

                    break;
                case "usebook":
                    engine.useBook = !engine.useBook;
                    System.out.println(engine.useBook);
                    break;
//                case "usett":
//                    engine.useTranspositionTable = !engine.useTranspositionTable;
//                    System.out.println(engine.useTranspositionTable);
//                    break;
                case "d":
                    output = Util.getBoardVisualStd(engine.cb.board,flip);
                    //output+="\n"+Util.getBoardVisual(engine.cb.board);
                    output+="\n"+("Fen: "+ FenUtils.cat(engine.cb.fenParts));
                    print(output);                    break;
                case "depth":
                    engine.setDepth(Integer.parseInt(partsBySpace[1]));
                    System.out.println("Search depth set to: "+engine.getDepth());
                    break;
                case "quit":
                    running = false;
                    break;
                case "fen":
                    print("Fen: "+ FenUtils.cat(engine.cb.fenParts));
                    break;
                case "flip":
                    flip = !flip;
                    break;
                case "save":
                    saveToLog = !saveToLog;
                    System.out.println(saveToLog);
                    break;
                case "push":
                    if (partsBySpace.length == 1){
                        engine.beginSearch();
                        while(engine.searching){
                            System.out.print("");
                        }
                        String move = engine.engineMove;
                        engine.make(move);
                        movesMade.add(move);
                        output = "played "+engine.mm.cvt(move)+"\n";
                        output += "Fen " + FenUtils.cat(engine.cb.fenParts);
                        print(output);
                        break;
                    }
                    try{
                        String move = engine.mm.getAllMoves().get(Integer.parseInt(partsBySpace[1])-1);
                        engine.make(move);
                        movesMade.add(move);
                        print("Fen " + FenUtils.cat(engine.cb.fenParts));
                    }catch(Exception e) {
                        engine.make(engine.mm.parse(partsBySpace[1]));
                        movesMade.add(partsBySpace[1]);
                        print("Fen " + FenUtils.cat(engine.cb.fenParts));
                    }
                    break;
                case "state":
                    print(engine.cb.gs.toString());
                    break;
                case "checkers":
                    if(engine.cb.gs == GameState.CHECK){
                        for(Integer index:engine.cb.checkers.keySet()){
                            print(Util.cvtCoord(index));
                        }
                    }else{
                        print(engine.cb.gs);
                    }
                    break;
                case "pinned":
                    for(Integer index:engine.cb.pinnedPieces.keySet()){
                        print(Util.cvtCoord(index));
                    }
                    break;
                case "stats":
                    print(engine.cb.stats());
                    break;
                case "list":
                    output = "";
                    int i=1;
                    for(String move : engine.mm.getAllMoves()){
                        output += Integer.toString(i) + ". "+engine.mm.cvt(move) + "\n";
                        i+=1;
                    }
                    print(output);
                    break;
                case "eval":
                    output = "eval ";
                    float evaluation = engine.ev.evaluate();
                    output += String.valueOf(evaluation);
                    print(output);
            }
        }

    }

    private void print(Object o){
        if(saveToLog){
            if(!Util.loggerInitialized())Util.initLogger();
            Util.writeToLog("Input: "+input+"\n"+o.toString());
        }else{
            System.out.println(o);
        }
    }



}
// position startpos server.move c2c3 c7c5 c3c4 b7b5 a2a3 b5c4 d2d3 d8b6 b1c3 c8b7 a3a4
// ,g1f3,g1h3,c1d2,c1e3,c1f4,c1g5,c1h6,d1c2,d1d2,d1a4,e1d2
// position startpos server.move c2c3 c7c5 c3c4 b7b5 a2a3 b5c4 d2d3 d8b6 b1c3 c8b7 a3a4 d7d5 a1a2 d5d4 e1d2 d4c3 -> solved
// position startpos server.move c2c3 c7c5 c3c4 b7b5 a2a3 b5c4 d2d3 d8b6 b1c3 c8b7 a3a4 d7d5 a1a2 d5d4 e1d2 b8d7 g1h3 -> depth 1 d7 not found after going perft 2 and then 1, fen changes somewhere in between
// position fen r3r1k1/pp3pbp/1qp3p1/2B5/2bP4/Q1n2N2/P2R1PPP/5K1R w - - 0 19->did not find d2d3
// position fen 8/4k3/8/8/8/8/4r2r/1R2K2R w K - 2 2 -> cant detect check, problem in cb.checkBoard()
// position fen 8/4k3/8/8/8/8/2r4r/R3K1R1 b Q - 3 2 -> go perft 1=correct output[check detects], go perft 2=incorrect output[cant detect check]