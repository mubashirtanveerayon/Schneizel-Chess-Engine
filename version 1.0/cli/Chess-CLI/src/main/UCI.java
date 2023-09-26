package main;

import schneizel.Schneizel;
import util.Constants;
import util.Util;

import java.util.Scanner;

public class UCI {

    public static void main(String[] args) {
        Schneizel engine = new Schneizel();
        boolean running = true;
        Scanner scanner = new Scanner(System.in);
        String input;

        while(running){
            input = scanner.nextLine();

            String[] partsBySpace = input.split(" ");

            switch(partsBySpace[0]){
                case "uci":
                    System.out.println("id name Schneizel 1\nid author see AUTHORS file\nuciok");
                    break;
                case "go":
                    int prevDepth = Constants.SEARCH_DEPTH;
                    if(partsBySpace.length == 3) {
                        Constants.SEARCH_DEPTH = Integer.parseInt(partsBySpace[2]);
                    }
                    String bestMoveStr = engine.cvtMove(engine.getBestMove());

                    int[] move = Util.parseMove(bestMoveStr);

                    boolean pawnPromotion = ((engine.getTurn() == Constants.WHITE && move[3] == 0) || (engine.getTurn() != Constants.WHITE && move[3] == 7)) && (Character.toUpperCase(engine.getBoard()[move[0]][move[1]]) == Constants.WHITE_PAWN);
                    if (pawnPromotion) {
                        if (move.length == 4) {
                            bestMoveStr = bestMoveStr.substring(0, 4) + "Q";
                        } else if (move[4] == 1) {
                            bestMoveStr = bestMoveStr.substring(0, 4) + "N";
                        } else if (move[4] == 2) {
                            bestMoveStr = bestMoveStr.substring(0, 4) + "B";
                        } else if (move[4] == 3) {
                            bestMoveStr = bestMoveStr.substring(0, 4) + "R";
                        }
                    }
                    Constants.SEARCH_DEPTH = prevDepth;
                    System.out.println("bestmove " + bestMoveStr);
                    break;
                case "isready":
                    System.out.println("readyok");
                    break;
                case "ucinewgame":
                    engine = new Schneizel();
                    break;
                case "position":
                    String fen="";
                    if(partsBySpace[1].equals("startpos")){
                        fen = Constants.STARTING_FEN;
                    }else{
                        for(int i=2;i< partsBySpace.length;i++){
                            fen += partsBySpace[i] + " ";
                        }
                    }
                    fen = fen.trim();
                    engine = new Schneizel(fen);
                    if(input.contains("moves")){
                        for(int i=3;i< partsBySpace.length;i++){
                            String moveStr=partsBySpace[i];
                            switch(Character.toUpperCase(partsBySpace[i].charAt(partsBySpace[i].length()-1))){
                                case Constants.WHITE_QUEEN:
                                    moveStr = moveStr.substring(0,4);
                                    break;
                                case Constants.WHITE_ROOK:
                                    moveStr = moveStr.substring(0,4) + "3";
                                    break;
                                case Constants.WHITE_KNIGHT:
                                    moveStr = moveStr.substring(0,4) + "1";
                                    break;
                                case Constants.WHITE_BISHOP:
                                    moveStr = moveStr.substring(0,4) + "2";
                                    break;

                            }
                            engine.makeMove(moveStr);
                        }
                    }else{
                        break;
                    }
                case "d":
                    System.out.println(Util.printBoard(engine.getBoard(),false));
                    break;
                case "quit":
                    running = false;
                    break;
                case "eval":
                    System.out.println("eval "+engine.evaluateBoard());
                    break;
            }


        }

    }



}
