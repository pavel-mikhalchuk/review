package com.prettybit.review.entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * @author Pavel Mikhalchuk
 */
@XmlRootElement(name = "comparison")
public class Comparison {

    @XmlElement(name = "left")
    private List<Line> left = new LinkedList<Line>();

    @XmlElement(name = "right")
    private List<Line> right = new LinkedList<Line>();

    public Comparison() {
    }

    public Comparison(List<Line> base, Diff diff) {
        Iterator<Line> d, b = base.iterator();
        int i = 1;

        while (b.hasNext()) {
            Line l = b.next();

            left.add(new Line(i, l.line()));
            right.add(new Line(i, l.line()));
            if (diff.existFor(l)) {
                d = diff.startingAt(l).lines().iterator();
                d.next();
                Line prevLeftLine = null;
                Line prevRightLine = null;
                boolean minus = false;

                for (l = d.next(); d.hasNext(); l = d.next()) {
                    if (l.withAction("+")) {
                        if (minus) {
                            prevLeftLine.setAction("!");
                            prevRightLine.setAction("!");
                            prevRightLine.setLine(l.line());
                            minus = false;
                        } else {
                            i++;
                            left.add(new Line(i, "", l.action()));
                            right.add(new Line(i, l.line(), l.action()));
                        }
                    } else {
                        i++;
                        minus = l.withAction("-");
                        left.add(prevLeftLine = new Line(i, l.line(), l.action()));
                        right.add(prevRightLine = new Line(i, minus ? "" : l.line(), l.action()));
                        b.next();
                    }
                }
            }
            i++;
        }
    }

    public List<Line> left() {
        return unmodifiableList(left);
    }

    public List<Line> right() {
        return unmodifiableList(right);
    }

}