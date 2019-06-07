package it.connectpa.odatapushservice.dao;

import it.connectpa.odatapushservice.model.MetadataInfo;
import it.connectpa.odatapushservice.server.model.Column;
import java.util.List;

public interface PushDataDAO {

    void insertMetaData(MetadataInfo metadata);

    String findMetaDataById(String id);

    Boolean ifExistMetaData(String name);

    void createColumn(String tableName, Column column);

    List<String> findTableColumns(String tableName);

    void insertData(String data);

}
