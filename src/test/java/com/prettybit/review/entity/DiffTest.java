package com.prettybit.review.entity;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Pavel Mikhalchuk
 */
public class DiffTest {

    @Test
    public void testParseDiff() throws IOException {
        List<Diff.Entry> diffs = Diff.doParse("pre\n@@ -666,s +l,s @@\ncontent\ncontent\n");

        assertEquals(1, diffs.size());
        assertEquals(asList(new Line("content"), new Line("content")), diffs.get(0).lines());

        diffs = Diff.doParse("pre\n@@ -3,s +l,s @@\ncontent\n@@ -6,s +l,s @@\n+post\n-post\n");

        assertEquals(2, diffs.size());
        assertEquals(asList(new Line("content")), diffs.get(0).lines());
        assertEquals(asList(new Line("post", "+"), new Line("post", "-")), diffs.get(1).lines());

        diffs = Diff.doParse("pre\n\n\n\n@@ -33,s +l,s @@\n\n\ncontent\n\n@@ -100,s +l,s @@\npost\n@@ -666,s +l,s @@\n\npost-post\n");

        assertEquals(3, diffs.size());
        assertEquals(asList(new Line(""), new Line(""), new Line("content"), new Line("")), diffs.get(0).lines());
        assertEquals(asList(new Line("post")), diffs.get(1).lines());
        assertEquals(asList(new Line(""), new Line("post-post")), diffs.get(2).lines());
    }

    @Test
    public void testIsSeparatorLine() {
        assertTrue(Diff.Reader.isSeparator("@@ -3,6 +3,7 @@"));
        assertTrue(Diff.Reader.isSeparator("@@ -29,13 +30,14 @@"));
        assertFalse(Diff.Reader.isSeparator("@ -29,13 +30,14 @@"));
        assertFalse(Diff.Reader.isSeparator("-29,13 +30,14 @@"));
        assertFalse(Diff.Reader.isSeparator("@@ -29,13 30,14 @@"));
        assertFalse(Diff.Reader.isSeparator("@@ -29,13+30,14 @@"));
        assertFalse(Diff.Reader.isSeparator("@@ -29,13+30,14@@"));
        assertFalse(Diff.Reader.isSeparator("@@ -29,13+30,14 @"));
        assertFalse(Diff.Reader.isSeparator("@@ -29,13+30,14"));
    }

    @Test
    public void testGetStartFromSeparator() {
        assertEquals(3, Diff.Reader.getStartFromSeparator("@@ -3,6 +3,7 @@").intValue());
        assertEquals(29, Diff.Reader.getStartFromSeparator("@@ -29,13 +30,14 @@").intValue());
    }

}