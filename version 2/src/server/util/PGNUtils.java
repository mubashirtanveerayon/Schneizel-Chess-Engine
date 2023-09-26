package server.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import server.board.ChessBoard;
import server.move.MoveManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PGNUtils {

    public static final int ALL_GAMES = -1;

    public static String generateSANMoveText(ArrayList<String> movesMade){
        ChessBoard cb = new ChessBoard();
        MoveManager mm = new MoveManager(cb);
        String pgn = "";
        int plyCount = 1;
        for(String move:movesMade){
            if(cb.turn == Constants.WHITE){
                pgn += plyCount+". ";
                plyCount+=1;
            }
            pgn += PGNUtils.cvt(move,mm)+" ";
            mm.makeMove(move);
        }
        return pgn.trim();
    }

    public static String cvt(String move, MoveManager mm){//move to san
        if(move.contains(Constants.QUEEN_SIDE_CASTLING)){
            return Constants.QUEEN_SIDE_CASTLING;
        }else if(move.contains(Constants.KING_SIDE_CASTLING)){
            return Constants.KING_SIDE_CASTLING;
        }

        ArrayList<String> moves = mm.getAllMoves();

        int fromFile = Integer.parseInt(Character.toString(move.charAt(0)));
        int fromRank = Integer.parseInt(Character.toString(move.charAt(1)));
        int toFile = Integer.parseInt(Character.toString(move.charAt(2)));
        int toRank = Integer.parseInt(Character.toString(move.charAt(3)));


        ArrayList<String> similarMoves = new ArrayList<>();
        char pieceToMove = mm.cb.board[fromRank][fromFile];
        for(String m:moves){
            if(!move.equals(m) && !m.substring(0,2).equals(move.substring(0,2)) && m.substring(2,4).equals(move.substring(2,4))){
                if(pieceToMove == mm.cb.board[Integer.parseInt(Character.toString(m.charAt(1)))][Integer.parseInt(Character.toString(m.charAt(0)))]){
                    similarMoves.add(m);
                }
            }
        }

        String san;
        if(Character.toUpperCase(pieceToMove) == Constants.WHITE_PAWN){
            san = "";
            if(!similarMoves.isEmpty()|| move.contains(Constants.EN_PASSANT_NOTATION) || move.charAt(5) != Constants.EMPTY_SQUARE ){
                san = Character.toString(Constants.FILES.charAt(fromFile));
            }
        }else{
            san = Character.toString(Character.toUpperCase(pieceToMove));
            boolean fileSame = false,rankSame = false;
            for(String similar:similarMoves){
                fileSame = fileSame || move.charAt(0) == similar.charAt(0);
                rankSame = rankSame || move.charAt(1) == similar.charAt(1);
            }

            if(rankSame && fileSame){
                san += Character.toString(Constants.FILES.charAt(fromFile)) + Character.toString(Constants.RANKS.charAt(fromRank));;
            }else if(rankSame){
                san += Character.toString(Constants.FILES.charAt(fromFile));
            }else if(fileSame){
                san += Character.toString(Constants.RANKS.charAt(fromRank));
            }else if(!similarMoves.isEmpty()){
                san += Character.toString(Constants.FILES.charAt(fromFile));
            }

        }
        if(move.charAt(5) != Constants.EMPTY_SQUARE ||move.contains(Constants.EN_PASSANT_NOTATION)){
            san += "x";
        }

        san += Character.toString(Constants.FILES.charAt(toFile)) + Character.toString(Constants.RANKS.charAt(toRank));


        if(Character.toUpperCase(pieceToMove) == Constants.WHITE_PAWN &&( toRank == 0 || toRank == 7)){
            san += "="+ move.split(Constants.MOVE_SEPARATOR)[Constants.PROMOTION_MOVE_LENGTH-1];
        }

        mm.makeMove(move);

        if(mm.cb.gs == GameState.CHECK){
            if(mm.getAllMoves().isEmpty()){
                san += "#";
            }else{
                san += "+";
            }
        }
        mm.undoMove(move);

        return san;
    }

    public static ArrayList<HashMap<String,String>> parsePGNFile(String path,int numberOfGames){
        return parsePGNText(getContent(path),numberOfGames);
    }

    public static ArrayList<HashMap<String,String>> parsePGNText(ArrayList<String> lines,int numberOfGames){
        ArrayList<HashMap<String,String>> games = new ArrayList<>();
        boolean parsingGame = false;
        HashMap<String,String> gameInfo = new HashMap<>();
        Pattern coordPattern = Pattern.compile("[a-h][1-8]");
        String moveText = "";
        int n = 0;
        for(String line:lines){
            String[] parts = line.split(" ");
            if(line.startsWith("[Event ")){
                parsingGame = true;
                gameInfo=new HashMap<>();
                moveText = "";
            }else if(!line.contains("\"") && (parts[parts.length-1].contains("-") || parts[parts.length-1].contains("*"))) {
                parsingGame = false;
                moveText += line;
                gameInfo.put("Moves",moveText.trim());
                games.add(gameInfo);
                n += 1;
                if(n == numberOfGames){
                    break;
                }
            }
            if (parsingGame){
                if(line.startsWith("[")){
                    String infoLine = line.replace("[", "").replace("]","");
                    String[] infoSegments = infoLine.split("\"");
                    String key = infoSegments[0].trim();
                    String value = infoSegments[1].trim();
                    gameInfo.put(key,value);
                }else if(Character.isDigit(line.charAt(0)) || parts[0].contains(Constants.KING_SIDE_CASTLING) || coordPattern.matcher(parts[0]).find()){
                    moveText += line+" ";
                }
            }
        }
        return games;
    }


    public static ArrayList<String> getContent(String filePath){
        ArrayList<String> content = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(filePath))){
            for(String line;(line = reader.readLine()) != null;){
                if(!line.isEmpty()){
                    content.add(line.trim());
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        //System.out.println(content);
        return content;
    }
    public static String parse(String san, MoveManager mm){//san to move

        if (san.toUpperCase().contains(Constants.QUEEN_SIDE_CASTLING)){
            return Util.constructCastlingMove(Constants.QUEEN_SIDE_CASTLING,mm.cb.fenParts);
        }else if (san.toUpperCase().contains(Constants.KING_SIDE_CASTLING)){
            return Util.constructCastlingMove(Constants.KING_SIDE_CASTLING,mm.cb.fenParts);
        }else{
            Pattern coordPattern = Pattern.compile("[a-h][1-8]");

            Matcher matcher = coordPattern.matcher(san);
            String from = "",to="";
            boolean invalid = true;
            while(matcher.find()){
                if(to.isEmpty()){
                    to = matcher.group();
                }else{
                    from = to;
                    to = matcher.group();
                }
                invalid = false;
            }
            if(invalid){
                return null;
            }
            ArrayList<String> allMoves = mm.getAllMoves();
            for(String move:allMoves){
                if(move.contains(Constants.KING_SIDE_CASTLING)){
                    continue;
                }
                int index = Integer.parseInt(Character.toString(move.charAt(2))) + Integer.parseInt(Character.toString(move.charAt(3))) * 8;
                String dstCoord = Util.cvtCoord(index);
                if(dstCoord.equals(to)){

                    index = Integer.parseInt(Character.toString(move.charAt(0))) + Integer.parseInt(Character.toString(move.charAt(1))) * 8;
                    char piece = mm.cb.board[index/8][index%8];
                    if(Character.isUpperCase(san.charAt(0))){
                        if(Character.toUpperCase(piece) == san.charAt(0)) {
                            if (from.isEmpty()) {
                                String firstPart = san.split(to)[0];
                                if (firstPart.length()>1 && firstPart.charAt(1) != 'x'){
                                    if(Util.cvtCoord(index).contains(Character.toString(firstPart.charAt(1)))){
                                        return move;
                                    }
                                }else{
                                    return move;
                                }
                            } else {
                                if (index == Constants.FILES.indexOf(from.charAt(0)) + Constants.RANKS.indexOf(from.charAt(1)) * 8) {
                                    return move;
                                }
                            }
                        }
                    }else{
                        if(Character.toUpperCase(piece) == Constants.WHITE_PAWN){
                            if(san.contains("=")){
                                if(!move.contains(Constants.EN_PASSANT_NOTATION)&&!Character.isDigit(move.charAt(move.length()-1))){
                                    String[] moveParts = move.split(Constants.MOVE_SEPARATOR);
                                    if(Character.toUpperCase(moveParts[moveParts.length-1].charAt(0)) == san.charAt(san.indexOf("=")+1)){
                                        return move;
                                    }
                                }
                            }else if(index % 8 == Constants.FILES.indexOf(san.charAt(0))){
                                return move;
                            }
                        }
                    }

                }

            }
        }
        return null;
    }

    public ArrayList<String> getMoves(String moveText){
        ChessBoard cb=new ChessBoard();
        MoveManager mm=new MoveManager(cb);
        ArrayList<String> movesMade = new ArrayList<>();
        for(String seg:moveText.split(" ")){
            if(!Character.isDigit(seg.charAt(0))){
                String move = parse(seg,mm);
                if(move!=null){
                    movesMade.add(move);
                    mm.makeMove(move);
                }
            }
        }
        return movesMade;
    }

}
