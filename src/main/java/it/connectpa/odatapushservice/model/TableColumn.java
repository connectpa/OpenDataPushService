package it.connectpa.odatapushservice.model;

public class TableColumn {

    String Field;

    String Type;

    String nullable;

    String key;

    public String getField() {
        return Field;
    }

    public void setField(final String Field) {
        this.Field = Field;
    }

    public String getType() {
        return Type;
    }

    public void setType(final String Type) {
        this.Type = Type;
    }

    public String getNullable() {
        return nullable;
    }

    public void setNullable(final String nullable) {
        this.nullable = nullable;
    }

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "TableColumn{" + "Field=" + Field + ", Type=" + Type + ", nullable=" + nullable + ", key=" + key + '}';
    }

}
