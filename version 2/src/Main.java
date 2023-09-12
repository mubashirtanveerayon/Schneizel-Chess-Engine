import server.uci.UCI;

public class Main {

    public static void main(String[] args) {


//
//        ChessBoard cb = new ChessBoard();
//        MoveManager mm = new MoveManager(cb);
//
//        ArrayList<String> pgn = (PGNParser.getMoveText("data/game1.pgn"));
//        System.out.println(pgn);
//        System.out.println(cb.stats());
//        for(String san : pgn){
//            String move = PGNParser.parseSAN(san,mm);
//            if(move == null){
//                System.out.println("move is null: "+san);
//                System.out.println(FenUtils.cat(cb.fenParts));
//                break;
//            }else{
//                mm.makeMove(move);
//                System.out.println(cb.stats());
//            }
//        }


//        String pgn = "ed6f7";
//        Pattern pattern = Pattern.compile("[a-h][1-8]");
//        Matcher matcher = pattern.matcher(pgn);
//        String[] coords = new String[2];
//        while(matcher.find()){
//            System.out.println(matcher.group());
//        }
//        System.out.println("0102".substring(2,4));
//        Engine engine = new Engine("r3r1k1/pp3pbp/1qp1b1p1/2B5/2BP4/Q1n2N2/P4PPP/3R1K1R w - - 4 18");
//        System.out.println(engine.cb.stats());
//        String move = (engine.search());
//        System.out.println(engine.mm.cvt(move));
//        System.out.println(engine.cb.stats());

//        ArrayList<String> a = new ArrayList<>();
//
//        a.add("a");
//        a.add("b");
//        a.add("c");
//        System.out.println(a.toString());
//        ArrayList<String> n = new ArrayList<>();
//
//        for(String s:a){
//            if(s.equals("b")){
//                n.add(s);
//            }
//        }
//        a.removeAll(n);
//        for (int i=0;i<n.size();i++){
//            a.add(i,n.get(i));
//        }

//        String s = a.get(1);
//        a.remove(s);
//        a.add(0,s);
//
//        System.out.println(a.toString());


        //position fen 5k2/n1Q4R/4p3/p3P2p/8/6K1/1P5P/2N5 w - - 2 42->wrong best move
        //position fen 1rb1k2r/1p1p1p2/p1n1p1pp/2Q1P3/6n1/2N2N2/PP3PPP/2KR1B1R w - - 1 16->wrong best move
        //position fen rnbqkbnr/pp2pppp/8/8/2ppP3/8/PP2PPPP/RNBQKBNR w KQkq - 0 1->wrong best move
        //position fen r3kbnr/ppp1pppp/8/3P1b2/2q5/1P6/P3PPPP/RNBQKB1R b KQkq - 0 8->wrong best move
        //rn1q1rk1/p1p1bppp/b3pn2/3p4/P2P1B2/2P2NP1/4PPBP/RN1Q1RK1 w - - 3 10
        //position startpos moves d2d4 g8f6 c2c4 e7e6 b1c3 f8b4 e2e3 b8c6 g1f3 d7d6 f1d3 h7h5 h1f1 c8d7 a2a3 b4c3 b2c3 a8c8 c1b2 a7a5 f3g5 e6e5 d4e5 c6e5 g5f7 e8f7 d3e2 d7f5 f2f4 e5g6 e2h5 h8h5 h2h4 h5h4 e1f2 d8e8 f2f3 f5g4 f3f2 f6e4 f2e1 g4d1 e1d1 h4h2 f4f5 g6e5 f1f4 h2h1 d1e2 e4g3 e2d2 e5c4 f4c4 g3e4 c4e4 h1a1 e4e8 f7e8 b2a1 b7b5 c3c4 b5c4 a1g7 e8d7 d2c3 c8g8 g7d4 d6d5 d4e5 g8g2 c3d4 c7c6 a3a4 g2a2 f5f6 a2a4 e3e4 d7e6 e5c7 e6f6 e4d5 c4c3 d4c3 c6d5 c3b3 a4b4 b3a3 b4e4 c7a5 e4e3 a3b4 f6e5 a5c7 e5e6 b4c5 e3c3 c5b6 c3b3 b6a5 d5d4 c7h2 b3b1 h2g3 e6d5 g3h4 d5c4 h4e7 b1b5 a5a6 d4d3 e7g5
        boolean debugging = false;
        if (debugging) {

//            ChessBoard cb = new ChessBoard();
//            MoveManager mm = new MoveManager(cb);
//            ArrayList<HashMap<String,String>> game = PGNUtils.parseFile("data/test.pgn",1);
//            String moveText = game.get(0).get("Moves");
//            ArrayList<String> movesMade = new ArrayList<>();
//            for(String seg:moveText.split(" ")){
//                String m = PGNUtils.parse(seg,mm);
//                if(m != null){
//                    mm.makeMove(m);
//                    movesMade.add(m);
//                    System.out.println(Util.getBoardVisualStd(cb.board));
//                    System.out.println(FenUtils.cat(cb.fenParts));
//                }
//            }
//            System.out.println(movesMade);
//
//            for(int i=movesMade.size()-1;i>=0;i--){
//                mm.undoMove(movesMade.get(i));
//            }
//            System.out.println(Util.getBoardVisualStd(cb.board));
//            String pgn = PGNUtils.getMoveText(movesMade);
//            System.out.println(pgn);

//5536/ /-/-/3 Nd2 | 1736/ /-/-/3 Nd2
//            System.out.println(PGNUtils.cvt("1736/ /-/-/3",mm));


            //debug();
//
//
//            float n = Float.POSITIVE_INFINITY * -0;
//            System.out.println(n);

//            ChessBoard cb = new ChessBoard();
//            long start = System.nanoTime();
//            FenUtils.cat(cb.fenParts);
//            System.out.println("fen " +(System.nanoTime() - start));
//            start = System.nanoTime();
//            cb.generateZobristKey(true);
//            System.out.println("key " +(System.nanoTime() - start));


//            HashMap<String,Integer> map = new HashMap<>();
//            map.put("a",1);
//            map.put("b",2);
//
//            map.put("v",0);
//
//           for(String s:map.keySet()){
//               map.put(s,3);
//           }
//
//            System.out.println(String.valueOf(Float.NEGATIVE_INFINITY));
//

//            Engine2 engine = new Engine2("rn2k1nr/1R2b3/8/p1p1p1pp/b2pN3/3P1P1P/qP2P1B1/2BQ1KNR w kq - 1 16");
//
//
//            System.out.println(engine.cb.stats());
//
//            long currentTime = System.currentTimeMillis();
//
//            engine.setDepth(6);
//            engine.beginSearch();
//
//            while(engine.searching){
//                System.out.print("");
//            }
//
//            long timeTaken = System.currentTimeMillis() - currentTime;
//
//            System.out.println("Time taken: "+timeTaken+" ms");


//            ArrayList<String> moves = engine.mm.getAllMoves();
//            System.out.println(moves);
//            engine.orderMoves(moves);
//            System.out.println(moves);
//
//
//            String g=null;
//            System.out.println("b "+g);

        } else {
            UCI cli = new UCI();
            cli.toggle();
            cli.run();
        }
    }
}
