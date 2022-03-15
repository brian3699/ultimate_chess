package view;

import javafx.scene.layout.GridPane;

import java.awt.*;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class BoardView extends GridPane {

    private static final String MAGIC_VALUE_RESOURCE_PATH = "view.resources.MagicValues";

    private final int rowCount;
    private final int colCount;
    private final Consumer clickMethod;
    private final ResourceBundle magicValueBundle;
    private final TileView[][] boardArray;
    private List<Point> possibleMoves;

    public BoardView(Consumer<Point> clickMethod, int rowCount, int colCount) {
        this.clickMethod = clickMethod;
        this.rowCount = rowCount;
        this.colCount = colCount;
        boardArray = new TileView[rowCount][colCount];
        magicValueBundle = ResourceBundle.getBundle(MAGIC_VALUE_RESOURCE_PATH);
    }

    public void setTile(String pieceType, int team, int rowNum, int colNum){
        TileView tile = new TileView(pieceType, team);
        addTileToBoard(tile, rowNum, colNum);
    }

    public void movePiece(int xOrigin, int yOrigin, int xNew, int yNew){
        removeHighlight();
        TileView piece = boardArray[yOrigin][xOrigin];
        TileView destinationTile = boardArray[yNew][xNew];
        addTileToBoard(piece, yNew, xNew);
        addTileToBoard(destinationTile, yOrigin, xOrigin);
    }

    public void capturePiece(int xOrigin, int yOrigin, int xNew, int yNew){
        removeHighlight();
        TileView piece = boardArray[yOrigin][xOrigin];
        TileView emptyTile = new TileView("-", 0);
        addTileToBoard(piece, yNew, xNew);
        addTileToBoard(emptyTile, yOrigin, xOrigin);
    }

    /**
     * Highlight possible moves in green.
     *
     * @param possibleMoves List of points with possible moves.
     */
    public void highlightPossibleMoves(List<Point> possibleMoves) {
        this.possibleMoves = possibleMoves;
        for (Point move : possibleMoves) {
            TileView tile = boardArray[move.y][move.x];
            tile.setOnMouseClicked(event -> clickMethod.accept(move));
            tile.setBackground(3);
            this.add(tile, move.y, move.x);
        }
    }

    private void removeHighlight(){
        for (Point move : possibleMoves) {
            TileView tile = boardArray[move.y][move.x];
            addTileToBoard(tile, move.y, move.x);
        }
    }

    private void setTileColor(TileView tile, int rowNum, int colNum){
        int tileColor = ((rowNum % 2) + colNum)%2;
        tile.setBackground(tileColor);
    }

    private void addTileToBoard(TileView tile, int rowNum, int colNum){
        tile.setOnMouseClicked(event -> clickMethod.accept(new Point(rowNum, colNum)));
        setTileColor(tile, rowNum, colNum);
        boardArray[rowNum][colNum] = tile;
        this.add(tile, colNum, rowNum);
    }
}