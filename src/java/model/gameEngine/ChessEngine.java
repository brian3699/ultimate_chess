package model.gameEngine;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;
import model.board.Board;
import model.piece.Piece;
import model.util.ReflectionHandler;

import java.awt.*;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.*;


public class ChessEngine {
    private static final String DEFAULT_BOARD_DATA_PATH = "resources/board/Default_Chess_Board.csv";
    private static final String DEFAULT_TEAM_DATA_PATH = "resources/board/Default_Chess_Board_Team.csv";
    private static final ResourceBundle CHESS_PIECE_DATA = ResourceBundle.getBundle("model/pieceInfo/ChessPiecePaths");
    private static final String CLASS_PATH = "model.gameEngine.ChessEngine";

    private Board<Piece> myBoard;
    private int width;
    private int height;
    private CSVReader boardReader;
    private CSVReader teamReader;
    private Point currentPiece;
    private final ReflectionHandler reflectionHandler;
    private int currentPlayer;
    private int turnCount;
    private Map<Integer, Integer> scoreBoard;
    private List<Point> possibleMoves;
    private int[][] myPlayerBoard;
    private boolean[][] player1PossibleMoves;
    private boolean[][] player2PossibleMoves;




    /**
     * sets the board using default file
     */
    public ChessEngine() {
        this(DEFAULT_BOARD_DATA_PATH, DEFAULT_TEAM_DATA_PATH);
    }

    /**
     * sets the board from user input
     * @param boardFilePath file path to a csv file containing the state of each cell
     * @param teamFilePath file path to a csv file containing the team number of each cell
     */
    public ChessEngine(String boardFilePath, String teamFilePath) {
        currentPlayer = 1;
        reflectionHandler = new ReflectionHandler();
        initializeScoreBoard();
        try{
            initializeBoard(boardFilePath, teamFilePath);
            setPiece();
        }catch (IOException | CsvException e){
            // TODO: Refactor
            System.out.println("Please Choose a Correct File");
        }

    }

    // initialize class Board
    private void initializeBoard(String boardFilePath, String teamFilePath) throws IOException, CsvException {
        // create csv file reader
        boardReader = new CSVReader(new FileReader(boardFilePath));
        teamReader = new CSVReader(new FileReader(teamFilePath));

        // get width and height
        width = boardReader.peek().length;
        height = (int) boardReader.readAll().size();
        // reset boardReader
        boardReader = new CSVReader(new FileReader(boardFilePath));
        // initialize Board
        myBoard = new Board<>(width, height);

        myPlayerBoard = new int[height][width];
    }

    private void initializeScoreBoard(){
        scoreBoard = new HashMap<>();
        scoreBoard.put(1,0);
        scoreBoard.put(2,0);
    }

    public String getPieceType(int x, int y){
        return myBoard.getPieceType(x,y);
    }
    public int getPieceTeam(int x, int y){
        return myBoard.getPlayerNumber(x,y);
    }

    // Set pieces in myBoard
    private void setPiece() throws CsvValidationException, IOException {

        for(int i=0; i<height; i++){
            // array containing the types of the pieces
            String[] pieceLine = boardReader.readNext();
            // array containing the team numbers of the pieces
            String[] teamLine = teamReader.readNext();
            for(int j = 0; j < width; j++){
                try {
                    // path to the ResourceBundle containing Piece information
                    String pieceInfoPath = CHESS_PIECE_DATA.getString(pieceLine[j]);
                    ResourceBundle pieceDataResource = ResourceBundle.getBundle(pieceInfoPath);
                    // team number of the piece
                    int teamNumber = Integer.parseInt(teamLine[j]);
                    myBoard.setCell(teamNumber, pieceDataResource, j, i);
                    myPlayerBoard[i][j] = teamNumber;
                }catch (MissingResourceException e){
                    break;
                }
            }
        }
    }

