package it.connectpa.odatapushservice.rest;

import com.opencsv.CSVReader;
import it.connectpa.odatapushservice.server.api.ApiApi;
import it.connectpa.odatapushservice.server.api.ResourceApi;
import it.connectpa.odatapushservice.server.model.Column;
import it.connectpa.odatapushservice.server.model.InstertedData;
import it.connectpa.odatapushservice.server.model.CreatedColumn;
import it.connectpa.odatapushservice.server.model.CreatedMetadata;
import it.connectpa.odatapushservice.server.model.Metadata;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class PushService implements ApiApi, ResourceApi {

    private static final Logger LOG = LoggerFactory.getLogger(PushService.class);

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    @Override
    public ResponseEntity<CreatedMetadata> createMetadata(final @RequestBody Metadata metadata) {
        LOG.info("POST for creating metadata: {}", metadata);

        CreatedMetadata responsePayload = new CreatedMetadata();
        responsePayload.setId(UUID.randomUUID().toString());
        responsePayload.setDescription(metadata.getDescription());
        responsePayload.setName(metadata.getName());

        return new ResponseEntity<>(responsePayload, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<CreatedColumn> addColumn(
            final @PathVariable("id") String id,
            final @RequestBody Column column) {
        LOG.info("POST to create a new column : {}  in a dataset with id {}", column, id);

        CreatedColumn responsePayload = new CreatedColumn();
        responsePayload.setId(id);
        responsePayload.setDataTypeName(column.getDataTypeName());
        responsePayload.setName(column.getName());

        return new ResponseEntity<>(responsePayload, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<InstertedData> insertData(
            final @PathVariable("id") String id,
            final @RequestParam("file") MultipartFile file) {
        LOG.info("PUT: {} {}", id, file.getOriginalFilename());

        InstertedData responsePayload = new InstertedData();
        try (InputStreamReader reader = new InputStreamReader(file.getInputStream());
                CSVReader csvReader = new CSVReader(reader)) {

            Integer recordsNumber = csvReader.readAll().size() - 1;
            responsePayload.setId(id);
            responsePayload.setRecordsNumber(recordsNumber);

            LOG.info("Number of records {}", recordsNumber);
        } catch (IOException e) {
            LOG.error("While parsing CSV file {}", e.getMessage());
        }

        return new ResponseEntity<>(responsePayload, HttpStatus.CREATED);
    }
}
