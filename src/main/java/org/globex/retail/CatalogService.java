package org.globex.retail;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;

import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class CatalogService {

    private static final Logger log = LoggerFactory.getLogger(CatalogService.class);

    @ConfigProperty(name = "catalog-service.url")
    String serviceUrl;

    @Inject
    Vertx vertx;

    WebClient client;

    void onStart(@Observes StartupEvent e) {
        int servicePort = serviceUrl.contains(":") ? Integer.parseInt(serviceUrl.substring(serviceUrl.indexOf(":") + 1)) : 8080;
        String serviceHost = serviceUrl.contains(":") ? serviceUrl.substring(0, serviceUrl.indexOf(":")) : serviceUrl;
        WebClientOptions options = new WebClientOptions().setDefaultHost(serviceHost).setDefaultPort(servicePort).setMaxPoolSize(100).setHttp2MaxPoolSize(100).setSsl(servicePort == 443);
        client = WebClient.create(vertx, options);
    }

    public Uni<JsonObject> productById(String id, Boolean inventory) {
        boolean inv = inventory == null || inventory;
        return client.get("/services/product/" + id).addQueryParam("inventory", String.valueOf(inv)).send().onItem().transform(resp -> {
            if (resp.statusCode() == 200) {
                return resp.bodyAsJsonObject();
            } else if (resp.statusCode() == 404) {
                return null;
            } else {
                log.error("Error when calling catalog service. Return code " + resp.statusCode());
                throw new WebApplicationException(resp.statusCode());
            }
        });

    }

    public Uni<JsonObject> products(int limit, int page, Boolean inventory) {
        boolean inv = inventory == null || inventory;
        return client.get("/services/products").addQueryParam("inventory", String.valueOf(inv)).addQueryParam("limit", String.valueOf(limit))
                .addQueryParam("page", String.valueOf(page)).send().onItem().transform(resp -> {
            if (resp.statusCode() == 200) {
                return resp.bodyAsJsonObject();
            } else {
                log.error("Error when calling catalog service. Return code " + resp.statusCode());
                throw new WebApplicationException(resp.statusCode());
            }
        });
    }

    public Uni<JsonArray> productList(String products, Boolean inventory) {
        boolean inv = inventory == null || inventory;
        return client.get("/services/product/list/" + products).addQueryParam("inventory", String.valueOf(inv)).send().onItem().transform(resp -> {
            if (resp.statusCode() == 200) {
                return resp.bodyAsJsonArray();
            } else {
                log.error("Error when calling catalog service. Return code " + resp.statusCode());
                throw new WebApplicationException(resp.statusCode());
            }
        });
    }
}
