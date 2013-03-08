package com.prettybit.review.entity;

import com.google.common.base.Joiner;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.lang.ArrayUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Pavel Mikhalchuk
 */
public class Fork extends JFrame {

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
        new Fork("", "").run();
    }

    private Map<Integer, String> toMap(String[] s) {
        Map<Integer, String> map = new TreeMap<Integer, String>();
        for (int i = 0; i < s.length; i++) {
            map.put(i, s[i]);
        }
        return map;
    }

    private Map<Integer, String> lm = toMap(cut("protected abstract void doIncrementalIndex(Multimap<Long, Long> itemsByAccount);"));
    private Map<Integer, String> rm = toMap(cut("protected abstract void doIncrementalIndex(Integer dbShardIndex, Multimap<Long, Long> itemsByAccount);"));

    private Map<Integer, String> lmd = new HashMap<Integer, String>(lm);

    private void run() {
        drawWindow();

        Multimap<Integer, WList> vars = LinkedHashMultimap.create();

        List<Map.Entry<Integer, String>> es = new ArrayList<Map.Entry<Integer, String>>(rm.entrySet());

        int i = 1;

        while (!es.isEmpty()) {
            WList w = calc(i++, es);
            vars.put(w.size(), w);

            es.remove(0);
            lmd = new HashMap<Integer, String>(lm);
        }

        int maxOverLap = 0;
        for (Integer overLap : vars.keySet()) {
            maxOverLap = Math.max(maxOverLap, overLap);
        }

        for (Integer k : vars.keySet()) {
            for (WList w : vars.get(k)) {
                addSides(maxOverLap, w);
            }
        }
    }

    private WList calc(Integer i, Collection<Map.Entry<Integer, String>> entries) {
        WList w = new WList(i);

        for (Map.Entry<Integer, String> re : entries) {
            Map.Entry<Integer, String> le = findLeft(re);
            if (le != null) {
                removeLdBeforeIncl(le.getKey());
                w.add(new W(le.getKey(), re.getKey(), re.getValue()));
            }
        }

        return w;
    }

    private void removeLdBeforeIncl(Integer i) {
        List<Integer> is = new LinkedList<Integer>();
        for (Integer li : lmd.keySet()) {
            if (li <= i) is.add(li);
        }
        for (Integer ir : is) {
            lmd.remove(ir);
        }
    }

    private void addSides(int maxOverLap, WList w) {
        getContentPane().add(new JLabel("  " + w.num() + "  "));

        JPanel yp = new JPanel();
        yp.setLayout(new BoxLayout(yp, BoxLayout.Y_AXIS));

        JPanel xp = new JPanel();
        xp.setLayout(new BoxLayout(xp, BoxLayout.X_AXIS));

        for (Map.Entry<Integer, String> e : lm.entrySet()) {
            if (w.hasLeft(e.getKey())) {
                addIn(xp, e.getValue());
            } else {
                addNonIn(xp, e.getValue());
            }
        }

        yp.add(xp);

        if (w.size() == maxOverLap) {
            yp.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        }

        xp = new JPanel();
        xp.setLayout(new BoxLayout(xp, BoxLayout.X_AXIS));

        for (Map.Entry<Integer, String> e : rm.entrySet()) {
            if (w.hasRight(e.getKey())) {
                addIn(xp, e.getValue());
            } else {
                addNonIn(xp, e.getValue());
            }
        }

        yp.add(xp);

        getContentPane().add(yp);
    }

    private Map.Entry<Integer, String> findLeft(Map.Entry<Integer, String> re) {
        for (Map.Entry<Integer, String> e : lmd.entrySet()) {
            if (e.getValue().equals(re.getValue())) return e;
        }
        return null;
    }

    private void drawWindow() {
        setSize(new Dimension(300, 300));

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setLocationRelativeTo(null);

        setVisible(true);
    }

    private void addIn(JPanel panel, String s) {
        JLabel l = new JLabel(s);
        l.setBackground(Color.CYAN);
        l.setForeground(Color.white);
        l.setOpaque(true);
        panel.add(l);
    }

    private void addNonIn(JPanel panel, String s) {
        JLabel l = new JLabel(s);
        l.setBackground(Color.BLUE);
        l.setForeground(Color.white);
        l.setOpaque(true);
        panel.add(l);
    }

    private class W {
        private Integer li;
        private Integer ri;
        private String w;

        private W(Integer li, Integer ri, String w) {
            this.li = li;
            this.ri = ri;
            this.w = w;
        }

        public Integer li() { return li; }

        public Integer ri() { return ri; }

        public String w() { return w; }

        @Override
        public String toString() {
            return w + " " + li + ":" + ri;
        }
    }

    private class WList {
        private int num;

        private WList(int num) {
            this.num = num;
        }

        private Map<Integer, W> l = new HashMap<Integer, W>();
        private Map<Integer, W> r = new HashMap<Integer, W>();
        private List<W> list = new LinkedList<W>();

        public void add(W w) {
            list.add(w);
            l.put(w.li, w);
            r.put(w.ri, w);
        }

        public int num() { return num; }

        public boolean hasLeft(Integer i) { return l.containsKey(i); }

        public boolean hasRight(Integer i) { return r.containsKey(i); }

        public int size() { return list.size(); }
    }

}