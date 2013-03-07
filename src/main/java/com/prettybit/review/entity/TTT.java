package com.prettybit.review.entity;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Pavel Mikhalchuk
 */
public class TTT extends JFrame {

    private Map<Integer, String> l = new HashMap<Integer, String>();
    private Map<Integer, String> r = new HashMap<Integer, String>();

    private Map<Integer, String> ld = new HashMap<Integer, String>();
//    private Map<Integer, String> rd = new HashMap<Integer, String>();

    {
        l.put(0, "a");
        l.put(1, " ");
        l.put(2, "b");
        l.put(4, "c");
        r.put(0, " ");
        r.put(1, "b");
        r.put(2, " ");
        r.put(3, "b");
        r.put(4, " ");
        r.put(5, "c");
        r.put(6, " ");
        ld.putAll(l);
//        rd.putAll(r);
    }

    private void run() {
        drawWindow();

        List<Map.Entry<Integer, String>> es = new ArrayList<Map.Entry<Integer, String>>(r.entrySet());

        while (!es.isEmpty()) {
            calc(es);
            es.remove(0);
            ld = new HashMap<Integer, String>(l);
//            rd = new HashMap<Integer, String>(r);
        }
    }

    private void calc(Collection<Map.Entry<Integer, String>> entries) {
        WList w = new WList();

        for (Map.Entry<Integer, String> re : entries) {
            Map.Entry<Integer, String> le = findLeft(re);
            if (le != null) {
                removeLdBeforeIncl(le.getKey());
                w.add(new W(le.getKey(), re.getKey(), re.getValue()));
            }
        }

        addSides(w);
    }

    private void removeLdBeforeIncl(Integer i) {
        List<Integer> is = new LinkedList<Integer>();
        for (Integer li : ld.keySet()) {
            if (li <= i) is.add(li);
        }
        for (Integer ir : is) {
            ld.remove(ir);
        }
    }

    private void addSides(WList w) {
        JPanel panel = new JPanel();
        panel.setSize(new Dimension(300, 50));
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
//        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        for (Map.Entry<Integer, String> e : l.entrySet()) {
            if (w.hasLeft(e.getKey())) {
                addIn(panel, e.getValue());
            } else {
                addNonIn(panel, e.getValue());
            }
        }

        addL(panel, "   ||   ");

        for (Map.Entry<Integer, String> e : r.entrySet()) {
            if (w.hasRight(e.getKey())) {
                addIn(panel, e.getValue());
            } else {
                addNonIn(panel, e.getValue());
            }
        }

        getContentPane().add(panel);
    }

    private Map.Entry<Integer, String> findLeft(Map.Entry<Integer, String> re) {
        for (Map.Entry<Integer, String> e : ld.entrySet()) {
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

    private void addL(JPanel panel, String s) {
        JLabel l = new JLabel(s);
        getContentPane().add(l);
        panel.add(l);
    }

    public static void main(String[] args) {
        new TTT().run();
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
        private Map<Integer, W> l = new HashMap<Integer, W>();
        private Map<Integer, W> r = new HashMap<Integer, W>();
        private List<W> list = new LinkedList<W>();

        public void add(W w) {
            list.add(w);
            l.put(w.li, w);
            r.put(w.ri, w);
        }

        public boolean hasLeft(Integer i) { return l.containsKey(i); }

        public boolean hasRight(Integer i) { return r.containsKey(i); }
    }

}