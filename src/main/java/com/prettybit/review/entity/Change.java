package com.prettybit.review.entity;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * @author Pavel Mikhalchuk
 */
public class Change {

    @XmlAttribute
    private String action;

    @XmlValue
    private String file;

    public String action() {
        return action;
    }

    public String file() {
        return file;
    }

}