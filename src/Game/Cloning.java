package Game;

import com.sun.org.apache.bcel.internal.generic.SWITCH;
import pieces.*;

public class Cloning {
    public static Piece Common(int color){
        int ran = (int) (Math.random() * 7);
        if (ran == 0){
            return new Elephant(color);
        } else if (ran == 1){
            return new Rook(color);
        } else if (ran == 2){
            return new Bishop(color);
        } else if (ran == 3){
            return new Frog(color);
        } else if (ran == 4){
            return new Knight(color);
        } else if (ran == 5){
            return new Camel(color);
        } else if (ran == 6){
            return new Bull(color);
        }
        return null;
    }
    public static Piece Pawn(int color){
        return new Pawn(color);
    }
    public static Piece Advanced(int color){
        int ran = (int) (Math.random() * 7);
        if (ran == 0){
            return new Amazon(color);
        } else if (ran == 1){
            return new Archbishop(color);
        } else if (ran == 2){
            return new Chancellor(color);
        } else if (ran == 3){
            return new General(color);
        } else if (ran == 4){
            return new King(color);
        } else if (ran == 5){
            return new Lion(color);
        } else if (ran == 6){
            return new Queen(color);
        }
        return null;
    }
    public static Piece Royal(int color){
        int ran = (int) (Math.random() * 3);
        if (ran == 0 || ran == 1){
            return new King(color);
        } else if (ran == 2){
            return new General(color);
        }
        return null;
    }
}