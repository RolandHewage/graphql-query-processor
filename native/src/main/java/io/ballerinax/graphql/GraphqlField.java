package io.ballerinax.graphql;

import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

public class GraphqlField {
    private String name;
    private Type type;
    private BMap<BString, Object> arguments;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public BMap<BString, Object> getArguments() {
        return this.arguments;
    }

    public boolean isRecordType() {
        return (this.type instanceof RecordType);
    }

    public boolean isArrayType() {
        return (this.type instanceof ArrayType);
    }

    public GraphqlField(Field field) {
        this.name = field.getFieldName();
        this.type = field.getFieldType();
    }

    public GraphqlField(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public GraphqlField(String name, BMap<BString, Object> arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    public GraphqlField(Field field, BMap<BString, Object> arguments) {
        this.name = field.getFieldName();
        this.arguments = arguments;
    }

    public GraphqlField(String name) {
        this.name = name;
    }

    public GraphqlField() {
    }
}
