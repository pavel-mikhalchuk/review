package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.jaxrs.json.annotation.EndpointConfig;
import com.prettybit.review.entity.Log;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.lang.annotation.Annotation;

/**
 * @author Pavel Mikhalchuk
 */
public class ObjectWriterTest {

    @Test
    public void testWrite() throws IOException, JAXBException {
        ProcessBuilder builder = new ProcessBuilder("svn", "log", "/Users/pacan/Development/projects/Sears/APP-FRAMEWORK/trunk", "-l", "10", "-v", "--xml");
        builder.redirectErrorStream(true);
        Log log = Log.parse(builder.start().getInputStream());

        ByteOutputStream stream = new ByteOutputStream();

        GET get = new GET() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return GET.class;
            }
        };

        Path path = new Path() {

            @Override
            public String value() {
                return "log";
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return Path.class;
            }
        };

        Produces produces = new Produces() {

            @Override
            public String[] value() {
                return new String[]{MediaType.APPLICATION_JSON};
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return Produces.class;
            }
        };

        XmlRootElement root = new XmlRootElement() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return XmlRootElement.class;
            }

            @Override
            public String namespace() {
                return "";
            }

            @Override
            public String name() {
                return "log";
            }
        };

        ObjectWriter writer = EndpointConfig.forWriting(new ObjectMapper(), new Annotation[]{root}, null).getWriter();

        JsonGenerator generator = writer.getJsonFactory().createJsonGenerator(stream, JsonEncoding.UTF8);

        writer.writeValue(generator, log);

        System.out.println(stream.toString());
    }

}