    /**
     * Return list of points a piece could move
     * @param x x coordinate of the piece
     * @param y y coordinate of the piece
     * @return ArrayList if Points a piece could move to
     */
    public ArrayList<Point> getValidMoves(int x, int y) {
        try{
            currentPiece = new Point(x,y);
            String methodName = "get" + myBoard.getPieceType(x,y) + "Moves";
            // invokes different method for each chess piece type
            System.out.println(""+x+y);
            System.out.println(reflectionHandler.handleMethod(methodName,CLASS_PATH));
            return (ArrayList<Point>) reflectionHandler.handleMethod(methodName,CLASS_PATH).invoke(ChessEngine.this);
        }catch(InvocationTargetException | IllegalAccessException e){
            return null;
        }
    }


    private List<Point> getPawnMoves(){
        int x = currentPiece.x;
        int y = currentPiece.y;
        List<Point> moves = getSimpleMoves(x, y);
        int player = myBoard.getPlayerNumber(x,y);
        if(player == 1 && y == height - 2 && myBoard.getPlayerNumber(x, y - 1) == 0) moves.add(new Point(x, y - 2));
        if(player == 2 && y == 1 && myBoard.getPlayerNumber(x, y + 1) == 0) moves.add(new Point(x, y + 2));
        List<Point> returnList = new ArrayList<>();

        for(Point move: moves){
            int xMove = move.x - x;
            int yMove = move.y - y;
            int playerNumber = myBoard.getPlayerNumber(move.x, move.y);
            if(playerNumber == 0){
                if((xMove == 1 && yMove == -1) | (xMove == -1 && yMove == -1) | (xMove == 1 && yMove == 1) |
                        (xMove == -1 && yMove == 1)) continue;
            }else{
                if((xMove == 0 && yMove == 1) | (xMove == 0 && yMove == 2) | (xMove == 0 && yMove == -1) |
                        (xMove == 0 && yMove == -2)) continue;
            }
            returnList.add(move);
        }
        possibleMoves = returnList;
        return possibleMoves;
    }

    private List<Point> getKnightMoves(){
        return getSimpleMoves(currentPiece.x, currentPiece.y);
    }

    private List<Point> getBishopMoves(){
        return getComplexMoves(currentPiece.x, currentPiece.y);
    }

    private List<Point> getRookMoves() {
        return getComplexMoves(currentPiece.x, currentPiece.y);
    }

    private List<Point> getKingMoves() {
        if(currentPlayer == myBoard.getPlayerNumber(currentPiece.x, currentPiece.y)) {
            possibleMoves =  removeCheckMoves(getSimpleMoves(currentPiece.x,currentPiece.y), currentPlayer);
            return possibleMoves;
        }else {
            return getSimpleMoves(currentPiece.x, currentPiece.y);
        }
    }

    private List<Point> getQueenMoves() {
        return getComplexMoves(currentPiece.x, currentPiece.y);
    }



    private List<Point> getSimpleMoves(int x, int y){
        String team = ""+ myBoard.getPlayerNumber(x,y);
        int teamNumber = Integer.parseInt(team);
        possibleMoves = new ArrayList<>();
        ResourceBundle pieceMoves = ResourceBundle.getBundle(CHESS_PIECE_DATA.getString(myBoard.getPieceType(x,y)));
        // enumerate all possible moves of a piece

        for(String key : pieceMoves.keySet()){
            if(team.equals("" + key.charAt(0))){
                String[] move = pieceMoves.getString(key).split(",");
                int newX = Integer.parseInt(move[0]) + currentPiece.x;
                int newY = Integer.parseInt(move[1]) + currentPiece.y;
                if(myBoard.getPlayerNumber(newX, newY) == myBoard.getPlayerNumber(x,y)) continue;
                else if(myBoard.getPlayerNumber(newX, newY) != 0){
                    possibleMoves.add(new Point(newX, newY));
                    continue;
                }
                possibleMoves.add(new Point(newX, newY));
            }
        }
        possibleMoves.removeIf(move -> move.x < 0 || move.x > width - 1 || move.y < 0 || move.y > height - 1);
        return possibleMoves;
    }

