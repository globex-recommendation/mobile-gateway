package org.globex.retail;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class ActivityTrackingService {

    @ConfigProperty(name = "activity-tracking-service.url")
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

    public Uni<Integer> trackActivity(JsonObject activity) {
        return client.post("/track").sendJsonObject(activity).onItem().transform(HttpResponse::statusCode);
    }

}
