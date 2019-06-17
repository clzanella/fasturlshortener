package com.seniortest.fasturlshortener.controller;

import com.google.gson.JsonObject;
import com.seniortest.fasturlshortener.common.InstantProvider;
import com.seniortest.fasturlshortener.model.ShortURL;
import com.seniortest.fasturlshortener.service.ShortURLService;
import org.jooby.Request;
import org.jooby.Result;
import org.jooby.Results;
import org.jooby.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Duration;
import java.util.Optional;

//@RestController
public class ShortURLController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShortURLController.class);

    private final Duration expiration;
    private final ShortURLService urlService;
    private final InstantProvider instantProvider;

    public ShortURLController(Duration expiration, ShortURLService urlService, InstantProvider instantProvider) {
        this.expiration = expiration;
        this.urlService = urlService;
        this.instantProvider = instantProvider;
    }


    public Result shortenUrl(Request request) throws Exception {
        JsonObject body = request.body(JsonObject.class);
        String url = body.get("url").getAsString();
        LOGGER.info("Received url to shorten: {}", url);

        if(! urlService.validateURL(url)){
            return Results.with(Status.UNPROCESSABLE_ENTITY);
        }

        if(!url.startsWith("http")){
            url = "http://" + url;
        }

        ShortURL shortURL = new ShortURL();
        shortURL.setUrl(url);
        shortURL.setCreatedAt(instantProvider.now());
        shortURL.setExpireSeconds(this.expiration.getSeconds());

        urlService.save(shortURL);

        String shortId = urlService.idToString(shortURL.getId());
        URI uri = new URI("http", null, request.hostname(), request.port(), null, null, null).resolve("/" + shortId);

        JsonObject response = new JsonObject();
        response.addProperty("newUrl", uri.toString());
        response.addProperty("expiresAt", shortURL.getCreatedAt().plusSeconds(shortURL.getExpireSeconds()).toEpochMilli());

        return Results.json(response);
    }


    public Result redirectUrl(Request request) {

        String id = request.param("id").value();
        LOGGER.info("Received shortened url to redirect: {}", id);

        Optional<ShortURL> entity = urlService.findByStringID(id);

        if(!entity.isPresent()){
            return Results.with(Status.NOT_FOUND);
        }

        String redirectUrlString = entity.get().getUrl();
        LOGGER.info("Original URL. From {} to {}", id, redirectUrlString);

        if(instantProvider.now().isAfter(entity.get().getCreatedAt().plusSeconds(entity.get().getExpireSeconds()))){
            LOGGER.info("Expired URL {}", id);
            return Results.with(Status.GONE);
        }

        return Results.tempRedirect(redirectUrlString);
    }

}
