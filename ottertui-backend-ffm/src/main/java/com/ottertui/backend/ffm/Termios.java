package com.ottertui.backend.ffm;

import java.lang.foreign.*;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.MethodHandle;
import java.util.HexFormat;

import static java.lang.foreign.ValueLayout.*;

/**
 * Unix terminal raw-mode control via JDK 22+ FFM API.
 *
 * <p>Directly calls tcgetattr(3) / tcsetattr(3) to configure the
 * terminal in non-canonical, no-echo mode for TUI rendering.</p>
 *
 * <p>Requires JDK 22+ (java.lang.foreign finalized).</p>
 * <p>Runtime: {@code --enable-native-access=ALL-UNNAMED}</p>
 */
public final class Termios implements AutoCloseable {

    private static final Linker LINKER = Linker.nativeLinker();
    private static final SymbolLookup LIBC = LibC.LOOKUP;

    // --- macOS termios layout ---
    // struct termios {
    //     tcflag_t c_iflag;     // unsigned long = 8 bytes on 64-bit macOS
    //     tcflag_t c_oflag;
    //     tcflag_t c_cflag;
    //     tcflag_t c_lflag;
    //     cc_t     c_cc[20];    // 20 bytes
    //     speed_t  c_ispeed;    // unsigned long = 8 bytes
    //     speed_t  c_ospeed;
    // };
    // total: 4*8 + 20 + 2*8 = 68 bytes

    private static final StructLayout TERMIOS_LAYOUT = MemoryLayout.structLayout(
        JAVA_LONG_UNALIGNED.withName("c_iflag"),
        JAVA_LONG_UNALIGNED.withName("c_oflag"),
        JAVA_LONG_UNALIGNED.withName("c_cflag"),
        JAVA_LONG_UNALIGNED.withName("c_lflag"),
        MemoryLayout.sequenceLayout(20, JAVA_BYTE).withName("c_cc"),
        JAVA_LONG_UNALIGNED.withName("c_ispeed"),
        JAVA_LONG_UNALIGNED.withName("c_ospeed")
    );

    private static final long IF_OFFSET = TERMIOS_LAYOUT.byteOffset(PathElement.groupElement("c_iflag"));
    private static final long LF_OFFSET = TERMIOS_LAYOUT.byteOffset(PathElement.groupElement("c_lflag"));
    private static final long CC_OFFSET = TERMIOS_LAYOUT.byteOffset(PathElement.groupElement("c_cc"));

    // --- Method handles ---
    private static final MethodHandle tcgetattr;
    private static final MethodHandle tcsetattr;

    static {
        tcgetattr = LINKER.downcallHandle(
            LIBC.find("tcgetattr").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_LONG)
        );
        tcsetattr = LINKER.downcallHandle(
            LIBC.find("tcsetattr").orElseThrow(),
            FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_INT, JAVA_LONG)
        );
    }

    // --- termios constants (macOS) ---
    private static final int TCSANOW = 0;
    private static final int TCSAFLUSH = 2;

    // Local flags
    private static final long ICANON = 0x00000100L;
    private static final long ECHO   = 0x00000008L;
    private static final long ISIG   = 0x00000080L;
    private static final long IEXTEN = 0x00000400L;

    // c_cc indices
    private static final int VMIN  = 16;
    private static final int VTIME = 17;

    private final int fd;
    private final MemorySegment original;
    private final Arena arena;

    private Termios(int fd) {
        this.fd = fd;
        this.arena = Arena.ofConfined();
        this.original = arena.allocate(TERMIOS_LAYOUT);

        int rc;
        try {
            rc = (int) tcgetattr.invokeExact(fd, original);
        } catch (Throwable e) {
            throw new RuntimeException("tcgetattr failed", e);
        }
        if (rc != 0) {
            throw new RuntimeException("tcgetattr returned " + rc);
        }
    }

    /** Create a Termios controller for STDIN (fd 0). */
    public static Termios forStdin() {
        return new Termios(0);
    }

    /** Enter non-canonical raw mode with non-blocking reads. */
    public void enterRawMode() {
        try (var localArena = Arena.ofConfined()) {
            var raw = localArena.allocate(TERMIOS_LAYOUT);
            MemorySegment.copy(original, 0, raw, 0, TERMIOS_LAYOUT.byteSize());

            // Clear canonical flags
            long lflag = raw.get(JAVA_LONG_UNALIGNED, LF_OFFSET);
            lflag &= ~(ICANON | ECHO | ISIG | IEXTEN);
            raw.set(JAVA_LONG_UNALIGNED, LF_OFFSET, lflag);

            // Non-blocking read: VMIN=0, VTIME=0
            raw.set(JAVA_BYTE, CC_OFFSET + VMIN, (byte) 0);
            raw.set(JAVA_BYTE, CC_OFFSET + VTIME, (byte) 0);

            int rc = (int) tcsetattr.invokeExact(fd, TCSAFLUSH, raw);
            if (rc != 0) {
                throw new RuntimeException("tcsetattr failed (rc=" + rc + ")");
            }
        } catch (Throwable e) {
            throw new RuntimeException("Failed to enter raw mode", e);
        }
    }

    /** Restore the original terminal settings. */
    public void restore() {
        try {
            tcsetattr.invokeExact(fd, TCSAFLUSH, original);
        } catch (Throwable e) {
            // best-effort restore
        }
    }

    @Override
    public void close() {
        restore();
        arena.close();
    }
}
