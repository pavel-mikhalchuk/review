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

        assertEquals("        for (Integer shardIndex : |$!|ShardLocator.allCatalogIndices()|!$|) {", f.left());
        assertEquals("        for (Integer shardIndex : |$!|shardIndices|!$|) {", f.right());

        f = Fork.hit("protected abstract void doIncrementalIndex(Multimap<Long, Long> itemsByAccount);",
                "protected abstract void doIncrementalIndex(Integer dbShardIndex, Multimap<Long, Long> itemsByAccount);");

        assertEquals("protected abstract void doIncrementalIndex(|$+||+$|Multimap<Long, Long> itemsByAccount);", f.left());
        assertEquals("protected abstract void doIncrementalIndex(|$+|Integer dbShardIndex, |+$|Multimap<Long, Long> itemsByAccount);", f.right());

        f = Fork.hit("DateRangeMessage message = DateRangeMessage.byteBufferToMessage(byteBuffer, DateRangeMessage.class);",
                "IncrementalIndexMessage message = IncrementalIndexMessage.byteBufferToMessage(byteBuffer, IncrementalIndexMessage.class);");

        assertEquals("|$!|DateRangeMessage|!$| message = |$!|DateRangeMessage|!$|.byteBufferToMessage(byteBuffer, |$!|DateRangeMessage|!$|.class);", f.left());
        assertEquals("|$!|IncrementalIndexMessage|!$| message = |$!|IncrementalIndexMessage|!$|.byteBufferToMessage(byteBuffer, |$!|IncrementalIndexMessage|!$|.class);", f.right());

        f = Fork.hit("return Constants.DUMMY_PATH.equals(asset.getAssetOriginalPath() != null ? asset.getAssetOriginalPath().trim() : asset.getAssetOriginalPath());",
                "return Constants.DUMMY_PATH.equals(path != null ? path.trim() : path);");

        assertEquals("return Constants.DUMMY_PATH.equals(|$!|asset.getAssetOriginalPath()|!$| != null ? |$!|asset.getAssetOriginalPath()|!$|.trim() : |$!|asset.getAssetOriginalPath()|!$|);", f.left());
        assertEquals("return Constants.DUMMY_PATH.equals(|$!|path|!$| != null ? |$!|path|!$|.trim() : |$!|path|!$|);", f.right());

        f = Fork.hit("a b c", " b b c ");

        assertEquals("|$!|a|!$| b c|$+||+$|", f.left());
        assertEquals("|$!| b|!$| b c|$+| |+$|", f.right());

        f = Fork.hit("                if (((input.getAppMask() & mask.getId()) != 0) && (isMaster == null || input.isMaster() == isMaster))",
                "                if (");

        assertEquals("                if (|$-|((input.getAppMask() & mask.getId()) != 0) && (isMaster == null || input.isMaster() == isMaster))|-$|", f.left());
        assertEquals("                if (|$-||-$|", f.right());
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