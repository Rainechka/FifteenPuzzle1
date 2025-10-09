// com.example.fifteenpuzzle.model.GameBoard

package com.example.fifteenpuzzle.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameBoard {
    private final int size;
    private final int[][] board;
    private int emptyRow;
    private int emptyCol;

    public GameBoard(int size) {
        if (size < 2 || size > 10) {
            throw new IllegalArgumentException("Size must be between 2 and 10");
        }
        this.size = size;
        this.board = new int[size][size];
        reset();
    }

    public void reset() {
        int num = 1;
        int total = size * size;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (num == total) {
                    board[i][j] = 0;
                    emptyRow = i;
                    emptyCol = j;
                } else {
                    board[i][j] = num++;
                }
            }
        }
        shuffle();
    }

    private void shuffle() {
        List<Integer> tiles = new ArrayList<>();
        for (int i = 1; i < size * size; i++) {
            tiles.add(i);
        }
        tiles.add(0);
        // Простое перемешивание (в production — добавить проверку разрешимости)
        Collections.shuffle(tiles);
        int idx = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j] = tiles.get(idx++);
                if (board[i][j] == 0) {
                    emptyRow = i;
                    emptyCol = j;
                }
            }
        }
    }

    public boolean canMoveTile(int tileId) {
        if (tileId < 1 || tileId >= size * size) return false;

        int tileRow = -1, tileCol = -1;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] == tileId) {
                    tileRow = i;
                    tileCol = j;
                    break;
                }
            }
            if (tileRow != -1) break;
        }
        if (tileRow == -1) return false;

        return (Math.abs(tileRow - emptyRow) == 1 && tileCol == emptyCol) ||
                (Math.abs(tileCol - emptyCol) == 1 && tileRow == emptyRow);
    }

    public boolean moveTile(int tileId) {
        if (!canMoveTile(tileId)) return false;

        int tileRow = -1, tileCol = -1;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] == tileId) {
                    tileRow = i;
                    tileCol = j;
                    break;
                }
            }
            if (tileRow != -1) break;
        }

        board[emptyRow][emptyCol] = tileId;
        board[tileRow][tileCol] = 0;
        emptyRow = tileRow;
        emptyCol = tileCol;
        return true;
    }

    public boolean isSolved() {
        int expected = 1;
        int total = size * size;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] != (expected % total)) {
                    return false;
                }
                expected++;
            }
        }
        return true;
    }

    public int[] getBoardAsFlatArray() {
        int[] flat = new int[size * size];
        int index = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                flat[index++] = board[i][j];
            }
        }
        return flat;
    }

    public int getSize() {
        return size;
    }
}