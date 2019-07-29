package it.connectpa.odatapushservice.dao.impl;

import it.connectpa.odatapushservice.dao.PushDataDAO;
import it.connectpa.odatapushservice.model.MetadataInfo;
import it.connectpa.odatapushservice.model.TableColumn;
import it.connectpa.odatapushservice.server.model.Column;
import it.connectpa.odatapushservice.util.DateUtil;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
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

    private static final BidiMap TYPES;

    static {
        TYPES = new DualHashBidiMap();
        TYPES.put("number", "DOUBLE");
        TYPES.put("checkbox", "BOOLEAN");
        TYPES.put("calendar_date", "DATE");
        TYPES.put("date", "DATETIME");
        TYPES.put("text", "VARCHAR (255)");
    }

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
                + " " + TYPES.get(column.getDataTypeName());
        jdbcTemplate.execute(sql);

    }

    @Override
    public List<TableColumn> findTableColumns(String tableName) {
        return jdbcTemplate.query(
                "SHOW COLUMNS FROM " + tableName,
                (rs, rownumber) -> build(rs));
    }

    @Override
    public void insertData(final String query, final List<String[]> batchList) {
        jdbcTemplate.batchUpdate(query,
                new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                int index = 1;
                for (String line : batchList.get(i)) {
                    Date date = DateUtil.convertToDate(line);
                    if (null != date) {
                        ps.setDate(index++, new java.sql.Date(date.getTime()));
                    } else {
                        ps.setString(index++, line);
                    }
                }
            }

            @Override
            public int getBatchSize() {
                return batchList.size();
            }
        });
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
