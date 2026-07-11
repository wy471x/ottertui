package com.ottertui.backend.ffm;

import com.ottertui.core.*;
import org.fusesource.jansi.AnsiConsole;

import java.io.*;
import java.lang.foreign.*;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static java.lang.foreign.ValueLayout.*;

/**
 * FFM Backend — zero external TUI dependency, pure JDK 22+ FFM API + Jansi.
 *
 * <p>Uses FFM API for: termios raw mode, ioctl TIOCGWINSZ, read(2),
 * signal(SIGWINCH). Uses Jansi for cross-platform ANSI output on Windows.</p>
 *
 * <p>Requires JDK 22+ (java.lang.foreign).</p>
 * <p>Runtime: {@code --enable-native-access=ALL-UNNAMED}</p>
 */
public class FfmBackend implements TerminalBackend {

    // --- ioctl / winsize ---
    // struct winsize { unsigned short ws_row; unsigned short ws_col;
    //                  unsigned short ws_xpixel; unsigned short ws_ypixel; };
    private static final StructLayout WINSIZE_LAYOUT = MemoryLayout.structLayout(
        JAVA_SHORT_UNALIGNED.withName("ws_row"),
        JAVA_SHORT_UNALIGNED.withName("ws_col"),
        JAVA_SHORT_UNALIGNED.withName("ws_xpixel"),
        JAVA_SHORT_UNALIGNED.withName("ws_ypixel")
    );

    // macOS: TIOCGWINSZ = 0x40087468
    private static final long TIOCGWINSZ = 0x40087468L;

    private static final Linker LINKER = Linker.nativeLinker();
    private static final SymbolLookup LIBC = LibC.LOOKUP;

    private static final MethodHandle ioctl;
    private static final MethodHandle readHandle;
    private static final MethodHandle writeHandle;

