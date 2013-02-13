package com.prettybit.review.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.Date;
import java.util.List;

/**
 * @author Pavel Mikhalchuk
 */
public class Revision {

    @XmlAttribute
    private String revision;

    @XmlElement
    private String author;

    @XmlElement
    @JsonFormat(pattern = "M/d/yy h:mm a")
    private Date date;

    @XmlElement(name = "msg")
    private String message;

    @XmlElementWrapper(name = "paths")
    @XmlElement(name = "path")
    private List<Change> changes;

    public String revision() {
        return revision;
    }

    public String author() {
        return author;
    }

    public Date date() {
        return date;
    }

    public String message() {
        return message;
    }

    public List<Change> changes() {
        return changes;
    }

}