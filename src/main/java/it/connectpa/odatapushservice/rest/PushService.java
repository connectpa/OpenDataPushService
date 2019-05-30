package it.connectpa.odatapushservice.rest;

import it.connectpa.odatapushservice.server.api.ApiApi;
import it.connectpa.odatapushservice.server.api.ResourceApi;
import it.connectpa.odatapushservice.server.model.InlineObject;
import it.connectpa.odatapushservice.server.model.InlineObject1;
import it.connectpa.odatapushservice.server.model.InlineObject2;
import it.connectpa.odatapushservice.server.model.InlineResponse201;
import java.util.Optional;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

@RestController
public class PushService implements ApiApi, ResourceApi {

    private static final Logger LOG = LoggerFactory.getLogger(PushService.class);

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    @Override
    public ResponseEntity<Void> addType(
            final @PathVariable("id") String id,
            final @Valid @RequestBody InlineObject1 body) {

        LOG.info("POST: {} {}", id, body);
        return null;
    }

    @Override
    public ResponseEntity<InlineResponse201> createMetadata(final InlineObject body) {
        LOG.info("POST: {}", body);

        InlineResponse201 responsePayload = new InlineResponse201();
        responsePayload.setId("abcd-efgh");
        return new ResponseEntity<>(responsePayload, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> insertData(final String id, final InlineObject2 body) {
        LOG.info("PUT: {} {}", id, body);
        return null;
    }
}
