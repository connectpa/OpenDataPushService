package it.connectpa.odatapushservice.dao;

import it.connectpa.odatapushservice.model.MetadataInfo;
import it.connectpa.odatapushservice.model.TableColumn;
import it.connectpa.odatapushservice.server.model.Column;
import java.util.List;
import java.util.Optional;

public interface PushDataDAO {

    void insertMetaData(MetadataInfo metadata);

    Optional<String> findMetaData(String column, String value);

    void createColumn(String tableName, Column column);

    List<TableColumn> findTableColumns(String tableName);

    void insertData(String query, List<String[]> batchList);

}
