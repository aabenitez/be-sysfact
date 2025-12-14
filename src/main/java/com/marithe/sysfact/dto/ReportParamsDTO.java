package com.marithe.sysfact.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.marithe.sysfact.util.ReportUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ReportParamsDTO implements Serializable {
    @JsonProperty("filters")
    List<ReportParamDTO> filters;

    public ReportParamsDTO() {
    }

    public ReportParamsDTO(List<ReportParamDTO> filters) {
        this.filters = filters;
    }

    public List<ReportParamDTO> getFilters() {
        return filters;
    }

    public void setFilters(List<ReportParamDTO> filters) {
        this.filters = filters;
    }

    public Map<String, Object> toMap() {
        return ReportUtils.parseParameters(this);
    }

    @Override
    public String toString() {
        return "ReportParamsDTO{" +
                "params=" + filters +
                '}';
    }
}
