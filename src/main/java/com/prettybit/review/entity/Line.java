package com.prettybit.review.entity;

import org.apache.commons.lang.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * @author Pavel Mikhalchuk
 */
public class Line {

    private static Map<String, String> actions = new HashMap<String, String>();

    static {
        actions.put("+", "+");
        actions.put("-", "-");
    }

    private Integer number;
    private String action;
    private String line;

    public Line(String line) {
        this.line = line;
    }

    public Line(Integer number, String line) {
        this.number = number;
        this.line = line;
    }

    public Line(String line, String action) {
        this.line = line;
        this.action = action;
    }

    public Line(Integer number, String line, String action) {
        this.number = number;
        this.line = line;
        this.action = action;
    }

    public Integer number() {
        return number;
    }

    public String action() {
        return action;
    }

    public String line() {
        return line;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Line)) return false;
        if (!ObjectUtils.equals(number, ((Line) obj).number())) return false;
        if (!ObjectUtils.equals(line, ((Line) obj).line())) return false;
        if (!ObjectUtils.equals(action, ((Line) obj).action())) return false;
        return true;
    }

    @Override
    public String toString() {
        String s = "";
        if (number != null) s = number + ". " + line;
        else s = line;
        return action != null ? s += " " + action : s;
    }

    public static String actionFromLine(String line) {
        return isNotBlank(line) ? actions.get(line.substring(0, 1)) : null;
    }

    public static String cutAction(String line) {
        return actionFromLine(line) != null ? line.substring(1) : line;
    }

}