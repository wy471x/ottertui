package com.ottertui.tui;

import com.ottertui.core.TerminalBackend;

import java.io.IOException;

public class BackendSelector {

    public static TerminalBackend create() {
        String backend = System.getProperty("ottertui.backend");
        if ("lanterna".equals(backend)) {
            try {
                return createLanterna();
            } catch (Exception e) {
                throw new RuntimeException("Lanterna backend requested but unavailable", e);
            }
        }
        if ("jline".equals(backend) || backend == null) {
            try {
                return createJLine();
            } catch (Exception e) {
                // fall through to lanterna
            }
        }

        try {
            return createLanterna();
        } catch (Exception e) {
            throw new IllegalStateException("No suitable terminal backend found", e);
        }
    }

    private static TerminalBackend createJLine() throws Exception {
        Class<?> clazz = Class.forName("com.ottertui.backend.jline.JLineBackend");
        return (TerminalBackend) clazz.getConstructor().newInstance();
    }

    private static TerminalBackend createLanterna() throws Exception {
        Class<?> clazz = Class.forName("com.ottertui.backend.lanterna.LanternaBackend");
        return (TerminalBackend) clazz.getConstructor().newInstance();
    }
}
