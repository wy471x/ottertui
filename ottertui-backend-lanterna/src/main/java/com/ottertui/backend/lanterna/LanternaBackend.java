package com.ottertui.backend.lanterna;

import com.ottertui.core.*;

import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Optional;

public class LanternaBackend implements TerminalBackend {

    private final Terminal terminal;

    public LanternaBackend() throws IOException {
        this.terminal = new DefaultTerminalFactory()
            .setTerminalEmulatorTitle("OtterTUI")
            .createTerminal();
    }

    @Override
    public void enterRawMode() {
        try {
            terminal.enterPrivateMode();
            terminal.setCursorVisible(false);
        } catch (IOException e) {
            throw new RuntimeException("Failed to enter raw mode", e);
        }
    }

    @Override
    public void exitRawMode() {
        try {
            terminal.exitPrivateMode();
            terminal.setCursorVisible(true);
            terminal.close();
        } catch (IOException e) {
            // best-effort
        }
    }

    @Override
    public void flush(Buffer buffer) {
        try {
            var size = terminal.getTerminalSize();
            for (int y = 0; y < Math.min(buffer.height(), size.getRows()); y++) {
                for (int x = 0; x < Math.min(buffer.width(), size.getColumns()); x++) {
                    Cell cell = buffer.getCell(x, y);
                    terminal.setCursorPosition(x, y);
                    applyStyle(cell.style());
                    terminal.putCharacter(cell.ch());
                }
            }
            terminal.flush();
            // Reset style after flush
            terminal.resetColorAndSGR();
        } catch (IOException e) {
            // ignore render errors
        }
    }

    @Override
    public TerminalSize size() {
        try {
            var s = terminal.getTerminalSize();
            return new TerminalSize(s.getColumns(), s.getRows());
        } catch (IOException e) {
            return new TerminalSize(80, 24);
        }
    }

    @Override
    public Optional<InputEvent> readInput() {
        try {
            KeyStroke ks = terminal.pollInput();
            if (ks == null) return Optional.empty();
            return Optional.of(decodeKey(ks));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    @Override
    public void showCursor() {
        try {
            terminal.setCursorVisible(true);
        } catch (IOException e) { /* ignore */ }
    }

    @Override
    public void hideCursor() {
        try {
            terminal.setCursorVisible(false);
        } catch (IOException e) { /* ignore */ }
    }

    @Override
    public void clearScreen() {
        try {
            terminal.clearScreen();
        } catch (IOException e) { /* ignore */ }
    }

    // -- private helpers --

    private void applyStyle(Style style) throws IOException {
        if (style.foreground() instanceof Color.Rgb rgb) {
            terminal.setForegroundColor(new TextColor.RGB(rgb.r(), rgb.g(), rgb.b()));
        } else if (style.foreground() instanceof Color.Reset) {
            terminal.setForegroundColor(TextColor.ANSI.DEFAULT);
        }

        if (style.background() instanceof Color.Rgb rgb) {
            terminal.setBackgroundColor(new TextColor.RGB(rgb.r(), rgb.g(), rgb.b()));
        } else if (style.background() instanceof Color.Reset) {
            terminal.setBackgroundColor(TextColor.ANSI.DEFAULT);
        }

        var sgrSet = EnumSet.noneOf(SGR.class);
        if (style.modifiers().contains(Modifier.BOLD))       sgrSet.add(SGR.BOLD);
        if (style.modifiers().contains(Modifier.ITALIC))     sgrSet.add(SGR.ITALIC);
        if (style.modifiers().contains(Modifier.UNDERLINE))  sgrSet.add(SGR.UNDERLINE);
        if (style.modifiers().contains(Modifier.REVERSED))   sgrSet.add(SGR.REVERSE);
        if (style.modifiers().contains(Modifier.CROSSED_OUT)) sgrSet.add(SGR.CROSSED_OUT);

        if (!sgrSet.isEmpty()) {
            for (SGR sgr : sgrSet) {
                terminal.enableSGR(sgr);
            }
        }
    }

    private InputEvent decodeKey(KeyStroke ks) {
        KeyType kt = ks.getKeyType();

        if (kt == KeyType.Character) {
            char c = ks.getCharacter();
            if (ks.isCtrlDown() && c == 'c') {
                return InputEvent.key(KeyCode.CHAR);
            }
            return InputEvent.charKey(c);
        }

        KeyCode code = switch (kt) {
            case ArrowUp    -> KeyCode.UP;
            case ArrowDown  -> KeyCode.DOWN;
            case ArrowLeft  -> KeyCode.LEFT;
            case ArrowRight -> KeyCode.RIGHT;
            case Home       -> KeyCode.HOME;
            case End        -> KeyCode.END;
            case PageUp     -> KeyCode.PAGE_UP;
            case PageDown   -> KeyCode.PAGE_DOWN;
            case Enter      -> KeyCode.ENTER;
            case Tab        -> KeyCode.TAB;
            case Backspace  -> KeyCode.BACKSPACE;
            case Escape     -> KeyCode.ESC;
            case Delete     -> KeyCode.DELETE;
            case Insert     -> KeyCode.INSERT;
            case F1         -> KeyCode.F1;
            case F2         -> KeyCode.F2;
            case F3         -> KeyCode.F3;
            case F4         -> KeyCode.F4;
            case F5         -> KeyCode.F5;
            case F6         -> KeyCode.F6;
            case F7         -> KeyCode.F7;
            case F8         -> KeyCode.F8;
            case F9         -> KeyCode.F9;
            case F10        -> KeyCode.F10;
            case F11        -> KeyCode.F11;
            case F12        -> KeyCode.F12;
            default         -> null;
        };

        return code != null ? InputEvent.key(code) : new InputEvent.Unknown();
    }
}
