package com.prettybit.review.entity;

import com.google.common.base.Joiner;
import org.apache.commons.lang.ArrayUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Pavel Mikhalchuk
 */
public class Fork {

    private static final String RET = "((?<=%s)|(?=%s))";
    private static final String[] CUT_POINTS = {" ", "\\(", "\\)", "\\.", ";", ",", "\\{", "\\}"};

    private String l;
    private String r;

    public Fork(String l, String r) {
        this.l = l;
        this.r = r;
    }

    public String left() { return l; }

    public String right() { return r; }

    public static Fork hit(String left, String right) {
        return new Fork(left, right);
    }

    public static String[] cut(String line) {
        String[] split = line.split(cutPointsRegExp());
        return split.length > 0 && split[0].isEmpty() ? (String[]) ArrayUtils.remove(split, 0) : split;
    }

    protected static String cutPointsRegExp() {
        List<String> rel = new LinkedList<String>();
        for (String point : CUT_POINTS) {
            rel.add(String.format(RET, point, point));
        }
        return Joiner.on("|").join(rel);
    }

    public static void main(String[] args) {
        String[] l = cut("a b c");
        String[] r = cut(" b b c ");

        Map<Integer, String> lm = new HashMap<Integer, String>();
        Map<Integer, String> rm = new HashMap<Integer, String>();

        for (int i = 0; i < l.length; i++) {
            lm.put(i, l[i]);
        }

        for (int i = 0; i < r.length; i++) {
            rm.put(i, r[i]);
        }

        Map<Integer, String> lmd = new HashMap<Integer, String>(lm);
        Map<Integer, String> rmd = new HashMap<Integer, String>(rm);

        List<W> cw = new LinkedList<W>();

        for (Map.Entry<Integer, String> re : rm.entrySet()) {
            Map.Entry<Integer, String> le = findLe(lmd, re);
            if (le != null) {
                lmd.remove(le.getKey());
                rmd.remove(re.getKey());
                cw.add(new W(le.getKey(), re.getKey(), le.getValue()));
            }
        }


    }

    private static Map.Entry<Integer, String> findLe(Map<Integer, String> lmd, Map.Entry<Integer, String> re) {
        for (Map.Entry<Integer, String> le : lmd.entrySet()) {
            if (le.getValue().equals(re.getValue())) {
                return le;
            }
        }
        return null;
    }

    private static class W {
        private Integer li;
        private Integer ri;
        private String w;

        private W(Integer li, Integer ri, String w) {
            this.li = li;
            this.ri = ri;
            this.w = w;
        }

        public Integer getLi() {
            return li;
        }

        public Integer getRi() {
            return ri;
        }

        public String getW() {
            return w;
        }

        @Override
        public String toString() {
            return w + " " + li + ":" + ri;
        }
    }

}