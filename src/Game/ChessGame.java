package Game;

import pieces.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class ChessGame extends JFrame implements MouseListener, MouseMotionListener {
    JLayeredPane layeredPane;
    Stockfish stockfish = new Stockfish();
    Board chessBoard;
    Tile selectedTile;
    int turn;
    Color selectedColor = new Color(86, 44, 44);
    Color highlightedColor = new Color(50, 150, 93);

    Action spaceAction;

    ArrayList<String> fens;

    public ChessGame(int size){
        Dimension boardSize = new Dimension(size, size);

        layeredPane = new JLayeredPane();
        getContentPane().add(layeredPane);
        layeredPane.setPreferredSize(boardSize);
        layeredPane.addMouseListener(this);
        layeredPane.addMouseMotionListener(this);

        spaceAction = new SpaceAction();



        chessBoard = new Board();
        layeredPane.add(chessBoard, JLayeredPane.DEFAULT_LAYER);
        chessBoard.setLayout(new GridLayout(8, 8));
        chessBoard.setPreferredSize(boardSize);
        chessBoard.setBounds(0, 0, boardSize.width, boardSize.height);

        for (int i = 0; i < 64; i++) {
            Tile tile = new Tile(i, new BorderLayout(), size / 8);
            chessBoard.add(tile);
        }

        setupPieces();

        fens = new ArrayList<String>();
        fens.add(chessBoard.computeFen(turn));
        chessBoard.getInputMap().put(KeyStroke.getKeyStroke("SPACE"), spaceAction);
        chessBoard.getActionMap().put(spaceAction, spaceAction);
        stockfish.startEngine();
    }

    public void setupPieces() {
        AudioPlayer.play("src/resources/audio/startgame.wav");

        //setup black pieces
        chessBoard.getTile(0).setPiece(new Rook(0));
        chessBoard.getTile(7).setPiece(new Rook(0));
        chessBoard.getTile(0).setCastleable(true);
        chessBoard.getTile(7).setCastleable(true);
        chessBoard.getTile(1).setPiece(new Knight(0));
        chessBoard.getTile(6).setPiece(new Knight(0));
        chessBoard.getTile(2).setPiece(new Bishop(0));
        chessBoard.getTile(5).setPiece(new Bishop(0));
        chessBoard.getTile(3).setPiece(new Queen(0));
        chessBoard.getTile(4).setPiece(new King(0));
        chessBoard.getTile(4).setCastleable(true);
        for (int i = 8; i < 16; i++) {
            chessBoard.getTile(i).setPiece(new Pawn(0));
        }
        //setup white pieces
        chessBoard.getTile(56).setPiece(new Rook(1));
        chessBoard.getTile(63).setPiece(new Rook(1));
        chessBoard.getTile(56).setCastleable(true);
        chessBoard.getTile(63).setCastleable(true);
        chessBoard.getTile(57).setPiece(new Knight(1));
        chessBoard.getTile(62).setPiece(new Knight(1));
        chessBoard.getTile(58).setPiece(new Bishop(1));
        chessBoard.getTile(61).setPiece(new Bishop(1));
        chessBoard.getTile(59).setPiece(new Queen(1));
        chessBoard.getTile(60).setPiece(new King(1));
        chessBoard.getTile(60).setCastleable(true);
        for (int i = 48; i < 56; i++) {
            Tile tile = (Tile) chessBoard.getComponent(i);
            tile.setPiece(new Pawn(1));
        }
        turn = 1;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        Tile[] tiles = chessBoard.getTiles();
        for (Tile rTile : tiles) {
            rTile.setBackground(rTile.getColor());
        }
        Tile tile = (Tile) chessBoard.getComponentAt(e.getX(), e.getY());
        Piece piece = tile.getPiece();
        if (piece != null) {
            if (piece.getColor() == turn) {
                selectedTile = tile;
                tile.setBackground(selectedColor);
                Tile[] legalMoves = tile.getPlayableMoves(chessBoard);
                for (Tile legalTile : legalMoves) {
                    legalTile.setBackground(highlightedColor);
                }
                return;
            }
        }
        int location = tile.getLocationOnBoard();
        if (selectedTile != null && selectedTile.isPlayableMove(location, chessBoard, true)) {
            //process move
            if (selectedTile.getPiece() instanceof Pawn){
                if (chessBoard.getTile(location + ((turn * 16)- 8)).getPiece() instanceof Pawn){
                    if (((Pawn) chessBoard.getTile(location + ((turn * 16)- 8)).getPiece()).moved2 == 1){
                        chessBoard.getTile(location + ((turn * 16)- 8)).setPiece(null);
                    }
                }
            }

            tile.setPiece(selectedTile.getPiece());
            selectedTile.setPiece(null);
            selectedTile.setBackground(selectedTile.getColor());
            selectedTile = null;
            AudioPlayer.play("src/resources/audio/move-self.wav");
            turn = 1 - turn;
            for (int check = 0; check < 64; check++){
                Piece checked = chessBoard.getTile(check).getPiece();
                if (checked instanceof Pawn){
                    Pawn p = (Pawn) checked;
                    if (p.moved2 > 0) {p.moved2 -=1;}
                }
            }

            //compute fen
            String fen = chessBoard.computeFen(turn);
            fens.add(fen);


            Tile[] enemyTiles = chessBoard.getOccupiedTilesOfColor(turn);

            boolean canMove = false;
            for (Tile enemyTile : enemyTiles) {
                if (enemyTile.getPlayableMoves(chessBoard).length > 0) {
                    canMove = true;
                    break;
                }
            }

            King king = (King) chessBoard.getKing(turn).getPiece();
            if (!canMove) {
                if (king.isInCheck(chessBoard)) {
                    AudioPlayer.play("src/resources/audio/win.wav");
                    checkmate();
                } else {
                    AudioPlayer.play("src/resources/audio/stalemate.wav");
                    stalemate();
                }
            }

            //check for three move repetition
            int priorOccurrences = 0;
            for (String oldFen : fens) {
                if (fen.equals(oldFen)) {
                    priorOccurrences++;
                }
            }
            if (priorOccurrences >= 3) {
                stalemate();
            }

        }
    }

    void checkmate() {
        AudioPlayer.play("src/resources/audio/win.wav");
        System.out.println("Checkmate! Here's the FEN for the final position!");
        System.out.println(fens.get(fens.size() - 1));
        int option;
        String buttons[] = {"Replay", "Quit"};
        if (turn == 1) {
            option = JOptionPane.showOptionDialog(null, "Black wins! Play again or quit?", "Checkmate", JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, buttons, "default");
        } else {
            option = JOptionPane.showOptionDialog(null, "White wins! Play again or quit?", "Checkmate", JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, buttons, "default");
        }
        if (option == 0) {
            for (int i = 0; i < 64; i++) {
                chessBoard.getTile(i).setPiece(null);
            }
            fens.clear();
            setupPieces();
        } else {
            System.exit(0);
        }
    }

    void stalemate() {
        AudioPlayer.play("src/resources/audio/stalemate.wav");
        System.out.println("Stalemate! Here's the FEN for the final position!");
        System.out.println(fens.get(fens.size() - 1));
        int option;
        String buttons[] = {"Replay", "Quit"};
        option = JOptionPane.showOptionDialog(null, "Stalemate! Play again or quit?", "Stalemate", JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, buttons, "default");
        if (option == 0) {
            for (int i = 0; i < 64; i++) {
                chessBoard.getTile(i).setPiece(null);
            }
            fens.clear();
            setupPieces();
        } else {
            System.exit(0);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    private void playBestMove() {
        String bestMove = stockfish.getBestMoveCode(fens.get(fens.size() - 1), 1000);
        int currentLocation = Board.convertCodeToLocation(new String(new char[]{bestMove.charAt(0), bestMove.charAt(1)}));
        int newLocation = Board.convertCodeToLocation(new String(new char[]{bestMove.charAt(2), bestMove.charAt(3)}));
        Tile selected = chessBoard.getTile(currentLocation);
        Tile destination = chessBoard.getTile(newLocation);

        if (selected != null && selected.isPlayableMove(newLocation, chessBoard, true)) {
            if (selected.getPiece() instanceof Pawn) {
                if (chessBoard.getTile(newLocation + ((turn * 16) - 8)).getPiece() instanceof Pawn) {
                    if (((Pawn) chessBoard.getTile(newLocation + ((turn * 16) - 8)).getPiece()).moved2 == 1) {
                        chessBoard.getTile(newLocation + ((turn * 16) - 8)).setPiece(null);
                    }
                }
            }
            destination.forceSetPiece(selected.getPiece());
            selected.setPiece(null);
            char promote = bestMove.charAt(4);
            int color = destination.getPiece().getColor();
            switch (promote) {
                case 'q':
                    destination.setPiece(new Queen(color));
                    break;
                case 'n':
                    destination.setPiece(new Knight(color));
                    break;
                case 'r':
                    destination.setPiece(new Rook(color));
                    break;
                case 'b':
                    destination.setPiece(new Bishop(color));
                    break;
            }
            AudioPlayer.play("src/resources/audio/move-self.wav");
            for (int check = 0; check < 64; check++) {
                Piece checked = chessBoard.getTile(check).getPiece();
                if (checked instanceof Pawn) {
                    Pawn p = (Pawn) checked;
                    if (p.moved2 > 0) {
                        p.moved2 -= 1;
                    }
                }
            }
            turn = 1 - turn;
            String fen = chessBoard.computeFen(turn);
            fens.add(fen);
            Tile[] enemyTiles = chessBoard.getOccupiedTilesOfColor(turn);
            boolean canMove = false;
            for (Tile enemyTile : enemyTiles) {
                if (enemyTile.getPlayableMoves(chessBoard).length > 0) {
                    canMove = true;
                    break;
                }
            }
            King king = (King) chessBoard.getKing(turn).getPiece();
            if (!canMove) {
                if (king.isInCheck(chessBoard)) {
                    checkmate();
                } else {
                    stalemate();
                }
            }
            //check for three move repetition
            int priorOccurrences = 0;
            for (String oldFen : fens) {
                if (fen.equals(oldFen)) {
                    priorOccurrences++;
                }
            }
            if (priorOccurrences >= 3) {
                stalemate();
            }
        }
    }

    public class SpaceAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            playBestMove();
        }
    }
}