package com.prettybit.review.entity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Pavel Mikhalchuk
 */
public class Diff {

    private Map<Integer, Entry> map = new HashMap<Integer, Entry>();

    public Diff(List<Entry> diff) {
        for (Entry entry : diff) {
            map.put(entry.start(), entry);
        }
    }

    public Entry startingAt(Integer line) {
        return map.get(line);
    }

    public static Diff parse(String diff) throws IOException {
        return new Diff(doParse(diff));
    }

    protected static List<Entry> doParse(String diff) throws IOException {
        List<Entry> result = new LinkedList<Entry>();
        Reader reader = new Reader(diff);
        for (Entry entry = reader.read(); entry != null; entry = reader.read()) {
            result.add(entry);
        }
        return result;
    }

    public static class Reader {

        private Integer start;
        private List<Line> lines;

        private BufferedReader reader;

        public Reader(String diff) {
            reader = new BufferedReader(new StringReader(diff));
        }

        public Entry read() throws IOException {
            for (String line = reader.readLine(); ; line = reader.readLine()) {
                if (isSeparator(line)) {
                    if (lines != null) {
                        Entry result = new Entry(start, lines);
                        start = getStartFromSeparator(line);
                        lines = new LinkedList<Line>();
                        return result;
                    }
                    start = getStartFromSeparator(line);
                    lines = new LinkedList<Line>();
                } else if (line == null) {
                    if (lines == null) return null;
                    else {
                        Entry entry = new Entry(start, lines);
                        start = null;
                        lines = null;
                        return entry;
                    }
                } else if (lines != null) lines.add(new Line(Line.cutAction(line), Line.actionFromLine(line)));
            }
        }

        protected static boolean isSeparator(String line) {
            return line != null && line.matches("@@ -.+,.+ \\+.+,.+ @@");
        }

        protected static Integer getStartFromSeparator(String line) {
            return Integer.valueOf(line.substring(4, line.indexOf(",")));
        }

    }

    public static class Entry {

        private Integer start;

        private List<Line> lines;

        private Line lastLine;

        public Entry(Integer start, List<Line> lines) {
            this.start = start;
            this.lines = lines;
            lastLine = lines.get(lines().size() - 1);
        }

        public Integer start() {
            return start;
        }

        public List<Line> lines() {
            return lines;
        }

        public boolean lastLine(Line line) {
            return lastLine.line().equals(line.line());
        }

    }

}