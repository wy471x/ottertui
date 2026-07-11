package com.ottertui.widgets;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * Terminal image protocol encoder — Kitty, iTerm2, Sixel.
 *
 * <p>Zero external dependencies beyond java.desktop (BufferedImage).
 * Auto-detects the best protocol from environment variables.</p>
 */
public final class TerminalImage {

    private TerminalImage() {}

    /** Supported image protocols. */
    public enum Protocol {
        KITTY,   // Kitty Graphics Protocol (APC escape)
        ITERM2,  // iTerm2 Inline Images (OSC 1337)
        SIXEL    // Sixel Graphics (DCS escape)
    }

    /** Auto-detect best protocol from environment. */
    public static Protocol detect() {
        return detect(System.getenv());
    }

    /** Package-private for testing — detect from a controlled env map. */
    static Protocol detect(Map<String, String> env) {
        String termProgram = env.get("TERM_PROGRAM");
        if (termProgram != null) {
            String lower = termProgram.toLowerCase(Locale.ROOT);
            if (lower.contains("iterm") || lower.contains("apple_terminal")) {
                return Protocol.ITERM2;
            }
        }

        if (env.get("KITTY_WINDOW_ID") != null) return Protocol.KITTY;
        if (env.get("ITERM_SESSION_ID") != null) return Protocol.ITERM2;

        String term = env.get("TERM");
        if (term != null) {
            if (term.contains("kitty") || term.contains("ghostty")) return Protocol.KITTY;
        }

        // Fallback: Sixel has broadest legacy support
        return Protocol.SIXEL;
    }

    // ==================== Kitty Graphics Protocol ====================

    /**
     * Encode a BufferedImage for Kitty Graphics Protocol.
     * @return APC escape sequence string
     */
    public static String kitty(BufferedImage image, int displayWidth, int displayHeight)
            throws IOException {
        byte[] png = toPngBytes(image);
        return kitty(png, displayWidth, displayHeight);
    }

    /**
     * Encode raw PNG bytes for Kitty Graphics Protocol.
     * Uses chunked transfer for payloads larger than 4096 base64 chars.
     */
    public static String kitty(byte[] pngData, int displayWidth, int displayHeight) {
        String b64 = Base64.getEncoder().encodeToString(pngData);
        String header = "\033_Gf=24,s=" + displayWidth
            + ",v=" + displayHeight + ",a=T";
        return emitKittyChunks(header, b64);
    }

    /** Emit Kitty APC chunks — exposed for testing large-image transfer. */
    static String emitKittyChunks(String header, String b64) {
        StringBuilder sb = new StringBuilder();
        sb.append(header);

        int maxChunk = 4096;
        if (b64.length() <= maxChunk) {
            sb.append(';').append(b64).append("\033\\");
        } else {
            int offset = 0;
            boolean first = true;
            while (offset < b64.length()) {
                int end = Math.min(offset + maxChunk, b64.length());
                if (first) {
                    sb.append(",m=1;");
                    first = false;
                } else if (end == b64.length()) {
                    sb.append("\033_Gm=0;");
                } else {
                    sb.append("\033_Gm=1;");
                }
                sb.append(b64, offset, end).append("\033\\");
                offset = end;
            }
        }
        return sb.toString();
    }

    // ==================== iTerm2 Inline Images ====================

    /**
     * Encode a BufferedImage for iTerm2 inline image protocol.
     */
    public static String iterm2(BufferedImage image, int displayWidth, int displayHeight)
            throws IOException {
        return iterm2(toPngBytes(image), displayWidth, displayHeight);
    }

    /**
     * Encode raw image bytes for iTerm2 inline image protocol.
     */
    public static String iterm2(byte[] imageData, int displayWidth, int displayHeight) {
        String b64 = Base64.getEncoder().encodeToString(imageData);
        return "\033]1337;File=inline=1;size=" + imageData.length
            + ";width=" + displayWidth + "px"
            + ";height=" + displayHeight + "px"
            + ":" + b64 + "\007";
    }

    // ==================== Sixel Graphics ====================

    /**
     * Encode a BufferedImage for Sixel graphics.
     */
    public static String sixel(BufferedImage image, int displayWidth, int displayHeight) {
        // Scale image to target cell dimensions
        // Each sixel character = 6 pixels tall. Width is in pixels.
        BufferedImage scaled = scaleImage(image, displayWidth, displayHeight * 6);
        return SixelEncoder.encode(scaled, displayWidth, displayHeight);
    }

    // ==================== helpers ====================

    private static byte[] toPngBytes(BufferedImage image) throws IOException {
        var baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }

    // Exposed for testing
    static BufferedImage scaleImage(BufferedImage src, int targetWidth, int targetHeight) {
        if (src.getWidth() == targetWidth && src.getHeight() == targetHeight) return src;
        BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        return scaled;
    }
}
