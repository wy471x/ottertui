package com.ottertui.widgets;

import com.ottertui.core.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChartTest {

    private Chart.Dataset makeDataset(double[][] data) {
        var points = new java.util.ArrayList<Chart.Point>();
        for (double[] d : data) {
            points.add(new Chart.Point(d[0], d[1]));
        }
        return new Chart.Dataset("test", points,
            new Style(Color.CYAN, Color.RESET, java.util.Set.of()));
    }

    @Test
    @DisplayName("default constructor with auto-scale")
    void defaultConstructor() {
        var ds = makeDataset(new double[][]{{0, 0}, {5, 5}});
        var c = new Chart(List.of(ds));
        assertNotNull(c);
    }

    @Test
    @DisplayName("constructor with explicit bounds")
    void explicitBounds() {
        var ds = makeDataset(new double[][]{{0, 0}, {10, 10}});
        var c = new Chart(List.of(ds), 0, 10, 0, 10);
        assertNotNull(c);
    }

    @Test
    @DisplayName("constructor with explicit bounds and axis style")
    void explicitBoundsWithStyle() {
        var ds = makeDataset(new double[][]{{0, 0}, {10, 10}});
        var c = new Chart(List.of(ds), 0, 10, 0, 10, Style.DEFAULT);
        assertNotNull(c);
    }

    @Test
    @DisplayName("render with empty datasets does nothing")
    void emptyDatasets() {
        var c = new Chart(List.of());
        Buffer b = new Buffer(20, 10);
        c.render(new Rect(0, 0, 20, 10), b);
        assertEquals(Cell.EMPTY, b.getCell(0, 0));
    }

    @Test
    @DisplayName("render single point draws braille dot")
    void renderSinglePoint() {
        var ds = makeDataset(new double[][]{{1, 1}});
        var c = new Chart(List.of(ds), 0, 2, 0, 2);
        Buffer b = new Buffer(20, 10);
        c.render(new Rect(0, 0, 20, 10), b);
        boolean foundDot = false;
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 20; x++) {
                if (b.getCell(x, y).ch() != ' ') {
                    foundDot = true;
                    break;
                }
            }
        }
        assertTrue(foundDot);
    }

    @Test
    @DisplayName("render diagonal line")
    void renderDiagonalLine() {
        var ds = makeDataset(new double[][]{{0, 0}, {10, 10}});
        var c = new Chart(List.of(ds), 0, 10, 0, 10);
        Buffer b = new Buffer(40, 20);
        c.render(new Rect(0, 0, 40, 20), b);
        int dotCount = 0;
        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 40; x++) {
                if (b.getCell(x, y).ch() != ' ') {
                    dotCount++;
                }
            }
        }
        assertTrue(dotCount > 0);
    }

    @Test
    @DisplayName("render multiple datasets")
    void renderMultipleDatasets() {
        var ds1 = makeDataset(new double[][]{{0, 0}, {5, 5}});
        var ds2 = new Chart.Dataset("test2",
            List.of(new Chart.Point(0, 10), new Chart.Point(10, 0)),
            new Style(Color.RED, Color.RESET, java.util.Set.of()));
        var c = new Chart(List.of(ds1, ds2));
        Buffer b = new Buffer(40, 20);
        c.render(new Rect(0, 0, 40, 20), b);
        boolean foundDot = false;
        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 40; x++) {
                if (b.getCell(x, y).ch() != ' ') {
                    foundDot = true;
                    break;
                }
            }
        }
        assertTrue(foundDot);
    }

    @Test
    @DisplayName("render with tiny area does nothing")
    void tinyArea() {
        var ds = makeDataset(new double[][]{{0, 0}});
        var c = new Chart(List.of(ds));
        Buffer b = new Buffer(1, 1);
        c.render(new Rect(0, 0, 1, 1), b);
        assertEquals(Cell.EMPTY, b.getCell(0, 0));
    }

    @Test
    @DisplayName("auto-scale with all same x values pads range")
    void allSameX() {
        var ds = makeDataset(new double[][]{{5, 0}, {5, 5}, {5, 10}});
        var c = new Chart(List.of(ds));
        Buffer b = new Buffer(40, 20);
        assertDoesNotThrow(() -> c.render(new Rect(0, 0, 40, 20), b));
    }

    @Test
    @DisplayName("auto-scale with all same y values pads range")
    void allSameY() {
        var ds = makeDataset(new double[][]{{0, 3}, {5, 3}, {10, 3}});
        var c = new Chart(List.of(ds));
        Buffer b = new Buffer(40, 20);
        assertDoesNotThrow(() -> c.render(new Rect(0, 0, 40, 20), b));
    }

    @Test
    @DisplayName("line with x1 > x2 draws right-to-left")
    void rightToLeftLine() {
        var ds = makeDataset(new double[][]{{10, 10}, {0, 0}});
        var c = new Chart(List.of(ds), 0, 10, 0, 10);
        Buffer b = new Buffer(40, 20);
        assertDoesNotThrow(() -> c.render(new Rect(0, 0, 40, 20), b));
    }

    @Test
    @DisplayName("dataset with empty points list skipped")
    void emptyPointsInDataset() {
        var ds1 = makeDataset(new double[][]{{0, 0}, {10, 10}});
        var ds2 = new Chart.Dataset("empty",
            List.of(),
            new Style(Color.RED, Color.RESET, java.util.Set.of()));
        var c = new Chart(List.of(ds1, ds2));
        Buffer b = new Buffer(40, 20);
        assertDoesNotThrow(() -> c.render(new Rect(0, 0, 40, 20), b));
    }

    @Test
    @DisplayName("auto-scale with all identical points")
    void allIdenticalPoints() {
        var ds = makeDataset(new double[][]{{5, 5}, {5, 5}, {5, 5}});
        var c = new Chart(List.of(ds));
        Buffer b = new Buffer(40, 20);
        assertDoesNotThrow(() -> c.render(new Rect(0, 0, 40, 20), b));
    }
}