    static {
        ioctl = LINKER.downcallHandle(
            LIBC.find("ioctl").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_LONG, JAVA_LONG)
        );
        readHandle = LINKER.downcallHandle(
            LIBC.find("read").orElseThrow(),
            FunctionDescriptor.of(JAVA_LONG, JAVA_INT, JAVA_LONG, JAVA_LONG)
        );
        writeHandle = LINKER.downcallHandle(
            LIBC.find("write").orElseThrow(),
            FunctionDescriptor.of(JAVA_LONG, JAVA_INT, JAVA_LONG, JAVA_LONG)
        );
    }

    private static final int STDIN = 0;
    private static final int STDOUT = 1;

    private final Termios termios;
    private final OutputStream output;
    private Buffer lastBuffer;
    private TerminalSize lastSize;

    public FfmBackend() {
        this.termios = Termios.forStdin();
        // Jansi wraps stdout for Windows ANSI support;
        // on Unix it is a transparent pass-through
        this.output = AnsiConsole.out();
    }

    // --- TerminalBackend impl ---

    @Override
    public void enterRawMode() {
        termios.enterRawMode();
        writeEsc("?1049h");  // enter alternate screen
        writeEsc("?25l");    // hide cursor
        writeEsc("?1000h");  // enable mouse tracking
        writeEsc("?1002h");
        writeEsc("?1015h");
        writeEsc("?1006h");
        flushOutput();
    }

    @Override
    public void exitRawMode() {
        writeEsc("?1006l");
        writeEsc("?1015l");
        writeEsc("?1002l");
        writeEsc("?1000l");
        writeEsc("?25h");    // show cursor
        writeEsc("?1049l");  // exit alternate screen
        write(CSI + "0m");   // SGR reset
        flushOutput();
        termios.close();
    }

    @Override
    public void flush(Buffer buffer) {
        var sb = new StringBuilder();

        for (int y = 0; y < buffer.height(); y++) {
            for (int x = 0; x < buffer.width(); x++) {
                Cell cell = buffer.getCell(x, y);

                // Diff rendering: skip unchanged cells
                if (lastBuffer != null) {
                    Cell prev = lastBuffer.getCell(x, y);
                    if (prev != null && prev.ch() == cell.ch()
                            && prev.style().equals(cell.style())) {
                        continue;
                    }
                }

                sb.append(AnsiUtil.cursorTo(y + 1, x + 1));
                sb.append(AnsiUtil.styleToSgr(cell.style()));
                sb.append(cell.ch());
            }
        }

        sb.append(CSI + "0m");
        if (lastSize != null) {
            sb.append(CSI).append(lastSize.height()).append(";0H");
        }

        write(sb.toString());
        flushOutput();
        lastBuffer = buffer;
    }

    @Override
    public TerminalSize size() {
        try (var arena = Arena.ofConfined()) {
            var winsize = arena.allocate(WINSIZE_LAYOUT);
            int rc = (int) ioctl.invokeExact(STDOUT, TIOCGWINSZ, winsize);
            if (rc != 0) return fallbackSize();

            short rows = winsize.get(JAVA_SHORT_UNALIGNED, 0);
            short cols = winsize.get(JAVA_SHORT_UNALIGNED, 2);
            lastSize = new TerminalSize(
                Short.toUnsignedInt(cols),
                Short.toUnsignedInt(rows));
            return lastSize;
        } catch (Throwable e) {
            return fallbackSize();
        }
    }

    @Override
    public Optional<InputEvent> readInput() {
        try (var arena = Arena.ofConfined()) {
            var buf = arena.allocate(1);
            long n = (long) readHandle.invokeExact(STDIN, buf, 1L);
            if (n <= 0) return Optional.empty();

            int first = Byte.toUnsignedInt(buf.get(JAVA_BYTE, 0));

            if (first == 0x1B) {
                return parseEscapeSequence(arena);
            }
            if (first == '\r' || first == '\n') return Optional.of(InputEvent.key(KeyCode.ENTER));
            if (first == '\t') return Optional.of(InputEvent.key(KeyCode.TAB));
            if (first == 127 || first == 8) return Optional.of(InputEvent.key(KeyCode.BACKSPACE));
            if (first >= 32) return Optional.of(InputEvent.charKey((char) first));

            return Optional.empty();
        } catch (Throwable e) {
            return Optional.empty();
        }
    }

    @Override
    public void showCursor() {
        writeEsc("?25h");
        flushOutput();
    }

    @Override
    public void hideCursor() {
        writeEsc("?25l");
        flushOutput();
    }

    @Override
    public void clearScreen() {
        writeEsc("2J");
        writeEsc("H");
        flushOutput();
    }

    // --- private helpers ---

    private static final String CSI = "\033[";

    private void write(String s) {
        try {
            byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
            try (var arena = Arena.ofConfined()) {
                var seg = MemorySegment.ofArray(bytes);
                writeHandle.invokeExact(STDOUT, seg, (long) bytes.length);
            }
        } catch (Throwable e) {
            // ignore write errors
        }
    }

    private void writeEsc(String code) {
        write(CSI + code);
    }

    private void flushOutput() {
        try {
            output.flush();
        } catch (IOException e) {
            // ignore
        }
    }

    private TerminalSize fallbackSize() {
        return lastSize != null ? lastSize : new TerminalSize(80, 24);
    }

    // --- Escape sequence parser ---

    private Optional<InputEvent> parseEscapeSequence(Arena arena) throws Throwable {
        var buf = arena.allocate(1);

        // Check if there's a second byte
        long n = (long) readHandle.invokeExact(STDIN, buf, 1L);
        if (n <= 0) return Optional.of(InputEvent.key(KeyCode.ESC));

        int second = Byte.toUnsignedInt(buf.get(JAVA_BYTE, 0));
        if (second == '[') {
            return parseCsiSequence(arena);
        }

        return Optional.of(InputEvent.key(KeyCode.ESC));
    }

    private Optional<InputEvent> parseCsiSequence(Arena arena) throws Throwable {
        var buf = arena.allocate(1);

        // Consume parameter bytes until terminator
        int terminator = 0;
        while (true) {
            long n = (long) readHandle.invokeExact(STDIN, buf, 1L);
            if (n <= 0) return Optional.empty();
            int b = Byte.toUnsignedInt(buf.get(JAVA_BYTE, 0));
            if (b >= 0x40 && b <= 0x7E) {
                terminator = b;
                break;
            }
        }

        return Optional.of(switch (terminator) {
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
        });
    }
}
