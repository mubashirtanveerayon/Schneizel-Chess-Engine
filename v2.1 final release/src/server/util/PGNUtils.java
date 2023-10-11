package server.util;

import java.io.BufferedReader;
import java.io.FileReader;

import server.board.ChessBoard;
import server.move.Move;
import server.move.MoveManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PGNUtils {

    public static final int ALL_GAMES = -1;

    public static String generateSANMoveText(ArrayList<Move> movesMade){
        ChessBoard cb = new ChessBoard();
        MoveManager mm = new MoveManager(cb);
        String pgn = "";
        int plyCount = 1;
        for(Move move:movesMade){
            if(cb.whiteToMove){
                pgn += plyCount+". ";
                plyCount+=1;
            }
            pgn += PGNUtils.cvt(move,mm)+" ";
            mm.makeMove(move);
        }
        return pgn.trim();
    }

    public static String cvt(Move move, MoveManager mm){//move to san
        if(move.isQueenSideCastling){
            return Constants.QUEEN_SIDE_CASTLING;
        }else if(move.isKingSideCastling){
            return Constants.KING_SIDE_CASTLING;
        }

        ArrayList<Move> moves = mm.getAllMoves();



        ArrayList<Move> similarMoves = new ArrayList<>();
        char pieceToMove = mm.cb.board[move.locRank][move.locFile];
        for(Move m:moves){
            if(!move.equals(m) && (move.locFile !=m.locFile && move.locRank!=m.locRank) && (move.destFile == m.destFile && move.destRank == m.destRank)){
                if(pieceToMove == mm.cb.board[m.locRank][m.locFile]){
                    similarMoves.add(m);
                }
            }
        }

        String san;
        if(Character.toUpperCase(pieceToMove) == Constants.WHITE_PAWN){
            san = "";
            if(!similarMoves.isEmpty()|| move.isEnPassant || move.capturedPiece != Constants.EMPTY_SQUARE ){
                san = Character.toString(Constants.FILES.charAt(move.locFile));
            }
        }else{
            san = Character.toString(Character.toUpperCase(pieceToMove));
            boolean fileSame = false,rankSame = false;
            for(Move similar:similarMoves){
                fileSame = fileSame || move.locFile == similar.locFile;
                rankSame = rankSame || move.locRank == similar.locRank;
            }

            if(rankSame && fileSame){
                san += Character.toString(Constants.FILES.charAt(move.locFile)) + Character.toString(Constants.RANKS.charAt(move.locRank));;
            }else if(rankSame){
                san += Character.toString(Constants.FILES.charAt(move.locFile));
            }else if(fileSame){
                san += Character.toString(Constants.RANKS.charAt(move.locRank));
            }else if(!similarMoves.isEmpty()){
                san += Character.toString(Constants.FILES.charAt(move.locFile));
            }

        }
        if(move.capturedPiece != Constants.EMPTY_SQUARE ||move.isEnPassant){
            san += "x";
        }

        san += Character.toString(Constants.FILES.charAt(move.destFile)) + Character.toString(Constants.RANKS.charAt(move.destRank));


        if(Character.toUpperCase(pieceToMove) == Constants.WHITE_PAWN &&( move.destRank == 0 || move.destRank == 7)){
            san += "="+ move.promotionPiece;
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
                    if(infoSegments.length<2){
                        continue;
                    }
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
    public static Move parse(String san, MoveManager mm){//san to move

        if (san.toUpperCase().contains(Constants.QUEEN_SIDE_CASTLING)){
            int rank = mm.cb.whiteToMove ? 7:0;
            Move move = new Move(4,rank,2,rank,mm.cb.board,mm.cb.castlingFEN,mm.cb.enPassantSquare,mm.cb.halfMoveClock);
            move.isQueenSideCastling = true;
            return move;
        }else if (san.toUpperCase().contains(Constants.KING_SIDE_CASTLING)){
            int rank = mm.cb.whiteToMove ? 7:0;
            Move move = new Move(4,rank,6,rank,mm.cb.board,mm.cb.castlingFEN,mm.cb.enPassantSquare,mm.cb.halfMoveClock);
            move.isKingSideCastling = true;
            return move;
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
            ArrayList<Move> allMoves = mm.getAllMoves();
            for(Move move:allMoves){
                if(move.isKingSideCastling || move.isQueenSideCastling){
                    continue;
                }

                int index = move.destFile + move.destRank * 8;
                String dstCoord = Util.cvtCoord(index);
                if(dstCoord.equals(to)){

                    index = move.locFile + move.locRank * 8;
                    char piece = mm.cb.board[move.locRank][move.locFile];
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
                                if(!move.isEnPassant){
                                    if(Character.toUpperCase(move.promotionPiece) ==Character.toUpperCase(san.charAt(san.indexOf("=")+1))){
                                        return move;
                                    }
                                }
                            }else if(move.locFile == Constants.FILES.indexOf(san.charAt(0)) ){
                                return move;
                            }
                        }
                    }

                }

            }
        }
        return null;
    }

    public static ArrayList<Move> getMoves(String moveText){
        ChessBoard cb=new ChessBoard();
        MoveManager mm=new MoveManager(cb);
        ArrayList<Move> movesMade = new ArrayList<>();
        for(String seg:moveText.split(" ")){
            if(seg.isEmpty() || Character.isDigit(seg.charAt(0)) || seg.charAt(0) == '{' || seg.contains("clk") || seg.contains("eval") || seg.endsWith("}")){
                continue;
            }

                Move move = parse(seg, mm);
                if (move != null) {
                    movesMade.add(move);
                    mm.makeMove(move);

                }

        }
        return movesMade;
    }

}
