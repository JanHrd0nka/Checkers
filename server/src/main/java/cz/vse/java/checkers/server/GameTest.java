package cz.vse.java.checkers.server;

import cz.vse.java.checkers.common.Game;
import cz.vse.java.checkers.common.Pos;

import java.util.ArrayList;
import java.util.List;

/**
 * Test class used for verifying complex game mechanics
 * in the Checkers game implementation.
 *
 * This class was used to test:
 * - possible moves generation
 * - multi-jump move sequences
 * - move execution logic
 * - game state evaluation
 *
 * It is not part of the production server and serves only
 * for local debugging and validation of game rules.
 *
 * @author Jan Hrdonka
 * @version 1.0
 */
public class GameTest {
    public void test1() {
        String content = """
        0 0 0 0 0 0 0 0
        0 0 0 0 0 0 0 0
        0 0 0 0 0 0 0 0
        0 0 0 0 3 0 0 0
        0 0 0 1 0 0 0 0
        0 0 3 0 3 0 0 0
        0 0 0 0 0 0 0 0
        0 0 0 0 0 0 0 0
        """.replace("\n", "").replace(" ", "");

        Pos pos = new Pos(3, 3);
        testPossibleMoves(content, pos);
    }
    public void test2() {
        String content = """
        0 0 0 0 0 0 0 0
        0 0 0 0 3 0 3 0
        0 0 0 0 0 0 0 0
        0 0 0 0 3 0 0 0
        0 0 0 1 0 0 0 0
        0 0 3 0 3 0 0 0
        0 0 0 0 0 0 0 0
        0 0 0 0 0 0 0 0
        """.replace("\n", "").replace(" ", "");

        Pos pos = new Pos(3, 3);
        testPossibleMoves(content, pos);
    }

    public void test3() {
        String content = """
        0 0 0 0 0 0 0 0
        0 0 0 0 3 0 3 0
        0 0 0 0 0 0 0 0
        0 0 0 0 3 0 0 0
        0 0 0 2 0 0 0 0
        0 0 3 0 3 0 0 0
        0 0 0 0 0 0 0 0
        0 0 0 0 0 0 0 0
        """.replace("\n", "").replace(" ", "");

        Pos pos = new Pos(3, 3);
        testPossibleMoves(content, pos);
    }

    public void test4() {
        String content = """
        0 0 0 0 0 0 0 0
        0 0 4 0 3 0 0 0
        0 0 0 0 0 0 0 0
        0 0 4 0 3 0 0 0
        0 0 0 2 0 0 0 0
        0 0 0 0 0 0 0 0
        0 0 0 0 0 0 0 0
        0 0 0 0 0 0 0 0
        """.replace("\n", "").replace(" ", "");


        List<Pos> move = new ArrayList<>();
        move.add(new Pos(3, 3));
        move.add(new Pos(5, 5));
        move.add(new Pos(3, 7));
        move.add(new Pos(1, 5));
        move.add(new Pos(3, 3));
        testMove(content, move);
    }

    private void testPossibleMoves(String content, Pos pos) {
        Game game = new Game(content);
        var moves = game.getPossibleMoves(pos);

        for (var path : moves) {

            StringBuilder moveString = new StringBuilder();
            moveString.append(pos.x())
                    .append(" ")
                    .append(pos.y())
                    .append(" -> ");

            for (var move : path) {
                moveString.append(move.x())
                        .append(" ")
                        .append(move.y())
                        .append(" -> ");
            }

            System.out.println(moveString);
        }
    }
    private void testMove(String content, List<Pos> path){
        Game game = new Game(content);
        printBoard(game);
        System.out.println("-----------");
        game.makeMove(path);
        printBoard(game);
        System.out.println("-----------");
        System.out.println(game.checkGameState());
        System.out.println("-----------");
    }

    private void printBoard(Game game) {

        String content = game.toContent();

        for (int y = 0; y < 8; y++) {

            StringBuilder line = new StringBuilder();

            for (int x = 0; x < 8; x++) {

                int i = y * 8 + x;

                line.append(content.charAt(i));

                if (x < 7) {
                    line.append(" ");
                }
            }

            System.out.println(line);
        }
    }
}
