package main;

import schneizel.Schneizel;
import util.*;
import java.util.Scanner;

public class Main{

    public static void main(String[] args){
        Schneizel engine = new Schneizel();
        boolean running = true;
        Scanner scanner = new Scanner(System.in);
        String input;
        String[] content;
        while(running){
            input = scanner.nextLine();
            content = input.split(";");
            switch(content.length){
                case 1:
                    if(Character.isDigit(content[0].charAt(0)) && Integer.parseInt(content[0]) >= 0){
                        engine.setDifficulty(Integer.parseInt(content[0]));
                    }
                    else if(content[0].equalsIgnoreCase("move")){
                        System.out.println(engine.getLegalMoves());
                    }else if (content[0].equalsIgnoreCase("d")){
                        System.out.println(Util.printBoard(engine.getBoard(),false));
                    }else if (content[0].equalsIgnoreCase("v")){
                        System.out.println(Constants.VERSION);
                    }else if(content[0].equalsIgnoreCase("q")){
                        running = false;
                    }else if(content[0].equalsIgnoreCase("s")){
                        System.out.println(engine.getDifficulty());
                    }else if(content[0].equalsIgnoreCase("fen")){
                        System.out.println(engine.getFen());
                    }else if(content[0].equalsIgnoreCase("go")){
                        String bestMoveStr = engine.cvtMove(engine.getBestMove());
                        System.out.println("bestmove "+bestMoveStr);
                    }else if (content[0].equalsIgnoreCase("play")){
                        String bestMoveStr = engine.cvtMove(engine.getBestMove());
                        String side = engine.getTurn() == 'w' ? "white" : "black";
                        engine.makeMove(bestMoveStr);
                        System.out.println(side+" has made move: "+bestMoveStr);
                    }else{
                        try{
                            String move = content[0];
                            if(engine.isValidMove(move)){
                                String side = engine.getTurn() == 'w' ? "white" : "black";
                                engine.makeMove(move);
                                System.out.println(side+" has made move: "+move);
                            }
                        }catch(Exception e){
                            System.out.println("Unknown command: "+content[0]);
                        }
                    }
                    break;
                case 2:
                    break;
                case 3:
                    if (input.contains("position fen")){
                        if(engine.isValidMove(content[2])){
                            engine = new Schneizel(content[2]);
                            System.out.println("Board position set to: "+content[2]);
                        }
                    }
                    break;
            }
        }
    }

}