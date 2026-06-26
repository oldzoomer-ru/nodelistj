package ru.oldzoomer.nodelistj.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ParserUtils}.
 */
class ParserUtilsTest {

    // ─── shouldSkipLine ───────────────────────────────────────────────

    @Test
    void shouldSkipLine_commentLine_returnsTrue() {
        assertTrue(ParserUtils.shouldSkipLine(";A comment"));
        assertTrue(ParserUtils.shouldSkipLine(";"));
    }

    @Test
    void shouldSkipLine_blankLine_returnsTrue() {
        assertTrue(ParserUtils.shouldSkipLine(""));
        assertTrue(ParserUtils.shouldSkipLine("   "));
        assertTrue(ParserUtils.shouldSkipLine("\t"));
    }

    @Test
    void shouldSkipLine_normalLine_returnsFalse() {
        assertFalse(ParserUtils.shouldSkipLine("Zone,1,Test,City,Sysop,Phone,300"));
        assertFalse(ParserUtils.shouldSkipLine("Host,102,SoCalNet,LA,Lee,Phone,300"));
    }

    @Test
    void shouldSkipLine_lineStartingWithComma_returnsFalse() {
        // Line starting with comma is not a comment — it needs preprocessing
        assertFalse(ParserUtils.shouldSkipLine(",1,Test,City,Sysop,Phone,300"));
    }

    // ─── preprocessLine ────────────────────────────────────────────────

    @Test
    void preprocessLine_startsWithComma_addsEmptyKeywordFix() {
        String result = ParserUtils.preprocessLine(",1,Test,City,Sysop,Phone,300");
        assertEquals("###,1,Test,City,Sysop,Phone,300", result);
    }

    @Test
    void preprocessLine_normalLine_returnsUnchanged() {
        String line = "Zone,1,Test,City,Sysop,Phone,300";
        assertEquals(line, ParserUtils.preprocessLine(line));
    }

    @Test
    void preprocessLine_commentLine_returnsUnchanged() {
        String line = ";A comment line";
        assertEquals(line, ParserUtils.preprocessLine(line));
    }

    // ─── parseInteger ──────────────────────────────────────────────────

    @Test
    void parseInteger_validInteger_returnsParsedValue() {
        assertEquals(42, ParserUtils.parseInteger("42"));
        assertEquals(0, ParserUtils.parseInteger("0"));
        assertEquals(-5, ParserUtils.parseInteger("-5"));
        assertEquals(9600, ParserUtils.parseInteger("9600"));
    }

    @Test
    void parseInteger_withWhitespace_trimsAndParses() {
        assertEquals(42, ParserUtils.parseInteger("  42  "));
        assertEquals(300, ParserUtils.parseInteger(" 300"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void parseInteger_nullOrBlank_returnsNull(String input) {
        assertNull(ParserUtils.parseInteger(input));
    }

    @Test
    void parseInteger_nonNumeric_returnsNull() {
        assertNull(ParserUtils.parseInteger("abc"));
        assertNull(ParserUtils.parseInteger("12.5"));
        assertNull(ParserUtils.parseInteger("1e3"));
        assertNull(ParserUtils.parseInteger("-Unpublished-"));
    }

    // ─── extractFlags ──────────────────────────────────────────────────

    @Test
    void extractFlags_lessThanMinFields_returnsEmptyArray() {
        String[] fields = {"Zone", "1", "Test", "City", "Sysop", "Phone"};
        String[] flags = ParserUtils.extractFlags(fields);
        assertEquals(0, flags.length);
    }

    @Test
    void extractFlags_exactlyMinFields_returnsEmptyArray() {
        String[] fields = {"Zone", "1", "Test", "City", "Sysop", "Phone", "300"};
        String[] flags = ParserUtils.extractFlags(fields);
        assertEquals(0, flags.length);
    }

    @Test
    void extractFlags_moreThanMinFields_returnsExtraFields() {
        String[] fields = {"Zone", "1", "Test", "City", "Sysop", "Phone", "300", "CM", "XX", "INA:test"};
        String[] flags = ParserUtils.extractFlags(fields);
        assertEquals(3, flags.length);
        assertEquals("CM", flags[0]);
        assertEquals("XX", flags[1]);
        assertEquals("INA:test", flags[2]);
    }

    // ─── ParsingContext ────────────────────────────────────────────────

    @Test
    void parsingContext_initialState_allNull() {
        ParserUtils.ParsingContext ctx = new ParserUtils.ParsingContext();
        assertNull(ctx.getCurrentZone());
        assertNull(ctx.getCurrentNetwork());
        assertNull(ctx.getCurrentTree());
    }

    @Test
    void parsingContext_setAndGetZone() {
        ParserUtils.ParsingContext ctx = new ParserUtils.ParsingContext();
        ctx.setCurrentZone(1);
        assertEquals(1, ctx.getCurrentZone());
    }

    @Test
    void parsingContext_setAndGetNetwork() {
        ParserUtils.ParsingContext ctx = new ParserUtils.ParsingContext();
        ctx.setCurrentNetwork(102);
        assertEquals(102, ctx.getCurrentNetwork());
    }

    @Test
    void parsingContext_setAndGetTree() {
        ParserUtils.ParsingContext ctx = new ParserUtils.ParsingContext();
        ctx.setCurrentTree(ParserUtils.ParsingContext.TreeLevel.ZONE);
        assertEquals(ParserUtils.ParsingContext.TreeLevel.ZONE, ctx.getCurrentTree());

        ctx.setCurrentTree(ParserUtils.ParsingContext.TreeLevel.NETWORK);
        assertEquals(ParserUtils.ParsingContext.TreeLevel.NETWORK, ctx.getCurrentTree());
    }

    @Test
    void parsingContext_fullLifecycle() {
        ParserUtils.ParsingContext ctx = new ParserUtils.ParsingContext();
        ctx.setCurrentZone(2);
        ctx.setCurrentNetwork(2);
        ctx.setCurrentTree(ParserUtils.ParsingContext.TreeLevel.ZONE);

        assertEquals(2, ctx.getCurrentZone());
        assertEquals(2, ctx.getCurrentNetwork());
        assertEquals(ParserUtils.ParsingContext.TreeLevel.ZONE, ctx.getCurrentTree());

        // Update network for a region
        ctx.setCurrentNetwork(20);
        ctx.setCurrentTree(ParserUtils.ParsingContext.TreeLevel.NETWORK);

        assertEquals(2, ctx.getCurrentZone());        // zone preserved
        assertEquals(20, ctx.getCurrentNetwork());      // network updated
        assertEquals(ParserUtils.ParsingContext.TreeLevel.NETWORK, ctx.getCurrentTree());
    }

    // ─── Constants ─────────────────────────────────────────────────────

    @Test
    void constants_haveExpectedValues() {
        assertEquals(7, ParserUtils.MIN_FIELDS_REQUIRED);
        assertEquals(";", ParserUtils.COMMENT_PREFIX);
        assertEquals("###", ParserUtils.EMPTY_KEYWORD_FIX);
        assertEquals(",", ParserUtils.FIELD_SEPARATOR);
    }
}
