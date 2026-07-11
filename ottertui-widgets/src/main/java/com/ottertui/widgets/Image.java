package com.ottertui.widgets;

import com.ottertui.core.Buffer;
import com.ottertui.core.Rect;
import com.ottertui.core.Style;
import com.ottertui.core.Widget;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import javax.imageio.ImageIO;

/**
 * Renders an image in the terminal using Kitty, iTerm2, or Sixel protocol.
 *
 * <p>Loads from BufferedImage, file path, classpath resource, or raw bytes.
 * Auto-detects the best protocol, or use a specific one.</p>
 *
 * <pre>{@code
 * Image img = Image.fromResource("/logo.png", 40, 20);
 * Image img = Image.fromFile("photo.jpg", 30, 15, Protocol.KITTY);
 * }</pre>
 */
public class Image implements Widget {

    private final byte[] imageData;
    private final int cellWidth;
    private final int cellHeight;
    private final TerminalImage.Protocol protocol;
    private final String cachedSeq;

    private Image(byte[] imageData, int cellWidth, int cellHeight,
                        TerminalImage.Protocol protocol) {
        this.imageData = Objects.requireNonNull(imageData, "imageData");
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.protocol = protocol != null ? protocol : TerminalImage.detect();
        this.cachedSeq = preComputeSeq(this.protocol);
    }

    private String preComputeSeq(TerminalImage.Protocol protocol) {
        return switch (protocol) {
            case KITTY -> {
                try {
                    yield TerminalImage.kitty(imageData, cellWidth, cellHeight);
                } catch (Exception e) {
                    yield "";
                }
            }
            case ITERM2 -> {
                try {
                    yield TerminalImage.iterm2(imageData, cellWidth, cellHeight);
                } catch (Exception e) {
                    yield "";
                }
            }
            case SIXEL -> {
                try {
                    BufferedImage img = ImageIO.read(
                        new java.io.ByteArrayInputStream(imageData));
                    yield img != null
                        ? TerminalImage.sixel(img, cellWidth, cellHeight)
                        : "";
                } catch (IOException e) {
                    yield "";
                }
            }
        };
    }

    // --- factory methods ---

    /** Create from a BufferedImage. */
    public static Image fromImage(BufferedImage image, int cellWidth, int cellHeight) {
        return fromImage(image, cellWidth, cellHeight, null);
    }

    /** Create from a BufferedImage with explicit protocol. */
    public static Image fromImage(BufferedImage image, int cellWidth, int cellHeight,
                                        TerminalImage.Protocol protocol) {
        try {
            return new Image(toBytes(image), cellWidth, cellHeight, protocol);
        } catch (IOException e) {
            throw new RuntimeException("Failed to encode image", e);
        }
    }

    /** Create from a file path. */
    public static Image fromFile(String path, int cellWidth, int cellHeight)
            throws IOException {
        return fromFile(path, cellWidth, cellHeight, null);
    }

    /** Create from a file path with explicit protocol. */
    public static Image fromFile(String path, int cellWidth, int cellHeight,
                                       TerminalImage.Protocol protocol) throws IOException {
        BufferedImage img = ImageIO.read(new java.io.File(path));
        if (img == null) throw new IOException("Unsupported image format: " + path);
        return new Image(toBytes(img), cellWidth, cellHeight, protocol);
    }

    /** Create from a classpath resource. */
    public static Image fromResource(String resourcePath, int cellWidth, int cellHeight)
            throws IOException {
        return fromResource(resourcePath, cellWidth, cellHeight, null);
    }

    /** Create from a classpath resource with explicit protocol. */
    public static Image fromResource(String resourcePath, int cellWidth, int cellHeight,
                                           TerminalImage.Protocol protocol) throws IOException {
        try (InputStream is = Image.class.getResourceAsStream(resourcePath)) {
            if (is == null) throw new IOException("Resource not found: " + resourcePath);
            BufferedImage img = ImageIO.read(is);
            if (img == null) throw new IOException("Unsupported image format: " + resourcePath);
            return new Image(toBytes(img), cellWidth, cellHeight, protocol);
        }
    }

    /** Create from raw bytes (PNG/JPEG data). */
    public static Image fromBytes(byte[] data, int cellWidth, int cellHeight) {
        return fromBytes(data, cellWidth, cellHeight, null);
    }

    /** Create from raw bytes with explicit protocol. */
    public static Image fromBytes(byte[] data, int cellWidth, int cellHeight,
                                        TerminalImage.Protocol protocol) {
        return new Image(data.clone(), cellWidth, cellHeight, protocol);
    }

    @Override
    public void render(Rect area, Buffer buffer) {
        if (cachedSeq.isEmpty()) return;
        buffer.addRawWrite(area.x(), area.y(), cachedSeq);
    }

    /** Return the protocol being used. */
    public TerminalImage.Protocol protocol() {
        return protocol;
    }

    /** Return the display width in cells. */
    public int cellWidth() {
        return cellWidth;
    }

    /** Return the display height in cells. */
    public int cellHeight() {
        return cellHeight;
    }

    private static byte[] toBytes(BufferedImage image) throws IOException {
        var baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }
}
