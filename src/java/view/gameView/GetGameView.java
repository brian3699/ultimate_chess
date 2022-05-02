package view.gameView;

import java.awt.*;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * Getter class of a Factory. Returns GameView of the board game.
 *
 * @author Young Jun
 */
public class GetGameView {

    /**
     * returns GameView for the requested board game
     *
     * @param game             name of the game
     * @param languageResource ResourceBundle containing language of the game
     * @param clickMethod      method that will be clicked when a user clicks on a tile
     * @param rowCount         number of rows
     * @param colCount         number of columns
     * @return GameView of the requested board game
     */
    public GameView getGameView(String game, ResourceBundle languageResource, Consumer<Point> clickMethod, int rowCount, int colCount) {
        switch (game) {
            case "Chess":
                return new ChessView(languageResource, clickMethod, rowCount, colCount);
        }
        return null;
    }
}
