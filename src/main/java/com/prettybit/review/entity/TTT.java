package com.prettybit.review.entity;

import javax.swing.*;
import java.util.HashMap;
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


}