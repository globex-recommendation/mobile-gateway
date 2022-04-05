package org.globex.retail;

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@ApplicationScoped
@Path("/recommendation")
public class RecommendationResource {

    @Inject
    CatalogService catalogService;

    @Inject
    RecommendationService recommendationService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/product")
    public Uni<Response> getRecommendedProducts() {
        return recommendationService.recommendedProducts()
                .onItem().transformToUni(jsonArray -> {
                    List<Uni<JsonArray>> unis = new ArrayList<>();
                    if (jsonArray.isEmpty()) {
                        return Uni.createFrom().item(() -> jsonArray);
                    }
                    StringBuilder productIds = new StringBuilder();
                    jsonArray.stream().map(o -> (JsonObject)o).forEach(p -> productIds.append(p.getString("productId")).append(","));
                    Uni<JsonArray> products = catalogService.productList(productIds.substring(0, productIds.length()-1), false)
                            .onItem().invoke(p -> {
                                for (int i = 0; i < jsonArray.size(); i++) {
                                    JsonObject score = jsonArray.getJsonObject(i);
                                    for (int j = 0; j < p.size(); j++) {
                                        if (p.getJsonObject(j).getString("itemId").equals(score.getString("productId"))) {
                                            score.put("product", p.getJsonObject(j));
                                            break;
                                        }
                                    }
                                }
                            });
                    unis.add(products);
                    return Uni.combine().all().unis(unis).combinedWith(l -> jsonArray);
                })
                .onItem().transform(json -> Response.ok(json).build());
    }
}
