package com.ottertui.backend.aesh;

import com.ottertui.core.*;
import org.aesh.terminal.tty.TerminalBuilder;
import org.aesh.terminal.Attributes;
import org.aesh.terminal.Terminal;
import org.aesh.terminal.tty.Size;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * AeshBackend — uses Aesh Readline as a terminal backend.
 *
 * <p>Aesh is a zero-dependency Java terminal I/O library with built-in
 * SSH/Telnet/WebSocket server support.</p>
 */
public class AeshBackend implements TerminalBackend {

    private final Terminal terminal;
    private final InputStream input;
    private final OutputStream output;

    public AeshBackend() throws IOException {
        this.terminal = TerminalBuilder.builder()
            .name("ottertui")
            .nativeSignals(true)
            .build();
        this.input = terminal.input();
        this.output = terminal.output();
    }

    @Override
    public void enterRawMode() {
        Attributes attr = terminal.getAttributes();
        attr.setLocalFlag(Attributes.LocalFlag.ECHO, false);
        attr.setLocalFlag(Attributes.LocalFlag.ICANON, false);
        attr.setLocalFlag(Attributes.LocalFlag.ISIG, true);
        terminal.setAttributes(attr);
        writeEsc("?1049h");  // enter alternate screen
        writeEsc("?25l");    // hide cursor
        writeEsc("?1000h");  // enable mouse tracking
        writeEsc("?1002h");
        writeEsc("?1015h");
        writeEsc("?1006h");
    }

    @Override
    public void exitRawMode() {
        try {
            writeEsc("?1006l");
            writeEsc("?1015l");
            writeEsc("?1002l");
            writeEsc("?1000l");
            writeEsc("?25h");
            writeEsc("?1049l");
            terminal.close();
        } catch (IOException e) {
            // best-effort
        }
    }

    @Override
    public void flush(Buffer buffer) {
        var sb = new StringBuilder();
        for (int y = 0; y < buffer.height(); y++) {
            for (int x = 0; x < buffer.width(); x++) {
                Cell cell = buffer.getCell(x, y);
                if (cell.ch() == ' ' && cell.style().equals(Style.DEFAULT)) continue;

                sb.append(CSI).append(y + 1).append(';').append(x + 1).append('H');
                sb.append(styleToSgr(cell.style()));
                sb.append(cell.ch());
            }
        }
        sb.append(CSI + "0m");
        try {
            output.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            output.flush();
        } catch (IOException e) {
            // ignore render errors
        }
    }

    @Override
    public TerminalSize size() {
        Size s = terminal.getSize();
        if (s == null) return new TerminalSize(80, 24);
        return new TerminalSize(s.getWidth(), s.getHeight());
    }

