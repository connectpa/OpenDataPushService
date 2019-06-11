package it.connectpa.odatapushservice.rest;

import com.opencsv.CSVReader;
import it.connectpa.odatapushservice.dao.PushDataDAO;
import it.connectpa.odatapushservice.model.MetadataInfo;
import it.connectpa.odatapushservice.model.TableColumn;
import it.connectpa.odatapushservice.server.api.ApiApi;
import it.connectpa.odatapushservice.server.api.ResourceApi;
import it.connectpa.odatapushservice.server.model.Column;
import it.connectpa.odatapushservice.server.model.InstertedData;
import it.connectpa.odatapushservice.server.model.CreatedColumn;
import it.connectpa.odatapushservice.server.model.CreatedMetadata;
import it.connectpa.odatapushservice.server.model.InlineResponse400;
import it.connectpa.odatapushservice.server.model.Metadata;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

@RestController
public class PushService implements ApiApi, ResourceApi {

    private static final Logger LOG = LoggerFactory.getLogger(PushService.class);

    @Autowired
    private PushDataDAO pushDataDAO;

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    @Override
    public ResponseEntity<CreatedMetadata> createMetadata(final @RequestBody Metadata metadata) {
        LOG.info("POST for creating metadata: {}", metadata);

        String name = metadata.getName().replaceAll("\\s", "_").toLowerCase();
        String id = UUID.nameUUIDFromBytes(name.getBytes()).toString();
        if (pushDataDAO.findMetaData("name", name).isPresent()) {
            throw new BadRequestException(400, "The inserted matadata with name: " + metadata.getName()
                    + " is already existing");
        } else {
            MetadataInfo metadataInfo = new MetadataInfo(id, name, metadata.getDescription());
            LOG.info("The metdata after processing: {}", metadataInfo);
            pushDataDAO.insertMetaData(metadataInfo);
        }

        CreatedMetadata responsePayload = new CreatedMetadata();
        responsePayload.setId(id);
        responsePayload.setDescription(metadata.getDescription());
        responsePayload.setName(name);

        return new ResponseEntity<>(responsePayload, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<CreatedColumn> addColumn(
            final @PathVariable("id") String id,
            final @RequestBody Column column) {
        LOG.info("POST to create a new column : {}  in a dataset with id {}", column, id);

        Optional<String> tableName = pushDataDAO.findMetaData("id", id);
        if (tableName.isPresent()) {
            List<TableColumn> columns = pushDataDAO.findTableColumns(tableName.get());
            if (CollectionUtils.isEmpty(columns)
                    || !(columns.stream().anyMatch(c -> column.getName().toLowerCase()
                    .equals(c.getField().toLowerCase())))) {
                pushDataDAO.createColumn(tableName.get(), column);

            } else {
                throw new BadRequestException(400, "The column with name " + column.getName()
                        + " is already existing");
            }
        } else {
            throw new BadRequestException(404, "The dataset with id " + id + " does not exist");
        }

        CreatedColumn responsePayload = new CreatedColumn();
        responsePayload.setId(id);
        responsePayload.setDataTypeName(column.getDataTypeName());
        responsePayload.setName(column.getName());

        return new ResponseEntity<>(responsePayload, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<InstertedData> insertData(
            final @PathVariable("id") String id,
            final @RequestBody String body) {

        LOG.info("PUT: {} {}", id, body);

        InstertedData responsePayload = new InstertedData();
        try (StringReader reader = new StringReader(body);
                CSVReader csvReader = new CSVReader(reader)) {

            Integer recordsNumber = csvReader.readAll().size() - 1;
            responsePayload.setId(id);
            responsePayload.setRecordsNumber(recordsNumber);

            LOG.info("Number of records {}", recordsNumber);
        } catch (IOException e) {
            LOG.error("While parsing CSV file {}", e.getMessage());
        }

        return new ResponseEntity<>(responsePayload, HttpStatus.OK);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<InlineResponse400> badRequestException(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final BadRequestException e) {
        LOG.error("Bad request", e);

        InlineResponse400 responsePayload = new InlineResponse400();
        responsePayload.setCode(e.getCode());
        responsePayload.setMessage(e.getMessage());
        return new ResponseEntity<>(responsePayload, HttpStatus.BAD_REQUEST);
    }

}
