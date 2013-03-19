package com.prettybit.review.entity;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import org.apache.commons.lang.ArrayUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;

/**
 * @author Pavel Mikhalchuk
 */
public class Fork {

    private static final String RET = "((?<=%s)|(?=%s))";
    private static final String[] CUT_POINTS = {" ", "\\(", "\\)", "\\.", ":", ";", ",", "\\{", "\\}", "\\<", "\\>", "\\/", "\\|", "\\\\", "\\+", "\\-", "\\*"};

    private String l;
    private String r;

    public Fork(String l, String r) {
        Graph g = new Graph(cut(l), cut(r));
        g.dijkstra();

        this.l = g.prettyX();
        this.r = g.prettyY();
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

    private static class Graph {

        private String[] x;
        private String[] y;

        private Node[][] g;

        private String prettyX = "";
        private String prettyY = "";

        private Graph(String[] x, String[] y) {
            this.x = x;
            this.y = y;
            build();
        }

        public void dijkstra() {
            node(0, 0).minPathCost = 0;
            doDijkstra(g[0][0]);

            pretty();
        }

        public Node node(int x, int y) {
            return g[x][y];
        }

        public String prettyX() { return prettyX; }

        public String prettyY() { return prettyY; }

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
            Node min = null;

            for (int i = 0; i <= x.length; i++) {
                for (int j = 0; j <= y.length; j++) {
                    if (node(i, j).isEnabled()) {
                        if (min == null || node(i, j).minPathCost < min.minPathCost) {
                            min = node(i, j);
                        }
                    }
                }
            }

            if (min != null) doDijkstra(min);

//            for (int i = 0; i <= x.length; i++) {
//                for (int j = 0; j <= y.length; j++) {
//                    if (node(i, j).isEnabled()) {
//                        doDijkstra(node(i, j));
//                    }
//                }
//            }
        }

        private Collection<Node> minPathMatchPoints() {
            return transform(filter(node(x.length, y.length).minPath, Edge.ifIsDiagonal()), Edge.toX2());
        }

        private void pretty() {
            int i = -1;
            int j = -1;

            for (Node n : minPathMatchPoints()) {
                List<String> l = new LinkedList<String>();
                List<String> r = new LinkedList<String>();

                i++;
                String w = x[i];
                while (!(w.equals(n.x) && i + 1 == n.xi) && i <= x.length) {
                    l.add(w);
                    i++;
                    w = x[i];
                }

                j++;
                w = y[j];
                while (!(w.equals(n.y) && j + 1 == n.yi) && j <= y.length) {
                    r.add(w);
                    j++;
                    w = y[j];
                }

                if (!l.isEmpty() && !r.isEmpty()) {
                    prettyX += "|$!|";
                    for (String _w : l) {
                        prettyX += _w;
                    }
                    prettyX += "|!$|";

                    prettyY += "|$!|";
                    for (String _w : r) {
                        prettyY += _w;
                    }
                    prettyY += "|!$|";
                } else if (!l.isEmpty()) {
                    prettyX += "|$-|";
                    for (String _w : l) {
                        prettyX += _w;
                    }
                    prettyX += "|-$|";

                    prettyY += "|$-||-$|";
                } else if (!r.isEmpty()) {
                    prettyY += "|$+|";
                    for (String _w : r) {
                        prettyY += _w;
                    }
                    prettyY += "|+$|";

                    prettyX += "|$+||+$|";
                }

                prettyX += n.x;
                prettyY += n.y;

                l = new LinkedList<String>();
                r = new LinkedList<String>();
            }

            if (i < x.length - 1 && j < y.length - 1) {
                prettyX += "|$!|";
                for (int kkk = i + 1; kkk < x.length; kkk++) {
                    prettyX += x[kkk];
                }
                prettyX += "|!$|";

                prettyY += "|$!|";
                for (int kkk = j + 1; kkk < y.length; kkk++) {
                    prettyY += y[kkk];
                }
                prettyY += "|!$|";
            } else if (i < x.length - 1) {
                prettyX += "|$-|";
                for (int kkk = i + 1; kkk < x.length; kkk++) {
                    prettyX += x[kkk];
                }
                prettyX += "|-$|";

                prettyY += "|$-||-$|";
            } else if (j < y.length - 1) {
                prettyY += "|$+|";
                for (int kkk = j + 1; kkk < y.length; kkk++) {
                    prettyY += y[kkk];
                }
                prettyY += "|+$|";

                prettyX += "|$+||+$|";
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

        private static class Edge {

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

            public static Predicate<Edge> ifIsDiagonal() {
                return new Predicate<Graph.Edge>() {
                    @Override
                    public boolean apply(Graph.Edge c) { return c.isDiagonal(); }
                };
            }

            public static Function<Edge, Node> toX2() {
                return new Function<Edge, Node>() {
                    @Override
                    public Node apply(Edge e) { return e.x2(); }
                };
            }

        }

    }

}