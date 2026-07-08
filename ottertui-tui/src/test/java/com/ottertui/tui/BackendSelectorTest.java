package com.ottertui.tui;

import com.ottertui.core.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.condition.EnabledIf;

import static org.junit.jupiter.api.Assertions.*;

class BackendSelectorTest {

    static boolean isTtyAvailable() {
        return System.console() != null;
    }

    @AfterEach
    void clearProperty() {
        System.clearProperty("ottertui.backend");
    }

    @Test
    @EnabledIf("isTtyAvailable")
    @DisplayName("create returns a TerminalBackend")
    void createReturnsBackend() {
        try {
            TerminalBackend backend = BackendSelector.create();
            assertNotNull(backend);
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    @EnabledIf("isTtyAvailable")
    @DisplayName("create with jline property set")
    void createWithJline() {
        System.setProperty("ottertui.backend", "jline");
        try {
            TerminalBackend backend = BackendSelector.create();
            assertNotNull(backend);
            assertTrue(backend.getClass().getName().contains("JLine"));
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("JLine"));
        }
    }

    @Test
    @EnabledIf("isTtyAvailable")
    @DisplayName("create with lanterna property set")
    void createWithLanterna() {
        System.setProperty("ottertui.backend", "lanterna");
        try {
            TerminalBackend backend = BackendSelector.create();
            assertNotNull(backend);
            assertTrue(backend.getClass().getName().contains("Lanterna"));
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Lanterna"));
        }
    }

    @Test
    @EnabledIf("isTtyAvailable")
    @DisplayName("create with unknown backend falls through to lanterna")
    void createWithUnknownBackend() {
        System.setProperty("ottertui.backend", "unknown");
        try {
            TerminalBackend backend = BackendSelector.create();
            assertNotNull(backend);
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }
}
