package tv.notube.commons.storage.alog;

import com.sun.jersey.api.core.InjectParam;
import org.joda.time.DateTime;
import tv.notube.commons.storage.model.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.UUID;

/**
 * put class description here
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
@Path("/activities")
@Produces(MediaType.APPLICATION_JSON)
public class ActivityLogService {

    @InjectParam
    private InstanceManager instanceManager;

    @GET
    @Path("/{owner}")
    public Response filter(
            @PathParam("owner") String owner,
            @QueryParam("from") Long from,
            @QueryParam("to") Long to,
            @QueryParam("q") String query
    ) {
        ActivityLog activityLog = instanceManager.getActivityLog();
        Query queryObj = new Query();
        try {
            query = URLDecoder.decode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Error while URL-decoding query", e);
        }
        try {
            Query.decompile(query, queryObj);
        } catch (QueryException e) {
            throw new RuntimeException("Query is malformed", e);
        }
        DateTime fromDt = getDate(from);
        DateTime toDt = getDate(to);
        Activity[] activities;
        try {
           activities =  activityLog.filter(fromDt, toDt, owner, queryObj);
        } catch (ActivityLogException e) {
            throw new RuntimeException("Error while calling alog", e);
        }
        return new Response(
                Response.Status.OK,
                "activities found",
                activities
        );
    }

    private DateTime getDate(long millisec) {
        return new DateTime(millisec);
    }

}
