package com.prettybit.review.entity;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Pavel Mikhalchuk
 */
public class LineTest {

    @Test
    public void testActionFromLine() {
        assertEquals("-", Line.actionFromLine("-asd"));
        assertEquals("+", Line.actionFromLine("+asd"));
        assertEquals(null, Line.actionFromLine("asd"));
        assertEquals(null, Line.actionFromLine("  "));
        assertEquals(null, Line.actionFromLine(""));
        assertEquals(null, Line.actionFromLine(null));
    }

}