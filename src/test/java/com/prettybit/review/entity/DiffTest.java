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
    public void testParseReadDiff() throws IOException {
        List<Diff.Entry> diff = Diff.doParse("Index: app-core/src/main/java/com/shc/obu/app/framework/processor/IncrementalIndexProcessor.java\n" +
                "===================================================================\n" +
                "--- app-core/src/main/java/com/shc/obu/app/framework/processor/IncrementalIndexProcessor.java\t(revision 124723)\n" +
                "+++ app-core/src/main/java/com/shc/obu/app/framework/processor/IncrementalIndexProcessor.java\t(revision 124724)\n" +
                "@@ -3,6 +3,7 @@\n" +
                " import com.shc.obu.app.framework.fetchers.FetchParamsKeys;\n" +
                " import com.shc.obu.app.framework.flow.Flow;\n" +
                " import com.shc.obu.app.framework.msg.DateRangeMessage;\n" +
                "+import com.shc.obu.app.framework.msg.IncrementalIndexMessage;\n" +
                " import com.shc.obu.app.framework.parameters.Parameters;\n" +
                " import com.shc.obu.app.framework.registry.ComponentLookupService;\n" +
                " import com.shc.obu.ca.common.fsqueue.AsyncMessageProcessor;\n" +
                "@@ -29,13 +30,14 @@\n" +
                " \n" +
                "     @Override\n" +
                "     public void onMessage(ByteBuffer byteBuffer, int i, Logger logger) {\n" +
                "-        DateRangeMessage message = DateRangeMessage.byteBufferToMessage(byteBuffer, DateRangeMessage.class);\n" +
                "+        IncrementalIndexMessage message = IncrementalIndexMessage.byteBufferToMessage(byteBuffer, IncrementalIndexMessage.class);\n" +
                "         try {\n" +
                "             Flow<Parameters> flow = ComponentLookupService.getFlow(flowName, flowVersion);\n" +
                " \n" +
                "             Parameters params = new Parameters();\n" +
                "             params.put(FetchParamsKeys.startDate, message.getStartDateTime());\n" +
                "             params.put(FetchParamsKeys.endDate, message.getEndDateTime());\n" +
                "+            params.put(FetchParamsKeys.dbShardIndex, message.getDbShardIndex());\n" +
                "             flow.preProcess(params);\n" +
                " \n" +
                "             flow.process();\n");

        assertEquals(2, diff.size());
        assertEquals(asList(
                new Line("import com.shc.obu.app.framework.fetchers.FetchParamsKeys;"),
                new Line("import com.shc.obu.app.framework.flow.Flow;"),
                new Line("import com.shc.obu.app.framework.msg.DateRangeMessage;"),
                new Line("import com.shc.obu.app.framework.msg.IncrementalIndexMessage;", "+"),
                new Line("import com.shc.obu.app.framework.parameters.Parameters;"),
                new Line("import com.shc.obu.app.framework.registry.ComponentLookupService;"),
                new Line("import com.shc.obu.ca.common.fsqueue.AsyncMessageProcessor;")
        ), diff.get(0).lines());
        assertEquals(asList(
                new Line(""),
                new Line("    @Override"),
                new Line("    public void onMessage(ByteBuffer byteBuffer, int i, Logger logger) {"),
                new Line("        DateRangeMessage message = DateRangeMessage.byteBufferToMessage(byteBuffer, DateRangeMessage.class);", "-"),
                new Line("        IncrementalIndexMessage message = IncrementalIndexMessage.byteBufferToMessage(byteBuffer, IncrementalIndexMessage.class);", "+"),
                new Line("        try {"),
                new Line("            Flow<Parameters> flow = ComponentLookupService.getFlow(flowName, flowVersion);"),
                new Line(""),
                new Line("            Parameters params = new Parameters();"),
                new Line("            params.put(FetchParamsKeys.startDate, message.getStartDateTime());"),
                new Line("            params.put(FetchParamsKeys.endDate, message.getEndDateTime());"),
                new Line("            params.put(FetchParamsKeys.dbShardIndex, message.getDbShardIndex());", "+"),
                new Line("            flow.preProcess(params);"),
                new Line(""),
                new Line("            flow.process();")
        ), diff.get(1).lines());
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