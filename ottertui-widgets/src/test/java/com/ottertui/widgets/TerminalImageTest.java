package com.ottertui.widgets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.*;

class TerminalImageTest {

    private BufferedImage createTestImage() {
        BufferedImage img = new BufferedImage(32, 24, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 24; y++) {
            for (int x = 0; x < 32; x++) {
                img.setRGB(x, y, (x * 8) << 16 | (y * 10) << 8 | 128);
            }
        }
        return img;
    }

    // ── Protocol detection ──

    @Test
    @DisplayName("detect returns a valid protocol")
    void detectReturnsProtocol() {
        TerminalImage.Protocol p = TerminalImage.detect();
        assertNotNull(p);
        assertTrue(p == TerminalImage.Protocol.KITTY
            || p == TerminalImage.Protocol.ITERM2
            || p == TerminalImage.Protocol.SIXEL);
    }

    @Test
    @DisplayName("detect with KITTY_WINDOW_ID env var")
    void detectKittyWindowId() {
        Map<String, String> env = new HashMap<>();
        env.put("KITTY_WINDOW_ID", "test123");
        assertEquals(TerminalImage.Protocol.KITTY, TerminalImage.detect(env));
    }

    @Test
    @DisplayName("detect with ITERM_SESSION_ID env var")
    void detectItermSessionId() {
        Map<String, String> env = new HashMap<>();
        env.put("ITERM_SESSION_ID", "session123");
        assertEquals(TerminalImage.Protocol.ITERM2, TerminalImage.detect(env));
    }

    @Test
    @DisplayName("detect with TERM_PROGRAM=iterm2")
    void detectTermProgramIterm() {
        Map<String, String> env = new HashMap<>();
        env.put("TERM_PROGRAM", "iTerm.app");
        assertEquals(TerminalImage.Protocol.ITERM2, TerminalImage.detect(env));
    }

    @Test
    @DisplayName("detect with TERM containing kitty")
    void detectTermKitty() {
        Map<String, String> env = new HashMap<>();
        env.put("TERM", "xterm-kitty");
        assertEquals(TerminalImage.Protocol.KITTY, TerminalImage.detect(env));
    }

    @Test
    @DisplayName("detect with TERM_PROGRAM=Apple_Terminal")
    void detectTermProgramApple() {
        Map<String, String> env = new HashMap<>();
        env.put("TERM_PROGRAM", "Apple_Terminal");
        assertEquals(TerminalImage.Protocol.ITERM2, TerminalImage.detect(env));
    }

    @Test
    @DisplayName("detect with TERM containing ghostty")
    void detectTermGhostty() {
        Map<String, String> env = new HashMap<>();
        env.put("TERM", "xterm-ghostty");
        assertEquals(TerminalImage.Protocol.KITTY, TerminalImage.detect(env));
    }

    @Test
    @DisplayName("detect returns SIXEL when no known env vars set")
    void detectReturnsSixelForUnknownEnv() {
        Map<String, String> env = new HashMap<>();
        assertEquals(TerminalImage.Protocol.SIXEL, TerminalImage.detect(env));
    }

    @Test
    @DisplayName("detect prioritizes TERM_PROGRAM over TERM")
    void detectPrioritizesTermProgram() {
        Map<String, String> env = new HashMap<>();
        env.put("TERM_PROGRAM", "iTerm.app");
        env.put("TERM", "xterm-kitty");
        assertEquals(TerminalImage.Protocol.ITERM2, TerminalImage.detect(env));
    }

    // ── Kitty ──

    @Test
    @DisplayName("kitty encoding produces valid APC sequence")
    void kittyEncoding() throws Exception {
        BufferedImage img = createTestImage();
        String seq = TerminalImage.kitty(img, 10, 8);
        assertNotNull(seq);
        assertTrue(seq.startsWith("\033_G"), "Should start with APC");
        assertTrue(seq.endsWith("\033\\"), "Should end with ST");
        assertTrue(seq.contains("f=24"), "Should contain PNG format marker");
    }

    @Test
    @DisplayName("kitty encoding from raw bytes")
    void kittyEncodingRawBytes() throws Exception {
        BufferedImage img = createTestImage();
        var baos = new ByteArrayOutputStream();
        ImageIO.write(img, "PNG", baos);
        byte[] data = baos.toByteArray();

        String seq = TerminalImage.kitty(data, 10, 8);
        assertNotNull(seq);
        assertTrue(seq.startsWith("\033_G"));
        assertTrue(seq.endsWith("\033\\"));
    }

    @Test
    @DisplayName("emitKittyChunks handles small payload (no chunking)")
    void emitKittyChunksSmall() {
        String b64 = "YWJjZGVm"; // "abcdef" in base64
        String seq = TerminalImage.emitKittyChunks(
            "\033_Gf=24,s=3,v=2,a=T", b64);
        assertTrue(seq.startsWith("\033_G"));
        assertTrue(seq.endsWith("\033\\"));
        assertTrue(seq.contains(";YWJjZGVm"));
        // Single chunk — no m=1 markers
        assertFalse(seq.contains("m=1"));
    }

    @Test
    @DisplayName("emitKittyChunks splits large payload into chunks")
    void emitKittyChunksLarge() {
        // Build a base64 string > 4096 chars
        StringBuilder large = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            large.append("YWJjZGVmZ2hpamtsbW5vcA=="); // ~24 chars each → ~12000 chars
        }
        String b64 = large.toString();
        assertTrue(b64.length() > 4096, "Payload should exceed 4096 chars");

