package com.ottertui.toolkit;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Recursive descent parser for .tcss (Terminal CSS) files.
 *
 * Grammar:
 *   stylesheet  = (rule | variableBlock | comment)*
 *   variableBlock = ':root' '{' (variable ';')* '}'
 *   variable    = '--' ident ':' value
 *   rule        = selector (',' selector)* '{' (declaration ';')* '}'
 *   selector    = type_sel | '#' id_sel | '.' class_sel | ':' pseudo_sel
 *                 | compound_sel
 *   compound_sel = simple_sel+
 *   simple_sel  = type_sel | '#' id_sel | '.' class_sel | ':' pseudo_sel
 *   declaration = property ':' value
 *   property    = ident
 *   value       = ident | hex_color | number | var_ref
 *   var_ref     = 'var(' '--' ident ')'
 *   hex_color   = '#' hex{3} | '#' hex{6}
 */
final class TcssParser {
    private final String input;
    private int pos;

    private TcssParser(String input) {
        this.input = input;
        this.pos = 0;
    }

    static StyleSheet parse(String input) {
        return new TcssParser(input).parseStylesheet();
    }

    private StyleSheet parseStylesheet() {
        var sheet = new StyleSheet();
        while (pos < input.length()) {
            skipWhitespaceAndComments();
            if (pos >= input.length()) break;

            if (peek(":root")) {
                parseVariableBlock(sheet);
            } else {
                parseRule(sheet);
            }
        }
        return sheet;
    }

    private void parseVariableBlock(StyleSheet sheet) {
        expect(":root");
        skipWhitespaceAndComments();
        expectChar('{');
        skipWhitespaceAndComments();

        while (pos < input.length() && peekChar() != '}') {
            skipWhitespaceAndComments();
            if (peekChar() == '}') break;
            parseVariable(sheet);
            skipWhitespaceAndComments();
        }
        expectChar('}');
    }

    private void parseVariable(StyleSheet sheet) {
        skipWhitespaceAndComments();
        if (peekChar() == '}') return;

        // Variable name: --varname
        if (peek("--")) {
            pos += 2;
        }
        String name = readIdent();
        skipWhitespaceAndComments();
        expectChar(':');
        skipWhitespaceAndComments();
        String value = readValue();
        expectSemicolon();

        sheet.setVariable("--" + name, value);
    }

    private void parseRule(StyleSheet sheet) {
        List<Selector> selectors = parseSelectorList();
        skipWhitespaceAndComments();
        expectChar('{');
        skipWhitespaceAndComments();

        Map<String, String> declarations = new LinkedHashMap<>();
        while (pos < input.length() && peekChar() != '}') {
            skipWhitespaceAndComments();
            if (peekChar() == '}') break;
            parseDeclaration(declarations);
            skipWhitespaceAndComments();
        }
        expectChar('}');

        for (var sel : selectors) {
            sheet.addRule(sel, declarations);
        }
    }

    private List<Selector> parseSelectorList() {
        List<Selector> selectors = new ArrayList<>();
        selectors.add(parseSelector());
        skipWhitespaceAndComments();
        while (pos < input.length() && peekChar() == ',') {
            pos++; // skip ','
            skipWhitespaceAndComments();
            selectors.add(parseSelector());
            skipWhitespaceAndComments();
        }
        return selectors;
    }

    private Selector parseSelector() {
        skipWhitespaceAndComments();
        List<Selector> parts = new ArrayList<>();
        String typeName = null;

        // Check if it starts with an identifier (type selector)
        if (Character.isLetter(peekChar()) || peekChar() == '*' || peekChar() == '_') {
            if (peekChar() == '*') {
                pos++;
                parts.add(Selector.universal());
            } else {
                typeName = readIdent();
                parts.add(Selector.type(typeName));
            }
        }

        // Parse id/class/pseudo suffixes
        while (pos < input.length()) {
            char c = peekChar();
            if (c == '#') {
                pos++;
                String id = readIdent();
                parts.add(Selector.id(id));
            } else if (c == '.') {
                pos++;
                String cls = readIdent();
                parts.add(Selector.clazz(cls));
            } else if (c == ':') {
                pos++;
                String pseudo = readIdent();
                parts.add(Selector.pseudo(pseudo));
            } else {
                break;
            }
        }

        if (parts.size() == 1) {
            return parts.get(0);
        }
        return new Selector.Compound(parts);
    }

    private void parseDeclaration(Map<String, String> declarations) {
        String property = readIdent();
        skipWhitespaceAndComments();
        if (pos < input.length() && peekChar() == ':') {
            pos++; // skip ':'
        }
        skipWhitespaceAndComments();
        String value = readValue();
        expectSemicolon();
        declarations.put(property, value);
    }

    private String readValue() {
        skipWhitespaceAndComments();
        if (peek("var(")) {
            pos += 4; // skip 'var('
            skipWhitespaceAndComments();
            String varName;
            if (peek("--")) {
                pos += 2;
                varName = "--" + readIdent();
            } else {
                varName = readIdent();
            }
            skipWhitespaceAndComments();
            if (peekChar() == ')') {
                pos++;
            }
            return "var(" + varName + ")";
        }
        if (peek("rgb(")) {
            int start = pos;
            pos += 4;
            while (pos < input.length() && peekChar() != ')') {
                pos++;
            }
            if (peekChar() == ')') pos++;
            return input.substring(start, pos);
        }
        if (peekChar() == '#') {
            int start = pos;
            pos++; // skip '#'
            while (pos < input.length() && Character.isLetterOrDigit(peekChar())) {
                pos++;
            }
            return input.substring(start, pos);
        }
        return readIdent();
    }

    private String readIdent() {
        skipWhitespaceAndComments();
        StringBuilder sb = new StringBuilder();
        while (pos < input.length()) {
            char c = peekChar();
            if (Character.isLetterOrDigit(c) || c == '-' || c == '_') {
                sb.append(c);
                pos++;
            } else {
                break;
            }
        }
        return sb.toString();
    }

    // --- Lexer helpers ---

    private void skipWhitespaceAndComments() {
        while (pos < input.length()) {
            char c = peekChar();
            if (Character.isWhitespace(c)) {
                pos++;
            } else if (peek("/*")) {
                pos += 2;
                while (pos < input.length() && !peek("*/")) {
                    pos++;
                }
                if (peek("*/")) pos += 2;
            } else {
                break;
            }
        }
    }

    private void expect(String s) {
        skipWhitespaceAndComments();
        for (int i = 0; i < s.length(); i++) {
            if (pos >= input.length() || input.charAt(pos) != s.charAt(i)) {
                throw new IllegalArgumentException(
                    "Expected '" + s + "' at position " + pos);
            }
            pos++;
        }
    }

    private void expectChar(char c) {
        skipWhitespaceAndComments();
        if (pos >= input.length() || peekChar() != c) {
            throw new IllegalArgumentException(
                "Expected '" + c + "' at position " + pos
                + " but found '" + (pos < input.length() ? peekChar() : "EOF") + "'");
        }
        pos++;
    }

    private void expectSemicolon() {
        skipWhitespaceAndComments();
        if (pos < input.length() && peekChar() == ';') {
            pos++;
        }
    }

    private boolean peek(String s) {
        if (pos + s.length() > input.length()) return false;
        return input.substring(pos, pos + s.length()).equals(s);
    }

    private char peekChar() {
        return pos < input.length() ? input.charAt(pos) : '\0';
    }
}
