package com.ottertui.examples.apps;

import com.ottertui.backend.jline.JLineBackend;
import com.ottertui.core.Buffer;
import com.ottertui.core.Cell;
import com.ottertui.core.Color;
import com.ottertui.core.KeyCode;
import com.ottertui.core.Modifier;
import com.ottertui.core.Rect;
import com.ottertui.core.Style;
import com.ottertui.examples.InteractiveExample;
import com.ottertui.tui.Component;
import com.ottertui.tui.TuiRunner;
import com.ottertui.widgets.Block;
import com.ottertui.widgets.BorderStyle;
import com.ottertui.widgets.Clear;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class TetrisGame {

    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 20;
    private static final long BASE_DROP_MS = 800;
    private static final long SOFT_DROP_MS = 50;

    public static void main(String[] args) throws IOException {
        var backend = new JLineBackend();
        var size = backend.size();
        if (size.width() <= 0 || size.height() <= 0) {
            System.err.println("Error: No terminal detected.");
            System.exit(1);
        }

        var root = new TetrisComponent();
        var runner = new TuiRunner(backend, root);
        runner.keyBindings().bind(KeyCode.CHAR, Set.of(), 'q', runner::stop);
        runner.keyBindings().bind(KeyCode.LEFT, Set.of(), 0, () -> root.handleKey("left"));
        runner.keyBindings().bind(KeyCode.RIGHT, Set.of(), 0, () -> root.handleKey("right"));
        runner.keyBindings().bind(KeyCode.DOWN, Set.of(), 0, () -> root.handleKey("down"));
        runner.keyBindings().bind(KeyCode.UP, Set.of(), 0, () -> root.handleKey("up"));
        runner.keyBindings().bind(KeyCode.CHAR, Set.of(), ' ', () -> root.handleKey("space"));
        runner.keyBindings().bind(KeyCode.CHAR, Set.of(), 'p', () -> root.handleKey("p"));
        runner.run();
    }

    public static TetrisComponent createComponent() {
        return new TetrisComponent();
    }

    // ---------- Tetromino shapes ----------

    private static final int[][][] SHAPES = {
        // I
        {{1, 1, 1, 1}},
        // O
        {{1, 1}, {1, 1}},
        // T
        {{0, 1, 0}, {1, 1, 1}},
        // S
        {{0, 1, 1}, {1, 1, 0}},
        // Z
        {{1, 1, 0}, {0, 1, 1}},
        // J
        {{1, 0, 0}, {1, 1, 1}},
        // L
        {{0, 0, 1}, {1, 1, 1}},
    };

    private static final Color[] PIECE_COLORS = {
        new Color.Rgb(0, 255, 255),   // I - cyan
        new Color.Rgb(255, 255, 0),   // O - yellow
        new Color.Rgb(160, 32, 240),  // T - purple
        new Color.Rgb(0, 255, 0),     // S - green
        new Color.Rgb(255, 0, 0),     // Z - red
        new Color.Rgb(0, 0, 255),     // J - blue
        new Color.Rgb(255, 165, 0),   // L - orange
    };

    static class TetrisComponent extends Component implements InteractiveExample {
        private final Color[][] board = new Color[BOARD_HEIGHT][BOARD_WIDTH];
        private final Random rng = new Random();
        private int currentPieceIdx;
        private int nextPieceIdx;
        private int[][] currentShape;
        private int pieceX, pieceY;
        private Color pieceColor;
        private long lastDropTime;
        private long dropInterval = BASE_DROP_MS;
        private int score;
        private int level = 1;
        private int lines;
        private boolean gameOver;
        private boolean paused;
        private long lastRenderTime;

        TetrisComponent() {
            currentPieceIdx = rng.nextInt(SHAPES.length);
            nextPieceIdx = rng.nextInt(SHAPES.length);
            spawnPiece();
            lastDropTime = System.currentTimeMillis();
            lastRenderTime = System.currentTimeMillis();
        }

        @Override
        public void handleKey(String key) {
            if (gameOver) {
                if (key.equals("space") || key.equals("p")) {
                    reset();
                }
                return;
            }
            if (key.equals("p")) {
                paused = !paused;
                return;
            }
            if (paused) return;

            switch (key) {
                case "left"  -> tryMove(-1, 0);
                case "right" -> tryMove(1, 0);
                case "down"  -> { if (tryMove(0, 1)) { score += 1; } }
                case "up"    -> tryRotate();
                case "space" -> hardDrop();
            }
        }

        private void reset() {
            for (int y = 0; y < BOARD_HEIGHT; y++) {
                Arrays.fill(board[y], null);
            }
            score = 0;
            level = 1;
            lines = 0;
            dropInterval = BASE_DROP_MS;
            gameOver = false;
            paused = false;
            currentPieceIdx = rng.nextInt(SHAPES.length);
            nextPieceIdx = rng.nextInt(SHAPES.length);
            spawnPiece();
            lastDropTime = System.currentTimeMillis();
        }

        private void spawnPiece() {
            currentShape = deepCopy(SHAPES[currentPieceIdx]);
            pieceColor = PIECE_COLORS[currentPieceIdx];
            pieceX = BOARD_WIDTH / 2 - currentShape[0].length / 2;
            pieceY = 0;

            if (!canPlace(currentShape, pieceX, pieceY)) {
                gameOver = true;
            }
        }

        private void advancePiece() {
            currentPieceIdx = nextPieceIdx;
            nextPieceIdx = rng.nextInt(SHAPES.length);
            spawnPiece();
        }

        private boolean tryMove(int dx, int dy) {
            if (canPlace(currentShape, pieceX + dx, pieceY + dy)) {
                pieceX += dx;
                pieceY += dy;
                return true;
            }
            return false;
        }

        private void tryRotate() {
            int[][] rotated = rotateCW(currentShape);
            // Try normal rotation
            if (canPlace(rotated, pieceX, pieceY)) {
                currentShape = rotated;
                return;
            }
            // Wall kick: try offset left
            if (canPlace(rotated, pieceX - 1, pieceY)) {
                currentShape = rotated;
                pieceX--;
                return;
            }
            // Wall kick: try offset right
            if (canPlace(rotated, pieceX + 1, pieceY)) {
                currentShape = rotated;
                pieceX++;
                return;
            }
            // Wall kick: try offset left 2
            if (canPlace(rotated, pieceX - 2, pieceY)) {
                currentShape = rotated;
                pieceX -= 2;
            }
        }

        private void hardDrop() {
            while (tryMove(0, 1)) {
                score += 2;
            }
            lockPiece();
        }

        private void lockPiece() {
            // Write piece to board
            for (int y = 0; y < currentShape.length; y++) {
                for (int x = 0; x < currentShape[y].length; x++) {
                    if (currentShape[y][x] != 0) {
                        int by = pieceY + y;
                        int bx = pieceX + x;
                        if (by >= 0 && by < BOARD_HEIGHT && bx >= 0 && bx < BOARD_WIDTH) {
                            board[by][bx] = pieceColor;
                        }
                    }
                }
            }

            // Check for completed lines
            int cleared = clearLines();
            if (cleared > 0) {
                lines += cleared;
                score += switch (cleared) {
                    case 1 -> 100 * level;
                    case 2 -> 300 * level;
                    case 3 -> 500 * level;
                    case 4 -> 800 * level;
                    default -> 0;
                };
                level = lines / 10 + 1;
                dropInterval = Math.max(50, BASE_DROP_MS - (level - 1) * 50L);
            }

            advancePiece();
            lastDropTime = System.currentTimeMillis();
        }

        private int clearLines() {
            int cleared = 0;
            for (int y = BOARD_HEIGHT - 1; y >= 0; y--) {
                boolean full = true;
                for (int x = 0; x < BOARD_WIDTH; x++) {
                    if (board[y][x] == null) {
                        full = false;
                        break;
                    }
                }
                if (full) {
                    // Shift everything down
                    for (int sy = y; sy > 0; sy--) {
                        System.arraycopy(board[sy - 1], 0, board[sy], 0, BOARD_WIDTH);
                    }
                    Arrays.fill(board[0], null);
                    cleared++;
                    y++; // Re-check this row
                }
            }
            return cleared;
        }

        private boolean canPlace(int[][] shape, int px, int py) {
            for (int y = 0; y < shape.length; y++) {
                for (int x = 0; x < shape[y].length; x++) {
                    if (shape[y][x] == 0) continue;
                    int bx = px + x;
                    int by = py + y;
                    if (bx < 0 || bx >= BOARD_WIDTH || by >= BOARD_HEIGHT) return false;
                    if (by < 0) continue; // Above the board is OK
                    if (board[by][bx] != null) return false;
                }
            }
            return true;
        }

        // ---------- Rendering ----------

        private static final Style BORDER_STYLE = new Style(Color.WHITE, Color.RESET, Set.of());
        private static final Style TITLE_STYLE = new Style(Color.CYAN, Color.RESET,
            Set.of(Modifier.BOLD));
        private static final Style LABEL_STYLE = new Style(Color.GRAY, Color.RESET, Set.of());
        private static final Style VALUE_STYLE = new Style(Color.WHITE, Color.RESET,
            Set.of(Modifier.BOLD));
        private static final Style GHOST_STYLE = new Style(Color.DARK_GRAY, Color.RESET, Set.of());
        private static final Style PAUSE_STYLE = new Style(Color.YELLOW, Color.RESET,
            Set.of(Modifier.BOLD));

        @Override
        public void render(Rect area, Buffer buffer) {
            new Clear().render(area, buffer);
            long now = System.currentTimeMillis();

            // Gravity
            if (!gameOver && !paused && (now - lastDropTime >= dropInterval)) {
                if (!tryMove(0, 1)) {
                    lockPiece();
                }
                lastDropTime = now;
            }
            lastRenderTime = now;

            // Outer border
            var outer = Block.bordered(BorderStyle.ROUNDED)
                .title(" TETRIS ")
                .titleStyle(TITLE_STYLE);
            outer.render(area, buffer);
            var inner = outer.innerRect(area);

            // Board area
            int cellW = 2;
            int boardPixelW = BOARD_WIDTH * cellW + (BOARD_WIDTH - 1); // + gaps
            int boardPixelH = BOARD_HEIGHT;
            int boardX = inner.x() + (inner.width() - boardPixelW - 22) / 2;
            int boardY = inner.y() + 1;

            // Draw the board
            var boardBlock = Block.bordered();
            Rect boardArea = new Rect(boardX - 1, boardY - 1,
                boardPixelW + 2, boardPixelH + 2);
            boardBlock.render(boardArea, buffer);

            // Draw placed pieces
            for (int y = 0; y < BOARD_HEIGHT; y++) {
                for (int x = 0; x < BOARD_WIDTH; x++) {
                    if (board[y][x] != null) {
                        drawCell(boardX + x * (cellW + 1), boardY + y, board[y][x],
                            cellW, buffer);
                    } else {
                        drawEmpty(boardX + x * (cellW + 1), boardY + y, cellW, buffer);
                    }
                }
            }

            // Draw ghost piece (shadow where piece would land)
            int ghostY = pieceY;
            while (canPlace(currentShape, pieceX, ghostY + 1)) {
                ghostY++;
            }
            if (ghostY != pieceY) {
                drawShape(currentShape, pieceX, ghostY, boardX, boardY,
                    cellW, GHOST_STYLE, buffer);
            }

            // Draw current piece
            drawShape(currentShape, pieceX, pieceY, boardX, boardY,
                cellW, pieceColor, buffer);

            // Sidebar
            int sidebarX = boardX + boardPixelW + 4;
            int sy = boardY;

            // Score
            buffer.setString(sidebarX, sy, "SCORE", LABEL_STYLE);
            buffer.setString(sidebarX, sy + 1, String.format("%8d", score), VALUE_STYLE);

            // Level
            sy += 3;
            buffer.setString(sidebarX, sy, "LEVEL", LABEL_STYLE);
            buffer.setString(sidebarX, sy + 1, String.format("%8d", level), VALUE_STYLE);

            // Lines
            sy += 3;
            buffer.setString(sidebarX, sy, "LINES", LABEL_STYLE);
            buffer.setString(sidebarX, sy + 1, String.format("%8d", lines), VALUE_STYLE);

            // Next piece
            sy += 3;
            buffer.setString(sidebarX, sy, "NEXT", LABEL_STYLE);
            int[][] nextShape = SHAPES[nextPieceIdx];
            Color nextColor = PIECE_COLORS[nextPieceIdx];
            for (int y = 0; y < nextShape.length; y++) {
                for (int x = 0; x < nextShape[y].length; x++) {
                    if (nextShape[y][x] != 0) {
                        buffer.setCell(sidebarX + x * 2, sy + 1 + y,
                            new Cell('█', new Style(nextColor, Color.RESET, Set.of())));
                        buffer.setCell(sidebarX + x * 2 + 1, sy + 1 + y,
                            new Cell('█', new Style(nextColor, Color.RESET, Set.of())));
                    }
                }
            }

            // Controls
            sy += 5;
            buffer.setString(sidebarX, sy, "CONTROLS", LABEL_STYLE);
            String[] controls = {
                "← →  Move",
                "↑     Rotate",
                "↓     Soft drop",
                "Space Hard drop",
                "p     Pause",
                "q     Quit",
            };
            for (int i = 0; i < controls.length && sy + 1 + i < inner.y() + inner.height(); i++) {
                buffer.setString(sidebarX, sy + 1 + i, controls[i],
                    new Style(Color.GRAY, Color.RESET, Set.of()));
            }

            // Game over overlay
            if (gameOver) {
                String goText = "GAME OVER";
                int goX = boardX + (boardPixelW - goText.length()) / 2;
                int goY = boardY + boardPixelH / 2;
                for (int x = boardX; x < boardX + boardPixelW; x++) {
                    for (int y = goY - 1; y <= goY + 1; y++) {
                        Cell c = buffer.getCell(x, y);
                        buffer.setCell(x, y, new Cell(c.ch(),
                            new Style(Color.WHITE, Color.BLACK, Set.of())));
                    }
                }
                buffer.setString(goX, goY, goText,
                    new Style(Color.RED, Color.BLACK, Set.of(Modifier.BOLD)));
                String restart = "Space to restart";
                buffer.setString(boardX + (boardPixelW - restart.length()) / 2, goY + 2,
                    restart, new Style(Color.GRAY, Color.BLACK, Set.of()));
            }

            // Pause overlay
            if (paused && !gameOver) {
                String pText = "PAUSED";
                int pX = boardX + (boardPixelW - pText.length()) / 2;
                int pY = boardY + boardPixelH / 2;
                buffer.setString(pX, pY, pText, PAUSE_STYLE);
            }

            // Status bar
            int statusY = area.y() + area.height() - 1;
            for (int x = area.x(); x < area.x() + area.width(); x++) {
                buffer.setCell(x, statusY, new Cell(' ',
                    new Style(Color.WHITE, Color.BLUE, Set.of())));
            }
            String status = gameOver
                ? " Game Over — Press Space to restart "
                : paused ? " Paused " : " Playing ";
            buffer.setString(area.x() + 2, statusY, status,
                new Style(Color.WHITE, Color.BLUE, Set.of()));
        }

        private void drawShape(int[][] shape, int px, int py,
                               int boardX, int boardY, int cellW,
                               Color color, Buffer buffer) {
            for (int y = 0; y < shape.length; y++) {
                for (int x = 0; x < shape[y].length; x++) {
                    if (shape[y][x] == 0) continue;
                    int drawX = boardX + (px + x) * (cellW + 1);
                    int drawY = boardY + py + y;
                    if (drawY >= boardY && drawY < boardY + BOARD_HEIGHT) {
                        drawCell(drawX, drawY, color, cellW, buffer);
                    }
                }
            }
        }

        private void drawShape(int[][] shape, int px, int py,
                               int boardX, int boardY, int cellW,
                               Style style, Buffer buffer) {
            for (int y = 0; y < shape.length; y++) {
                for (int x = 0; x < shape[y].length; x++) {
                    if (shape[y][x] == 0) continue;
                    int drawX = boardX + (px + x) * (cellW + 1);
                    int drawY = boardY + py + y;
                    if (drawY >= boardY && drawY < boardY + BOARD_HEIGHT) {
                        buffer.setCell(drawX, drawY, new Cell('░', style));
                        buffer.setCell(drawX + 1, drawY, new Cell('░', style));
                    }
                }
            }
        }

        private void drawCell(int drawX, int drawY, Color color, int cellW, Buffer buffer) {
            var cellStyle = new Style(color, Color.RESET, Set.of());
            for (int i = 0; i < cellW; i++) {
                buffer.setCell(drawX + i, drawY, new Cell('█', cellStyle));
            }
        }

        private void drawEmpty(int drawX, int drawY, int cellW, Buffer buffer) {
            for (int i = 0; i < cellW; i++) {
                buffer.setCell(drawX + i, drawY,
                    new Cell('·', new Style(Color.DARK_GRAY, Color.RESET, Set.of())));
            }
        }

        // ---------- Helpers ----------

        private static int[][] rotateCW(int[][] shape) {
            int rows = shape.length;
            int cols = shape[0].length;
            int[][] result = new int[cols][rows];
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) {
                    result[x][rows - 1 - y] = shape[y][x];
                }
            }
            return result;
        }

        private static int[][] deepCopy(int[][] src) {
            int[][] dst = new int[src.length][];
            for (int i = 0; i < src.length; i++) {
                dst[i] = Arrays.copyOf(src[i], src[i].length);
            }
            return dst;
        }
    }
}
