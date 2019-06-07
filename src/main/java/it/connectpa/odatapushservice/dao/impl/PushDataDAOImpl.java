package it.connectpa.odatapushservice.dao.impl;

import it.connectpa.odatapushservice.dao.PushDataDAO;
import it.connectpa.odatapushservice.model.MetadataInfo;
import it.connectpa.odatapushservice.server.model.Column;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    public String findMetaDataById(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Boolean ifExistMetaData(final String name) {
        Integer count =
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM metadata WHERE name = ?",
                        new Object[] { name },
                        Integer.class);

        return count > 0;
    }

    @Override
    public void createColumn(String tableName, Column column) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<String> findTableColumns(String tableName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void insertData(String data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
