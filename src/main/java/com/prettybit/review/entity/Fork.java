package com.prettybit.review.entity;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.apache.commons.lang.ArrayUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
        List<String> result = new LinkedList<String>();
        for (String point : CUT_POINTS) {
            result.add(String.format(RET, point, point));
        }
        return Joiner.on("|").join(result);
    }

    public static void main(String[] args) {
        new Fork("", "").run();
    }

//    private String[] lm = cut("protected abstract void doIncrementalIndex(Multimap<Long, Long> itemsByAccount);");
//    private String[] rm = cut("protected abstract void doIncrementalIndex(Integer dbShardIndex, Multimap<Long, Long> itemsByAccount);");

//    private String[] lm = cut("DateRangeMessage message = DateRangeMessage.byteBufferToMessage(byteBuffer, DateRangeMessage.class);");
//    private String[] rm = cut("IncrementalIndexMessage message = IncrementalIndexMessage.byteBufferToMessage(byteBuffer, IncrementalIndexMessage.class);");

    private String[] lm = cut("return Constants.DUMMY_PATH.equals(asset.getAssetOriginalPath() != null ? asset.getAssetOriginalPath().trim() : asset.getAssetOriginalPath());");
    private String[] rm = cut("return Constants.DUMMY_PATH.equals(path != null ? path.trim() : path);");

//    private String[] lm = cut("a b c");
//    private String[] rm = cut(" b b c ");

    private void run() {
        drawWindow();

        Graph g = new Graph(lm, rm);
        g.dijkstra();

//        System.out.println("Cost: " + g.node(lm.length, rm.length).minPathCost);
//        System.out.println("Path: " + g.node(lm.length, rm.length).minPath);

//        System.out.println("Sol: " + Collections2.transform(Collections2.filter(g.node(lm.length, rm.length).minPath, new Predicate<Graph.Edge>() {
//            @Override
//            public boolean apply(Graph.Edge c) { return c.isDiagonal(); }
//        }), new Function<Graph.Edge, Object>() {
//            @Override
//            public Object apply(Graph.Edge c) {
//                return c.x2().x + "(" + c.x2 + ")";
//            }
//        }));

        Collection<Graph.Node> mp = Collections2.transform(Collections2.filter(g.node(lm.length, rm.length).minPath,
                new Predicate<Graph.Edge>() {
                    @Override
                    public boolean apply(Graph.Edge c) {
                        return c.isDiagonal();
                    }
                }), new Function<Graph.Edge, Graph.Node>() {
            @Override
            public Graph.Node apply(Graph.Edge d) {
                return d.x2();
            }
        });

        WList wl = new WList(1);

        for (Graph.Node ddd : mp) {
            wl.add(new W(ddd.xi, ddd.yi, ddd.x));
        }

        addSides(wl);
    }

    private class Graph {

        private String[] x;
        private String[] y;

        private Node[][] g;

        private Graph(String[] x, String[] y) {
            this.x = x;
            this.y = y;
            build();
        }

        public void dijkstra() {
            node(0, 0).minPathCost = 0;
            doDijkstra(g[0][0]);
        }

        public Node node(int x, int y) {
            return g[x][y];
        }

        private void doDijkstra(Node node) {
            for (Edge c : node.connections) {
                if (c.x2().minPathCost > node.minPathCost + c.cost) {
                    c.x2().minPathCost = node.minPathCost + c.cost;
                    c.x2().minPath.clear();
                    c.x2().minPath.addAll(node.minPath);
                    c.x2().minPath.add(c);
                }
            }
            node.disable();

            doDijkstraForFirstEnabledNode();
        }

        private void doDijkstraForFirstEnabledNode() {
            for (int i = 0; i <= x.length; i++) {
                for (int j = 0; j <= y.length; j++) {
                    if (node(i, j).isEnabled()) doDijkstra(node(i, j));
                }
            }
        }

        private void build() {
            g = new Node[x.length + 1][y.length + 1];

            for (int i = 0; i <= x.length; i++) {
                for (int j = 0; j <= y.length; j++) {
                    g[i][j] = new Node(this, i, i == 0 ? "0" : x[i - 1], j, j == 0 ? "0" : y[j - 1]);

                    if (i != x.length) { g[i][j].connect(i + 1, j, 1); }
                    if (i <= x.length - 1 && j <= y.length - 1 && x[i].equals(y[j])) { g[i][j].connect(i + 1, j + 1, 0); }
                    if (j != y.length) { g[i][j].connect(i, j + 1, 1); }
                }
            }
        }

        private class Node {

            private Graph parent;

            private Integer xi;
            private String x;

            private Integer yi;
            private String y;

            private List<Edge> connections = new LinkedList<Edge>();

            private Integer minPathCost = Integer.MAX_VALUE;
            private List<Edge> minPath = new LinkedList<Edge>();

            private boolean enabled = true;

            public Node(Graph parent, Integer xi, String x, Integer yi, String y) {
                this.parent = parent;
                this.x = x;
                this.xi = xi;
                this.y = y;
                this.yi = yi;
            }

            public void connect(Integer x, Integer y, Integer cost) {
                connections.add(new Edge(parent, xi, yi, x, y, cost));
            }

            public boolean isEnabled() { return enabled; }

            public void disable() { enabled = false; }

            @Override
            public String toString() {
                return String.format("[%s, %s]", xi, yi);
            }

        }

        private class Edge {

            private Graph parent;

            private Integer x1;
            private Integer y1;

            private Integer x2;
            private Integer y2;

            private Integer cost;

            public Edge(Graph parent, Integer x1, Integer y1, Integer x2, Integer y2, Integer cost) {
                this.parent = parent;
                this.x1 = x1;
                this.y1 = y1;
                this.x2 = x2;
                this.y2 = y2;
                this.cost = cost;
            }

            public Node x1() {
                return parent.node(x1, y1);
            }

            public Node x2() {
                return parent.node(x2, y2);
            }

            public boolean isDiagonal() {
                return x2 == x1 + 1 && y2 == y1 + 1;
            }

            @Override
            public String toString() {
                return x1() + ":" + x2();
            }

        }

    }

    private void addSides(WList w) {
        ppp.add(new JLabel("  " + w.num() + ":::" + w.size() + "  "));

        JPanel yp = new JPanel();
        yp.setLayout(new BoxLayout(yp, BoxLayout.Y_AXIS));

        JPanel xp = new JPanel();
        xp.setLayout(new BoxLayout(xp, BoxLayout.X_AXIS));

        for (int i = 1; i <= lm.length; i++) {
            if (w.hasLeft(i)) {
                addIn(xp, lm[i - 1]);
            } else {
                addNonIn(xp, lm[i - 1]);
            }
        }

        yp.add(xp);

        yp.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        xp = new JPanel();
        xp.setLayout(new BoxLayout(xp, BoxLayout.X_AXIS));

        for (int j = 1; j <= rm.length; j++) {
            if (w.hasRight(j)) {
                addIn(xp, rm[j - 1]);
            } else {
                addNonIn(xp, rm[j - 1]);
            }
        }

        yp.add(xp);

        ppp.add(yp);
    }

    private JPanel ppp = new JPanel();

    private void drawWindow() {
        setSize(new Dimension(300, 300));

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        ppp.setLayout(new BoxLayout(ppp, BoxLayout.Y_AXIS));
        getContentPane().add(new JScrollPane(ppp));

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
        private Integer num;

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

        public Integer num() { return num; }

        public boolean hasLeft(Integer i) { return l.containsKey(i); }

        public boolean hasRight(Integer i) { return r.containsKey(i); }

        public int size() { return list.size(); }
    }

}