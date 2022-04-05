package org.globex.retail;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;

@ApplicationScoped
@Path("/activity")
public class ActivityTrackingResource {

    @Inject
    ActivityTrackingService activityTrackingService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/")
    public Uni<Response> activity(String payload) {
        return activityTrackingService.trackActivity(new JsonObject(payload)).onItem().transform(i -> Response.status(i).build());
    }
}
