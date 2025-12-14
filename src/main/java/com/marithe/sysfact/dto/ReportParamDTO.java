package com.marithe.sysfact.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class ReportParamDTO implements Serializable {
    @JsonProperty("field")
    private String field;
    @JsonProperty("value")
    private Object value;
    @JsonProperty("type")
    private String type;

    public ReportParamDTO() {
    }

    public ReportParamDTO(String field, Object value, String type) {
        this.field = field;
        this.value = value;
        this.type = type;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ReportParamDTO{" +
                "field='" + field + '\'' +
                ", value=" + value +
                ", type='" + type + '\'' +
                '}';
    }
}
