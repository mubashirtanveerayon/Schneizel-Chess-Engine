package debug;

import server.board.ChessBoard;
import server.move.MoveManager;

import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Debugger {

    public static void main(String[] args) throws Exception{


        ChessBoard cb=new ChessBoard();
        MoveManager mm=new MoveManager(cb);

        Stockfish sf = new Stockfish();

        sf.startEngine();
        Scanner sc = new Scanner(System.in);
        String input;

        boolean running = true;
        while(running&&(input = sc.nextLine()) != null){
            String[] parts = input.split(" ");
            if(parts[0].equals("set")){
                String fen = input.split("set ")[1];
                cb = new ChessBoard(fen);
                mm = new MoveManager(cb);
                sf.sendCommand("position fen "+fen);
            }else if(input.equals("quit")){
                running = false;
                sf.stopEngine();
            }else if(parts[0].equals("perft") && parts.length >= 2 && Character.isDigit(input.charAt(6))){
                int depth = Integer.parseInt(parts[1]);
                sf.sendCommand("go perft "+depth);
                String engine = mm.moveGenerationTest(depth);
                int waitTime = 2000;
                if(parts.length == 3){
                    waitTime = Integer.parseInt(parts[2]);
                }
                String sfOutput = sf.getOutput(waitTime);
                Pattern movePattern = Pattern.compile("[a-h][1-8]");
                HashMap<String,Integer> enginePerftTest = new HashMap<>();
                HashMap<String,Integer> sfPerftTest = new HashMap<>();
                for(String line:engine.split("\n")){
                    if(movePattern.matcher(line).find()){
                        String[] perftPart = line.split(": ");
                        enginePerftTest.put(perftPart[0],Integer.parseInt(perftPart[1]));
                    }
                }
                for(String line:sfOutput.split("\n")){
                    if(movePattern.matcher(line).find()){
                        String[] perftPart = line.split(": ");
                        sfPerftTest.put(perftPart[0],Integer.parseInt(perftPart[1]));
                    }
                }
                comparePerftOutput(enginePerftTest,sfPerftTest);
            }else if(input.equals("stats")){
                System.out.println(cb.stats());
            }
        }



    }

    private static void comparePerftOutput(HashMap<String,Integer> engine,HashMap<String,Integer> sf){
        System.out.println("comparing...");
        for(String key:engine.keySet()){
            try{
                if(!Objects.equals(sf.get(key), engine.get(key))){
                    System.out.println("engine report: "+engine.get(key));
                    System.out.println("fish report: "+sf.get(key));
                }
            }catch(Exception e){
                System.out.println("Invalid move: "+key);
            }
        }

        for(String key:sf.keySet()){
            try{
                if(!Objects.equals(engine.get(key), sf.get(key))){
                    System.out.println("engine report: "+engine.get(key));
                    System.out.println("sf report: "+sf.get(key));
                }
            }catch(Exception e){
                System.out.println("Could not find move: "+key);
            }
        }
        System.out.println("test finished");
    }




}
