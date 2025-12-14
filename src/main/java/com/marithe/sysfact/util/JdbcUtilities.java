package py.com.ventasjdbc.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public final class JdbcUtilities {

	public static boolean doesColumnExist(String columnName, ResultSet rs) throws SQLException {
		ResultSetMetaData meta = rs.getMetaData();
		int numCol = meta.getColumnCount();
		for (int i = 1; i <= numCol; i++) {
			if (meta.getColumnName(i).equalsIgnoreCase(columnName)) {
				return true;

			}

		}
		return false;
	}

}
