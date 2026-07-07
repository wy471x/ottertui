package com.ottertui.backend.jline;

import com.ottertui.core.*;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingReader;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

public class JLineBackend implements TerminalBackend {

    private final Terminal terminal;
    private final NonBlockingReader reader;
    private final PrintWriter output;

    public JLineBackend() throws IOException {
        this.terminal = TerminalBuilder.builder()
            .name("OtterTUI")
            .jansi(true)
            .system(true)
            .build();

        this.reader = terminal.reader();
        this.output = terminal.writer();
    }

    @Override
    public void enterRawMode() {
        terminal.enterRawMode();
        terminal.trackMouse(Terminal.MouseTracking.Any);
    }

    @Override
    public void exitRawMode() {
        try {
            terminal.trackMouse(Terminal.MouseTracking.Off);
            terminal.close();
        } catch (IOException e) {
            // best-effort
        }
    }

    @Override
    public void flush(Buffer buffer) {
        var sb = new StringBuilder();

        var prevStyle = Style.DEFAULT;
        for (int y = 0; y < buffer.height(); y++) {
            for (int x = 0; x < buffer.width(); x++) {
                Cell cell = buffer.getCell(x, y);
                sb.append(cursorTo(y + 1, x + 1));
                if (!cell.style().equals(prevStyle)) {
                    sb.append(styleToSgr(cell.style()));
                    prevStyle = cell.style();
                }
                sb.append(cell.ch());
            }
        }

        sb.append(Reset);
        output.write(sb.toString());
        output.flush();
    }

    @Override
    public TerminalSize size() {
        org.jline.terminal.Size s = terminal.getSize();
        return new TerminalSize(s.getColumns(), s.getRows());
    }

    @Override
    public Optional<InputEvent> readInput() {
        try {
            int ch = reader.read(1);
            if (ch < 0) return Optional.empty();
            if (ch == 0x1B) {
                return parseEscapeSequence();
            }
            return Optional.of(decodeKey(ch));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    @Override
    public void showCursor() {
        output.write("\033[?25h");
        output.flush();
    }

    @Override
    public void hideCursor() {
        output.write("\033[?25l");
        output.flush();
    }

    @Override
    public void clearScreen() {
        output.write("\033[2J");
        output.flush();
    }

    // -- private helpers --

    private static final String Reset = "\033[0m";

    private String cursorTo(int row, int col) {
        return "\033[" + row + ";" + col + "H";
    }

    private String styleToSgr(Style style) {
        var sb = new StringBuilder();
        sb.append("\033[0");

        if (style.foreground() instanceof Color.Rgb rgb) {
            sb.append(";38;2;").append(rgb.r()).append(";").append(rgb.g()).append(";").append(rgb.b());
        }
        if (style.background() instanceof Color.Rgb rgb) {
            sb.append(";48;2;").append(rgb.r()).append(";").append(rgb.g()).append(";").append(rgb.b());
        }
        if (style.modifiers().contains(Modifier.BOLD))       sb.append(";1");
        if (style.modifiers().contains(Modifier.DIM))         sb.append(";2");
        if (style.modifiers().contains(Modifier.ITALIC))      sb.append(";3");
        if (style.modifiers().contains(Modifier.UNDERLINE))   sb.append(";4");
        if (style.modifiers().contains(Modifier.REVERSED))    sb.append(";7");
        if (style.modifiers().contains(Modifier.CROSSED_OUT)) sb.append(";9");

        sb.append("m");
        return sb.toString();
    }

    private InputEvent decodeKey(int ch) {
        return switch (ch) {
            case '\t'       -> InputEvent.key(KeyCode.TAB);
            case '\r', '\n' -> InputEvent.key(KeyCode.ENTER);
            case 127, '\b'  -> InputEvent.key(KeyCode.BACKSPACE);
            default -> (ch >= 32 && ch < 127)
                ? InputEvent.charKey((char) ch)
                : new InputEvent.Unknown();
        };
    }

    private Optional<InputEvent> parseEscapeSequence() {
        try {
            int b = reader.read(20);
            if (b < 0) {
                return Optional.of(InputEvent.key(KeyCode.ESC));
            }

            if (b == '[') {
                return parseCsiSequence();
            }
            if (b == 'O') {
                return parseSs3Sequence();
            }

            // Alt + key
            if (b >= 32 && b < 127) {
                return Optional.of(InputEvent.charKey((char) b));
            }
            return Optional.of(InputEvent.key(KeyCode.ESC));
        } catch (IOException e) {
            return Optional.of(InputEvent.key(KeyCode.ESC));
        }
    }

    private Optional<InputEvent> parseCsiSequence() {
        try {
            StringBuilder params = new StringBuilder();
            int terminator;
            while (true) {
                int b = reader.read(20);
                if (b < 0) return Optional.empty();
                if (b >= 0x40 && b <= 0x7E) {
                    terminator = b;
                    break;
                }
                params.append((char) b);
            }

            return Optional.of(switch (terminator) {
                case 'A' -> InputEvent.key(KeyCode.UP);
                case 'B' -> InputEvent.key(KeyCode.DOWN);
                case 'C' -> InputEvent.key(KeyCode.RIGHT);
                case 'D' -> InputEvent.key(KeyCode.LEFT);
                case 'H' -> InputEvent.key(KeyCode.HOME);
                case 'F' -> InputEvent.key(KeyCode.END);
                case '~' -> switch (params.toString()) {
                    case "1", "7" -> InputEvent.key(KeyCode.HOME);
                    case "2" -> InputEvent.key(KeyCode.INSERT);
                    case "3" -> InputEvent.key(KeyCode.DELETE);
                    case "4", "8" -> InputEvent.key(KeyCode.END);
                    case "5" -> InputEvent.key(KeyCode.PAGE_UP);
                    case "6" -> InputEvent.key(KeyCode.PAGE_DOWN);
                    case "11" -> InputEvent.key(KeyCode.F1);
                    case "12" -> InputEvent.key(KeyCode.F2);
                    case "13" -> InputEvent.key(KeyCode.F3);
                    case "14" -> InputEvent.key(KeyCode.F4);
                    case "15" -> InputEvent.key(KeyCode.F5);
                    case "17" -> InputEvent.key(KeyCode.F6);
                    case "18" -> InputEvent.key(KeyCode.F7);
                    case "19" -> InputEvent.key(KeyCode.F8);
                    case "20" -> InputEvent.key(KeyCode.F9);
                    case "21" -> InputEvent.key(KeyCode.F10);
                    case "23" -> InputEvent.key(KeyCode.F11);
                    case "24" -> InputEvent.key(KeyCode.F12);
                    default  -> new InputEvent.Unknown();
                };
                default -> new InputEvent.Unknown();
            });
        } catch (IOException e) {
            return Optional.of(new InputEvent.Unknown());
        }
    }

    private Optional<InputEvent> parseSs3Sequence() {
        try {
            int b = reader.read(20);
            if (b < 0) return Optional.empty();
            KeyCode code = switch (b) {
                case 'P' -> KeyCode.F1;
                case 'Q' -> KeyCode.F2;
                case 'R' -> KeyCode.F3;
                case 'S' -> KeyCode.F4;
                default -> null;
            };
            return Optional.of(code != null ? InputEvent.key(code) : new InputEvent.Unknown());
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
