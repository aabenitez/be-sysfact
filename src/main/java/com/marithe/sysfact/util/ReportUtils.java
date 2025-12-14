package py.com.ventasjdbc.util;

import py.com.ventasjdbc.dto.ReportParamDTO;
import py.com.ventasjdbc.dto.ReportParamsDTO;

import java.util.HashMap;
import java.util.Map;

public class ReportUtils {

    public static Map<String, Object> parseParameters(ReportParamsDTO params) {
        Map<String, Object> map = new HashMap<>();
        for (ReportParamDTO param : params.getFilters()) {
            Object parsedParam = ReportUtils.parseValue(param);
            if (parsedParam != null) {
                map.put(param.getField(), parsedParam);
            }
        }
        return map;
    }

    public static Object parseValue(ReportParamDTO param) {
        try {
            if (param.getType().equalsIgnoreCase(ReportParamType.NUMBER) || param.getType().equalsIgnoreCase(ReportParamType.SELECT)) {
                return Integer.valueOf(String.valueOf(param.getValue()));
            } else if (param.getType().equalsIgnoreCase(ReportParamType.DATE)) {
                return java.sql.Date.valueOf(DateUtils.defaultFormat(String.valueOf(param.getValue())));
            } else if (param.getType().equalsIgnoreCase(ReportParamType.TEXT)) {
                return String.valueOf(param.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
