package server.util;

import java.security.SecureRandom;

public class Constants {


    public static final int HIGHEST_VALUE = 1000000;

    public static final float CAPTURE_SCORE = 2;

    public static final float PASSED_PAWN_BONUS = 0.24f;

    public static final float COVERED_PAWN_BONUS = 0.15f;

    public static final float DOUBLED_PAWN_PENALTY = 0.1f;

    public static final float PINNED_PIECES_PENALTY = 3.5f;

    public static final float CHECK_SCORE = 1.3f;

    public static final float PROMOTION_SCORE = 2.5f;

    public static final float CASTLING_SCORE = 1.62f;

    public static final float EN_PASSANT_SCORE = 2;

    public static final int ROWS = 8;

    public static final int COLUMNS = 8;

    public static final char WHITE = 'w';

    public static final String EN_PASSANT_NOTATION = "en";

    public static final String MOVE_SEPARATOR = "/";

    public static final char BLACK = 'b';

    public static final char WHITE_PAWN = 'P';

    public static final char WHITE_KNIGHT = 'N';

    public static final char WHITE_BISHOP = 'B';

    public static final char WHITE_ROOK = 'R';

    public static final char WHITE_QUEEN = 'Q';

    public static final char WHITE_KING = 'K';

    public static final char BLACK_PAWN = 'p';

    public static final char BLACK_ROOK = 'r';

    public static final char BLACK_QUEEN = 'q';

    public static final char BLACK_KING = 'k';

    public static final char BLACK_KNIGHT = 'n';

    public static final char BLACK_BISHOP = 'b';

    public static final char EMPTY_SQUARE = ' ';

    public static final float PAWN_VALUE = 10.4f;//1.7f;

    public static final float KNIGHT_VALUE = 30.5f;//3.2f;

    public static final float BISHOP_VALUE = 30.7f;//3.5f;

    public static final float ROOK_VALUE = 50.5f;

    public static final float QUEEN_VALUE = 90.6f;//9f;

//    public static final float PAWN_VALUE = 1.7f;
//
//    public static final float KNIGHT_VALUE = 3.2f;
//
//    public static final float BISHOP_VALUE = 3.5f;
//
//    public static final float ROOK_VALUE = 5.5f;
//
//    public static final float QUEEN_VALUE = 9f;

    public static final float KING_VALUE = 1f;

    public static final String FILES = "abcdefgh";

    public static final String RANKS = "87654321";
    public static final int PROMOTION_MOVE_LENGTH = 6;
    public static final int EN_PASSANT_MOVE_LENGTH = 6;
    public static final int NORMAL_MOVE_LENGTH = 5;
    public static final int CASTLING_MOVE_LENGTH = 4;


    public static String KING_SIDE_CASTLING = "O-O";

    public static String QUEEN_SIDE_CASTLING = "O-O-O";

    //                                                                      index mustn't be modified
    public static final int[][] ALL_DIRECTIONS = {{1,0},{-1,0},{0,1},{0,-1},{1,1},{-1,1},{-1,-1},{1,-1}};

    //                                                                      index mustn't be modified
    public static final int[][] KNIGHT_DIRECTION= {{1, 2},{2,1},{2, -1}, {1, -2},  {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}};


    public static final int BOOK_RANDOM = 1;

    public static final int BOOK_BEST = 0;

    public static final int BOOK_MOSTLY_PLAYED = -1;

    public static final  String STARTING_FEN="rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
//                                              0       1       2 3 4 5     6       7     8  9  10 11 12

    public static long[][][] zobristArray;
    public static long zobristTurnToMove;


    static {
        if(zobristArray == null) {
            SecureRandom rand = new SecureRandom();
            zobristArray = new long[2][6][64];
            for (int i=0;i<2;i++){
                for(int j=0;j<6;j++){
                    for(int k=0;k<64;k++){
                        zobristArray[i][j][k] = rand.nextLong();
                    }
                }
            }
            zobristTurnToMove = rand.nextLong();
        }
    }



}