    private List<Point> getComplexMoves(int x, int y){
        String team = Integer.toString(myBoard.getPlayerNumber(x,y));
        possibleMoves = new ArrayList<>();
        ResourceBundle pieceMoves = ResourceBundle.getBundle(CHESS_PIECE_DATA.getString(myBoard.getPieceType(x,y)));
        // enumerate all possible moves of a piece

        for(String key : pieceMoves.keySet()){
            if(team.equals("" + key.charAt(0))){
                String[] move = pieceMoves.getString(key).split(",");
                for(int i = 1; i < Math.max(width, height); i++){
                    int newX = Integer.parseInt(move[0])*i + currentPiece.x;
                    int newY = Integer.parseInt(move[1])*i + currentPiece.y;
                    if(myBoard.getPlayerNumber(newX, newY) == myBoard.getPlayerNumber(x,y)) break;
                    else if(myBoard.getPlayerNumber(newX, newY) != 0){
                        possibleMoves.add(new Point(newX, newY));
                        break;
                    }
                    possibleMoves.add(new Point(newX, newY));
                }
            }
        }
        possibleMoves.removeIf(move -> move.x < 0 || move.x > width - 1 || move.y < 0 || move.y > height - 1);
        return possibleMoves;
    }

    public String clickType(int x, int y){
        if (currentPlayer == myBoard.getPlayerNumber(x, y)) return "clickOnPiece";
        if (possibleMoves == null) return "errorClick";
        for (Point move : possibleMoves) {
            if (move.x == x && move.y == y && myBoard.getPlayerNumber(x, y) == 0) return "movePiece";
            if (move.x == x && move.y == y && myBoard.getPlayerNumber(x, y) != 0) return "capturePiece";
        }
        return "errorClick";
    }

    public void movePiece(int x, int y){
        myPlayerBoard[currentPiece.y][currentPiece.x] = 0;
        myPlayerBoard[y][x] = currentPlayer;
        myBoard.movePiece(currentPiece.x, currentPiece.y, x, y);
        nextTurn();
    }

    public void capturePiece(int x, int y){
        int score = myBoard.getPiecePoint(x, y);
        myPlayerBoard[currentPiece.y][currentPiece.x] = 0;
        myPlayerBoard[y][x] = currentPlayer;
        scoreBoard.put(currentPlayer, scoreBoard.get(currentPlayer) + score);
        myBoard.capture(currentPiece.x, currentPiece.y, x, y);
        nextTurn();
    }

    private void nextTurn(){
        possibleMoves = null;
        currentPlayer = currentPlayer % 2 + 1;
        turnCount += 1;
    }

    public boolean[][] getAllMovableTile(int playerNumber){
        ArrayList<Point> playerAllPossibleMoves = new ArrayList<>();
        for(int i = 0; i < height; i ++){
            for(int j = 0; j < width; j++){
                if(myPlayerBoard[i][j] == playerNumber){
                    playerAllPossibleMoves.addAll(getValidMoves(j, i));
                }
            }
        }
        boolean[][] playerBoard = new boolean[height][width];
        if(playerNumber == 1) player1PossibleMoves = playerBoard;
        else if(playerNumber == 2) player2PossibleMoves = playerBoard;

        for(Point move : playerAllPossibleMoves){
            playerBoard[move.y][move.x] = true;
        }

        return playerBoard;
    }

    private List<Point> removeCheckMoves(List<Point> possibleMoves, int playerNumber){
            Point bufferPiece = currentPiece;
            int opponent = playerNumber % 2 + 1;
            boolean[][] opponentMoves = getAllMovableTile(opponent);

            List<Point> returnList = new ArrayList<>();

            for(Point move : possibleMoves){
                if(!opponentMoves[move.y][move.x]) returnList.add(move);
            }
            currentPiece = bufferPiece;
            return returnList;
    }

    public Point getCurrentPiece(){
        return new Point(currentPiece.x, currentPiece.y);
    }

    public int getUserScore(int playerNumber){
        return scoreBoard.get(playerNumber);
    }

    public int getCurrentPlayer(){return currentPlayer;}

    public boolean detectCheck(){
        int opponent = currentPlayer % 2 + 1;
        boolean[][] opponentMoves = getAllMovableTile(opponent);
        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                if(opponentMoves[j][i] && myBoard.getPieceType(i,j).equals("King") && myBoard.getPlayerNumber(i,j) == currentPlayer){
                    return true;
                }
            }
        }
        return false;
    }


}
