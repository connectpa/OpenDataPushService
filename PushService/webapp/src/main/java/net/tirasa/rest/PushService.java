package net.tirasa.rest;

import net.tirasa.api.ApiApi;
import net.tirasa.api.ResourceApi;
import net.tirasa.model.Body;
import net.tirasa.model.Body1;
import net.tirasa.model.Body2;
import net.tirasa.model.InlineResponse201;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class PushService implements ApiApi, ResourceApi {

    private static final Logger LOG = LoggerFactory.getLogger(PushService.class);

    @Override
    public ResponseEntity<Void> addType(final Body1 body, final String id) {
        LOG.info("POST: {}", body);
        return null;
    }

    @Override
    public ResponseEntity<InlineResponse201> createMetadata(final Body body) {
        LOG.info("POST: {}", body);

        InlineResponse201 responsePayload = new InlineResponse201();
        responsePayload.setId("abcd-efgh");
        return new ResponseEntity<>(responsePayload, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> insertData(final Body2 body, final String id) {
        LOG.info("PUT: {}", body);
        return null;
    }
}