        String seq = TerminalImage.emitKittyChunks(
            "\033_Gf=24,s=10,v=8,a=T", b64);
        assertNotNull(seq);
        assertTrue(seq.startsWith("\033_G"));
        assertTrue(seq.endsWith("\033\\"));
        // Should contain chunk markers
        assertTrue(seq.contains("m=1"), "Should have intermediate chunk marker m=1");
        assertTrue(seq.contains("m=0"), "Should have final chunk marker m=0");
    }

    @Test
    @DisplayName("emitKittyChunks with exact chunk boundary")
    void emitKittyChunksExactBoundary() {
        // Build exactly 4100 chars to cross the 4096 boundary
        StringBuilder sb = new StringBuilder();
        while (sb.length() < 4100) {
            sb.append('A');
        }
        String b64 = sb.toString();

        String seq = TerminalImage.emitKittyChunks(
            "\033_Gf=24,s=1,v=1,a=T", b64);
        assertNotNull(seq);
        assertTrue(seq.contains("m=1"));
        assertTrue(seq.contains("m=0"));
    }

    @Test
    @DisplayName("kitty encoding triggers chunked path for large image")
    void kittyEncodingLargeImageTriggersChunking() throws Exception {
        // Create a busy image that produces a large PNG
        BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 200; y++) {
            for (int x = 0; x < 200; x++) {
                img.setRGB(x, y, (x * y) | 0xFF000000);
            }
        }
        String seq = TerminalImage.kitty(img, 60, 60);
        assertNotNull(seq);
        assertTrue(seq.startsWith("\033_G"));
        assertTrue(seq.endsWith("\033\\"));
    }

    // ── iTerm2 ──

    @Test
    @DisplayName("iterm2 encoding produces valid OSC sequence")
    void iterm2Encoding() throws Exception {
        BufferedImage img = createTestImage();
        String seq = TerminalImage.iterm2(img, 10, 8);
        assertNotNull(seq);
        assertTrue(seq.startsWith("\033]1337;File="), "Should be OSC 1337");
        assertTrue(seq.contains("inline=1"), "Should be inline");
        assertTrue(seq.endsWith("\007"), "Should end with BEL");
    }

    @Test
    @DisplayName("iterm2 encoding from raw bytes")
    void iterm2EncodingRawBytes() throws Exception {
        BufferedImage img = createTestImage();
        var baos = new ByteArrayOutputStream();
        ImageIO.write(img, "PNG", baos);
        byte[] data = baos.toByteArray();

        String seq = TerminalImage.iterm2(data, 20, 15);
        assertNotNull(seq);
        assertTrue(seq.contains("size="));
        assertTrue(seq.contains("width=20px"));
        assertTrue(seq.contains("height=15px"));
    }

    @Test
    @DisplayName("iterm2 from BufferedImage delegates to raw bytes path")
    void iterm2FromBufferedImage() throws Exception {
        BufferedImage img = createTestImage();
        String seq = TerminalImage.iterm2(img, 5, 3);
        assertNotNull(seq);
        assertTrue(seq.startsWith("\033]1337;File="));
    }

    // ── Sixel ──

    @Test
    @DisplayName("sixel encoding produces valid DCS sequence")
    void sixelEncoding() {
        BufferedImage img = createTestImage();
        String seq = TerminalImage.sixel(img, 10, 8);
        assertNotNull(seq);
        assertTrue(seq.startsWith("\033Pq"), "Should start with DCS Sixel");
        assertTrue(seq.endsWith("\033\\"), "Should end with ST");
    }

    @Test
    @DisplayName("sixel encoding of solid color produces minimal sequence")
    void sixelSolidColor() {
        BufferedImage img = new BufferedImage(12, 6, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 12; x++) {
                img.setRGB(x, y, 0xFF0000); // solid red
            }
        }
        String seq = TerminalImage.sixel(img, 12, 1);
        assertNotNull(seq);
        assertFalse(seq.isEmpty());
        assertTrue(seq.length() > 10);
    }

    @Test
    @DisplayName("sixel encoding handles small image")
    void sixelSmallImage() {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        img.setRGB(0, 0, 0x00FF00);
        String seq = TerminalImage.sixel(img, 1, 1);
        assertNotNull(seq);
        assertTrue(seq.startsWith("\033Pq"));
    }

    // ── Scale ──

    @Test
    @DisplayName("scaleImage preserves aspect-like resize")
    void scaleImageResize() {
        BufferedImage src = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        BufferedImage scaled = TerminalImage.scaleImage(src, 20, 60);
        assertEquals(20, scaled.getWidth());
        assertEquals(60, scaled.getHeight());
    }

    @Test
    @DisplayName("scaleImage with same size returns original")
    void scaleImageSameSize() {
        BufferedImage src = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        BufferedImage scaled = TerminalImage.scaleImage(src, 10, 10);
        assertSame(src, scaled);
    }

    // ── Protocol enum ──

    @Test
    @DisplayName("protocol enum has all three values")
    void protocolEnumValues() {
        assertEquals(3, TerminalImage.Protocol.values().length);
        assertNotNull(TerminalImage.Protocol.valueOf("KITTY"));
        assertNotNull(TerminalImage.Protocol.valueOf("ITERM2"));
        assertNotNull(TerminalImage.Protocol.valueOf("SIXEL"));
    }
}
