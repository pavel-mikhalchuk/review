package com.prettybit.review;

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
import java.util.Iterator;
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
    public String diff(@QueryParam("file") String file, @QueryParam("rev") String revision) throws Exception {
        System.out.println(file + ":" + revision);
        ProcessBuilder builder = new ProcessBuilder("svn", "diff", "-c", revision, "/Users/pacan/Development/projects/Sears/" + file);
        builder.redirectErrorStream(true);
//        return Joiner.on("\n(#*&#(*&$(#*&$(*#&$(*#&(*$\n").join(Diff.doParse(IOUtils.toString(builder.start().getInputStream())));
        return null;
    }

    public static void main(String[] args) throws IOException {
        ProcessBuilder builder = new ProcessBuilder("svn", "cat", "-r", "124723", "/Users/pacan/Development/projects/Sears/app-framework/trunk/app-core/src/main/java/com/shc/obu/app/framework/processor/IncrementalIndexProcessor.java");
        builder.redirectErrorStream(true);

        int i = 1;

        List<Line> lines = new LinkedList<Line>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(builder.start().getInputStream()));

        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            lines.add(new Line(i++, line));
        }

        builder = new ProcessBuilder("svn", "diff", "-c", "124724", "/Users/pacan/Development/projects/Sears/app-framework/trunk/app-core/src/main/java/com/shc/obu/app/framework/processor/IncrementalIndexProcessor.java");
        builder.redirectErrorStream(true);

        Diff diff = Diff.parse(IOUtils.toString(builder.start().getInputStream()));

        Iterator<Line> baseI = lines.iterator();

        Line baseLine = baseI.next();

        Iterator<Line> diffI = null;

        Line diffLine = null;

        while (baseI.hasNext()) {
            if (diffI != null) {
                if (diffI.hasNext()) {
                    diffLine = diffI.next();
                    if (diffLine.action() == null || !diffLine.action().equals("-")) {
                        System.out.println(diffLine + " !!!!!!!!!!!!");
                    }
                    if (diffLine.action() == null || !diffLine.action().equals("+")) {
                        baseLine = baseI.next();
                    }
                } else {
                    diffI = null;
                }
            } else {
                Diff.Entry entry = diff.startingAt(baseLine.number());
                if (entry != null) diffI = entry.lines().iterator();
                else {
                    System.out.println(baseLine);
                    baseLine = baseI.next();
                }
            }
        }

        System.out.println(baseLine);

    }

}