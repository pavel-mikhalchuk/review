package com.prettybit.review;

import com.prettybit.review.entity.Log;
import org.apache.commons.io.IOUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * @author Pavel Mikhalchuk
 */
@Path("review")
public class ReviewApi {

    @GET
    @Path("log")
    @Produces(MediaType.APPLICATION_JSON)
    public Log log() throws Exception {
        ProcessBuilder builder = new ProcessBuilder("svn", "log", "/Users/pacan/Development/projects/Sears/APP-FRAMEWORK/trunk", "-l", "10", "-v", "--xml");
        builder.redirectErrorStream(true);
        return Log.parse(builder.start().getInputStream());
    }

    @GET
    @Path("cat")
    public String cat(@QueryParam("file") String file, @QueryParam("rev") String revision) throws Exception {
        System.out.println(file + ":" + revision);
        ProcessBuilder builder = new ProcessBuilder("svn", "cat", "-r", revision, "/Users/pacan/Development/projects/Sears/" + file);
        builder.redirectErrorStream(true);
        return IOUtils.toString(builder.start().getInputStream());
    }

}