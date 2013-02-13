package com.prettybit.review.entity;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.InputStream;
import java.util.List;

/**
 * @author Pavel Mikhalchuk
 */
@XmlRootElement(name = "log")
public class Log {

    @XmlElement(name = "logentry")
    private List<Revision> revisions;

    public List<Revision> revisions() {
        return revisions;
    }

    public static Log parse(InputStream stream) throws JAXBException {
        return (Log) JAXBContext.newInstance(Log.class).createUnmarshaller().unmarshal(stream);
    }

}