    @Override
    public Optional<InputEvent> readInput() {
        try {
            if (input.available() == 0) return Optional.empty();

            // Read escape sequences byte by byte
            int first = input.read();
            if (first == -1) return Optional.empty();

            if (first == 27) {
                // Escape sequence — read next bytes if available
                if (input.available() > 0) {
                    int second = input.read();
                    if (second == '[' && input.available() > 0) {
                        int third = input.read();
                        // Consume remaining bytes of the sequence
                        while (input.available() > 0) input.read();
                        return Optional.of(decodeCsi(third));
                    }
                    return Optional.of(InputEvent.key(KeyCode.ESC));
                }
                return Optional.of(InputEvent.key(KeyCode.ESC));
            }

            if (first == '\r' || first == '\n') return Optional.of(InputEvent.key(KeyCode.ENTER));
            if (first == '\t') return Optional.of(InputEvent.key(KeyCode.TAB));
            if (first == 127 || first == 8) return Optional.of(InputEvent.key(KeyCode.BACKSPACE));
            if (first >= 32) return Optional.of(InputEvent.charKey((char) first));

            return Optional.empty();
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    @Override
    public void showCursor() {
        writeEsc("?25h");
    }

    @Override
    public void hideCursor() {
        writeEsc("?25l");
    }

    @Override
    public void clearScreen() {
        writeEsc("2J");
        writeEsc("H");
    }

    // -- private helpers --

    private static final String CSI = "\033[";

    private void writeEsc(String code) {
        try {
            output.write((CSI + code).getBytes(StandardCharsets.UTF_8));
            output.flush();
        } catch (IOException e) {
            // ignore
        }
    }

    private String styleToSgr(Style style) {
        var sb = new StringBuilder(CSI);
        boolean hasContent = false;

        // Foreground
        String fg = colorToAnsiFg(style.foreground());
        if (!fg.isEmpty()) {
            sb.append(fg);
            hasContent = true;
        }

        if (hasContent) sb.append('m').append(CSI);
        hasContent = false;

        // Background
        String bg = colorToAnsiBg(style.background());
        if (!bg.isEmpty()) {
            sb.append(bg);
            hasContent = true;
        }

        if (hasContent) sb.append('m').append(CSI);
        hasContent = false;

        // Modifiers
        if (style.modifiers().contains(Modifier.BOLD))       { sb.append("1;"); hasContent = true; }
        if (style.modifiers().contains(Modifier.ITALIC))     { sb.append("3;"); hasContent = true; }
        if (style.modifiers().contains(Modifier.UNDERLINE))  { sb.append("4;"); hasContent = true; }
        if (style.modifiers().contains(Modifier.REVERSED))   { sb.append("7;"); hasContent = true; }
        if (style.modifiers().contains(Modifier.CROSSED_OUT)) { sb.append("9;"); hasContent = true; }

        if (hasContent) {
            if (sb.charAt(sb.length() - 1) == ';') {
                sb.setCharAt(sb.length() - 1, 'm');
            }
        }
        return sb.toString();
    }

    private static String colorToAnsiFg(Color c) {
        if (c instanceof Color.Rgb r) return "38;2;" + r.r() + ";" + r.g() + ";" + r.b();
        if (c == Color.RESET) return "39";
        if (c == Color.BLACK)        return "30";
        if (c == Color.RED)          return "31";
        if (c == Color.GREEN)        return "32";
        if (c == Color.YELLOW)       return "33";
        if (c == Color.BLUE)         return "34";
        if (c == Color.MAGENTA)      return "35";
        if (c == Color.CYAN)         return "36";
        if (c == Color.WHITE)        return "37";
        if (c == Color.GRAY)         return "90";
        if (c == Color.DARK_GRAY)    return "90";
        return "";
    }

    private static String colorToAnsiBg(Color c) {
        if (c instanceof Color.Rgb r) return "48;2;" + r.r() + ";" + r.g() + ";" + r.b();
        if (c == Color.RESET) return "49";
        if (c == Color.BLACK)        return "40";
        if (c == Color.RED)          return "41";
        if (c == Color.GREEN)        return "42";
        if (c == Color.YELLOW)       return "43";
        if (c == Color.BLUE)         return "44";
        if (c == Color.MAGENTA)      return "45";
        if (c == Color.CYAN)         return "46";
        if (c == Color.WHITE)        return "47";
        if (c == Color.GRAY)         return "100";
        if (c == Color.DARK_GRAY)    return "100";
        return "";
    }

    private InputEvent decodeCsi(int code) {
        return switch (code) {
            case 'A' -> InputEvent.key(KeyCode.UP);
            case 'B' -> InputEvent.key(KeyCode.DOWN);
            case 'C' -> InputEvent.key(KeyCode.RIGHT);
            case 'D' -> InputEvent.key(KeyCode.LEFT);
            case 'H' -> InputEvent.key(KeyCode.HOME);
            case 'F' -> InputEvent.key(KeyCode.END);
            case '5' -> InputEvent.key(KeyCode.PAGE_UP);
            case '6' -> InputEvent.key(KeyCode.PAGE_DOWN);
            case '3' -> InputEvent.key(KeyCode.DELETE);
            default  -> new InputEvent.Unknown();
        };
    }
}
