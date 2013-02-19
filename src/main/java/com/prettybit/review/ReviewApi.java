package com.prettybit.review;

import com.prettybit.review.entity.Comparison;
import com.prettybit.review.entity.Diff;
import com.prettybit.review.entity.Line;
import com.prettybit.review.entity.Log;
import org.apache.commons.io.IOUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

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

    @GET
    @Path("diff")
    @Produces(MediaType.APPLICATION_JSON)
    public Comparison diff(@QueryParam("file") String file, @QueryParam("rev") Integer revision) throws Exception {
        System.out.println(file + ":" + revision);
        return new Comparison(readLines(file, revision - 1), readDiff(file, revision));
    }

    private List<Line> readLines(String file, Integer revision) throws IOException {
        ProcessBuilder builder = new ProcessBuilder("svn", "cat", "-r", revision.toString(), "/Users/pacan/Development/projects/Sears/" + file);
        builder.redirectErrorStream(true);
        BufferedReader reader = new BufferedReader(new InputStreamReader(builder.start().getInputStream()));

        List<Line> base = new LinkedList<Line>();
        int i = 1;
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            base.add(new Line(i++, line));
        }
        return base;
    }

    private Diff readDiff(String file, Integer revision) throws IOException {
        ProcessBuilder builder = new ProcessBuilder("svn", "diff", "-c", revision.toString(), "/Users/pacan/Development/projects/Sears/" + file);
        builder.redirectErrorStream(true);
        return Diff.parse(IOUtils.toString(builder.start().getInputStream()));
    }

}