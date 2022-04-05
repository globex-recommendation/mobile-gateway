package org.globex.retail;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.smallrye.mutiny.Uni;

@ApplicationScoped
@Path("/catalog")
public class CatalogResource {

    @Inject
    CatalogService catalogService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/product/{id}")
    public Uni<Response> getProductById(@PathParam("id") String id, @QueryParam("inventory") Boolean inventory) {
        return catalogService.productById(id, inventory).onItem().transform(json -> {
            if (json == null) {
                return Response.status(Response.Status.NOT_FOUND.getStatusCode()).build();
            } else {
                return Response.ok(json).build();
            }
        });
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/products")
    public Uni<Response> getProducts(@QueryParam("limit") int limit, @QueryParam("page") int page, @QueryParam("inventory") Boolean inventory) {
        return catalogService.products(limit, page, inventory).onItem().transform(json -> Response.ok(json).build());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/product/list/{ids}")
    public Uni<Response> getProductList(@PathParam("ids") String ids, @QueryParam("inventory") Boolean inventory) {
        return catalogService.productList(ids, inventory).onItem().transform(json -> Response.ok(json).build());
    }
}
