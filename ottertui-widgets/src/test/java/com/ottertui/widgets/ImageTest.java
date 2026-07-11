package com.ottertui.widgets;

import com.ottertui.core.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.*;

class ImageTest {

    private BufferedImage testImage() {
        BufferedImage img = new BufferedImage(16, 12, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 12; y++) {
            for (int x = 0; x < 16; x++) {
                img.setRGB(x, y, (x * 16) << 16 | (y * 20) << 8);
            }
        }
        return img;
    }

    @Test
    @DisplayName("fromImage creates widget with default protocol")
    void fromImageDefault() {
        Image w = Image.fromImage(testImage(), 10, 8);
        assertNotNull(w);
        assertEquals(10, w.cellWidth());
        assertEquals(8, w.cellHeight());
    }

    @Test
    @DisplayName("fromImage with explicit protocol")
    void fromImageExplicitProtocol() {
        Image w = Image.fromImage(testImage(), 20, 15,
            TerminalImage.Protocol.KITTY);
        assertEquals(TerminalImage.Protocol.KITTY, w.protocol());
    }

    @Test
    @DisplayName("fromBytes creates widget")
    void fromBytes() throws Exception {
        var baos = new ByteArrayOutputStream();
        ImageIO.write(testImage(), "PNG", baos);
        byte[] data = baos.toByteArray();

        Image w = Image.fromBytes(data, 15, 10,
            TerminalImage.Protocol.ITERM2);
        assertNotNull(w);
        assertEquals(TerminalImage.Protocol.ITERM2, w.protocol());
    }

    @Test
    @DisplayName("fromBytes without protocol auto-detects")
    void fromBytesAutoDetect() throws Exception {
        var baos = new ByteArrayOutputStream();
        ImageIO.write(testImage(), "PNG", baos);
        byte[] data = baos.toByteArray();

        Image w = Image.fromBytes(data, 10, 10);
        assertNotNull(w);
        assertNotNull(w.protocol());
    }

    @Test
    @DisplayName("fromFile loads from disk")
    void fromFile(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("test.png");
        ImageIO.write(testImage(), "PNG", file.toFile());

        Image w = Image.fromFile(file.toString(), 10, 10);
        assertNotNull(w);
        assertEquals(10, w.cellWidth());
    }

    @Test
    @DisplayName("fromFile with explicit protocol")
    void fromFileWithProtocol(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("test.png");
        ImageIO.write(testImage(), "PNG", file.toFile());

        Image w = Image.fromFile(file.toString(), 8, 6,
            TerminalImage.Protocol.SIXEL);
        assertEquals(TerminalImage.Protocol.SIXEL, w.protocol());
    }

    @Test
    @DisplayName("fromResource loads from classpath")
    void fromResource() throws Exception {
        Image w = Image.fromResource("/test-image.png", 1, 1,
            TerminalImage.Protocol.KITTY);
        assertNotNull(w);
        assertEquals(1, w.cellWidth());
        assertEquals(1, w.cellHeight());
    }

    @Test
    @DisplayName("fromResource without protocol auto-detects")
    void fromResourceAutoDetect() throws Exception {
        Image w = Image.fromResource("/test-image.png", 1, 1);
        assertNotNull(w);
        assertNotNull(w.protocol());
    }

    @Test
    @DisplayName("fromResource throws on non-existent resource")
    void fromResourceThrows() {
        assertThrows(IOException.class, () ->
            Image.fromResource("/nonexistent.png", 10, 10));
    }

    @Test
    @DisplayName("fromFile throws on non-existent file")
    void fromFileThrows() {
        assertThrows(Exception.class, () ->
            Image.fromFile("/nonexistent/path.png", 10, 10));
    }

    @Test
    @DisplayName("fromFile with non-image file throws")
    void fromFileNonImage(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("not-image.txt");
        Files.writeString(file, "hello world");
        assertThrows(IOException.class, () ->
            Image.fromFile(file.toString(), 10, 10));
    }

    // ── Render ──

    @Test
    @DisplayName("render with Kitty protocol produces raw write")
    void renderKitty() {
        Image w = Image.fromImage(testImage(), 10, 8,
            TerminalImage.Protocol.KITTY);
        Buffer b = new Buffer(80, 24);
        w.render(new Rect(0, 0, 80, 24), b);
        assertFalse(b.rawContent().isEmpty());
        assertTrue(b.rawContent().get(0).text().startsWith("\033_"));
    }

    @Test
    @DisplayName("render with iTerm2 protocol produces raw write")
    void renderITerm2() {
        Image w = Image.fromImage(testImage(), 10, 8,
            TerminalImage.Protocol.ITERM2);
        Buffer b = new Buffer(80, 24);
        w.render(new Rect(0, 0, 80, 24), b);
        assertFalse(b.rawContent().isEmpty());
        assertTrue(b.rawContent().get(0).text().startsWith("\033]"));
    }

    @Test
    @DisplayName("render with Sixel protocol produces raw write")
    void renderSixel() {
        Image w = Image.fromImage(testImage(), 10, 8,
            TerminalImage.Protocol.SIXEL);
        Buffer b = new Buffer(80, 24);
        w.render(new Rect(0, 0, 80, 24), b);
        assertFalse(b.rawContent().isEmpty());
        assertTrue(b.rawContent().get(0).text().startsWith("\033P"));
    }

    @Test
    @DisplayName("render at offset position")
    void renderAtOffset() {
        Image w = Image.fromImage(testImage(), 5, 4,
            TerminalImage.Protocol.KITTY);
        Buffer b = new Buffer(80, 24);
        w.render(new Rect(10, 5, 20, 10), b);
        assertFalse(b.rawContent().isEmpty());
        assertEquals(10, b.rawContent().get(0).x());
        assertEquals(5, b.rawContent().get(0).y());
    }

    @Test
    @DisplayName("render with auto-detected protocol")
    void renderAutoDetect() {
        Image w = Image.fromImage(testImage(), 6, 4);
        Buffer b = new Buffer(80, 24);
        assertDoesNotThrow(() -> w.render(new Rect(0, 0, 80, 24), b));
    }

    @Test
    @DisplayName("render with iTerm2 protocol from bytes")
    void renderITerm2FromBytes() throws Exception {
        var baos = new ByteArrayOutputStream();
        ImageIO.write(testImage(), "PNG", baos);
        Image w = Image.fromBytes(baos.toByteArray(), 10, 8,
            TerminalImage.Protocol.ITERM2);
        Buffer b = new Buffer(80, 24);
        w.render(new Rect(0, 0, 80, 24), b);
        assertFalse(b.rawContent().isEmpty());
        assertTrue(b.rawContent().get(0).text().startsWith("\033]"));
    }

    @Test
    @DisplayName("render with corrupt data for sixel returns early")
    void renderCorruptSixelData() {
        // Sixel needs valid image bytes; corrupt bytes cause render to return early
        byte[] corrupt = new byte[]{0x00, 0x01, 0x02};
        Image w = Image.fromBytes(corrupt, 10, 8,
            TerminalImage.Protocol.SIXEL);
        Buffer b = new Buffer(80, 24);
        // Should not throw — render returns early when ImageIO.read fails
        assertDoesNotThrow(() -> w.render(new Rect(0, 0, 80, 24), b));
    }

    @Test
    @DisplayName("cellWidth and cellHeight reflect display size")
    void displayDimensions() {
        Image w = Image.fromImage(testImage(), 30, 20,
            TerminalImage.Protocol.KITTY);
        assertEquals(30, w.cellWidth());
        assertEquals(20, w.cellHeight());
    }
}
