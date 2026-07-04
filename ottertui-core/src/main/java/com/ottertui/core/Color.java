package com.ottertui.core;

public sealed interface Color {
    record Reset() implements Color {}
    record Indexed(int index) implements Color {}
    record Rgb(int r, int g, int b) implements Color {}

    Color RESET = new Reset();
    Color BLACK = new Rgb(0, 0, 0);
    Color RED = new Rgb(255, 0, 0);
    Color GREEN = new Rgb(0, 255, 0);
    Color YELLOW = new Rgb(255, 255, 0);
    Color BLUE = new Rgb(0, 0, 255);
    Color MAGENTA = new Rgb(255, 0, 255);
    Color CYAN = new Rgb(0, 255, 255);
    Color WHITE = new Rgb(255, 255, 255);
    Color GRAY = new Rgb(128, 128, 128);
    Color DARK_GRAY = new Rgb(64, 64, 64);
    Color LIGHT_RED = new Rgb(255, 128, 128);
    Color LIGHT_GREEN = new Rgb(128, 255, 128);
    Color LIGHT_YELLOW = new Rgb(255, 255, 128);
    Color LIGHT_BLUE = new Rgb(128, 128, 255);
    Color LIGHT_MAGENTA = new Rgb(255, 128, 255);
    Color LIGHT_CYAN = new Rgb(128, 255, 255);
}
