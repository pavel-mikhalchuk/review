package com.prettybit.review.entity;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Pavel Mikhalchuk
 */
public class ForkTest {

    @Test
    public void testHit() {
        Fork f = Fork.hit("        for (Integer shardIndex : ShardLocator.allCatalogIndices()) {",
                "        for (Integer shardIndex : shardIndices) {");

        assertEquals("        for (Integer shardIndex : |ShardLocator.allCatalogIndices()|) {", f.left());
        assertEquals("        for (Integer shardIndex : |shardIndices|) {", f.right());
    }

    @Test
    public void testCut() {
        String[] expected = {" ", " ", " ", " ", " ", " ", " ", " ", "for", " ", "(", "Integer", " ", "shardIndex",
                " ", ":", " ", "ShardLocator", ".", "allCatalogIndices", "(", ")", ")", " ", "{"};
        assertArrayEquals(expected, Fork.cut("        for (Integer shardIndex : ShardLocator.allCatalogIndices()) {"));
    }

    @Test
    public void testGetCutPointsRegExp() {
        assertEquals("((?<= )|(?= ))|((?<=\\()|(?=\\())|((?<=\\))|(?=\\)))|((?<=\\.)|(?=\\.))|((?<=;)|(?=;))|((?<=,)|(?=,))|((?<=\\{)|(?=\\{))|((?<=\\})|(?=\\}))",
                Fork.cutPointsRegExp());
    }

}