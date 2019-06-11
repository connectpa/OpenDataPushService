package it.connectpa.odatapushservice.dao.impl;

import it.connectpa.odatapushservice.dao.PushDataDAO;
import it.connectpa.odatapushservice.model.MetadataInfo;
import it.connectpa.odatapushservice.model.TableColumn;
import it.connectpa.odatapushservice.server.model.Column;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.simple.SimpleJdbcInsertOperations;
import org.springframework.stereotype.Repository;

@Repository
public class PushDataDAOImpl implements PushDataDAO {

    private static final Logger LOG = LoggerFactory.getLogger(PushDataDAO.class);

    private static final String METADATA = "metadata";

    private static final String ID = "id";

    private static final String NAME = "name";

    private static final String DESCRIPTION = "description";

    private static final String FIELD = "field";

    private static final String TYPE = "type";

    private static final String NULL = "null";

    private static final String KEY = "key";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void insertMetaData(final MetadataInfo metadata) {
        LOG.info("Inserting Metadata infornmation");

        SimpleJdbcInsertOperations insert = new SimpleJdbcInsert(jdbcTemplate).
                withTableName(METADATA).
                usingColumns(ID, NAME, DESCRIPTION);

        Map<String, String> args = new HashMap<>();
        args.put(ID, metadata.getId());
        args.put(NAME, metadata.getName());
        args.put(DESCRIPTION, metadata.getDescription());

        int number = insert.execute(args);
        if (number == 1) {
            jdbcTemplate.execute("CREATE TABLE " + metadata.getName());
        }

        LOG.info("Number of rows affected: {} ", number);
    }

    @Override
    public Optional<String> findMetaData(final String column, final String value) {
        String name = null;
        try {
            name = jdbcTemplate.queryForObject(
                    "SELECT name FROM metadata WHERE " + column + "= ?",
                    new Object[] { value },
                    String.class);
        } catch (IncorrectResultSizeDataAccessException e) {
            LOG.debug("Expected to find exactly one metadata with {} = {}", column, value, e);
        }

        return Optional.ofNullable(name);
    }

    @Override
    public void createColumn(final String tableName, final Column column) {
        String sql = "ALTER TABLE " + tableName + " ADD " + column.getName()
                + " " + transformType(column.getDataTypeName());
        jdbcTemplate.execute(sql);

    }

    @Override
    public List<TableColumn> findTableColumns(String tableName) {
        return jdbcTemplate.query(
                "SHOW COLUMNS FROM " + tableName,
                (rs, rownumber) -> build(rs));
    }

    @Override
    public void insertData(String data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private String transformType(final String type) {
        switch (type) {
            case "number":
                return "DOUBLE";
            case "checkbox":
                return "BOOLEAN";
            case "calendar_date":
                return "DATE";
            case "date":
                return "DATETIME";
            case "text":
                return "VARCHAR (255)";
        }
        return null;
    }

    private TableColumn build(final ResultSet rs) throws SQLException {
        TableColumn column = new TableColumn();
        column.setField(rs.getString(FIELD));
        column.setType(rs.getString(TYPE));
        column.setNullable(rs.getString(NULL));
        column.setKey(rs.getString(KEY));

        return column;
    }
}
