package it.connectpa.odataservice.dao.impl;

import it.connectpa.odataservice.model.MetadataInfo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import it.connectpa.odataservice.dao.ODataDAO;
import it.connectpa.odataservice.model.EntityProperty;
import org.springframework.stereotype.Repository;

@Repository
public class ODataDAOImpl implements ODataDAO {

    private static final Logger LOG = LoggerFactory.getLogger(ODataDAOImpl.class);

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
    public List<MetadataInfo> findAllEntities() {
        return jdbcTemplate.query("SELECT * FROM metadata",
                (rs, rownumber) -> build(rs));
    }

    @Override
    public List<EntityProperty> findProperties(final String entity) {
        return jdbcTemplate.query(
                "SHOW COLUMNS FROM " + entity,
                (rs, rownumber) -> buildProperty(rs));
    }

    private MetadataInfo build(final ResultSet rs) throws SQLException {
        MetadataInfo entityType = new MetadataInfo();
        entityType.setId(rs.getString(ID));
        entityType.setName(rs.getString(NAME));
        entityType.setDescription(rs.getString(DESCRIPTION));

        return entityType;
    }

    private EntityProperty buildProperty(final ResultSet rs) throws SQLException {
        EntityProperty property = new EntityProperty();
        property.setField(rs.getString(FIELD));
        property.setType(rs.getString(TYPE));
        property.setNullable(rs.getString(NULL));
        property.setKey(rs.getString(KEY));

        return property;
    }

}
