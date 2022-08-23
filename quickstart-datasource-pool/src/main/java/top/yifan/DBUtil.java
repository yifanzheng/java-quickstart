package top.yifan;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * DBUtil
 *
 * @author Star Zheng
 */
public class DBUtil {

    private static final Logger log = LoggerFactory.getLogger(DBUtil.class);

    private DBUtil() {

    }

    public static void closeDBResources(AutoCloseable... closeables) {
        if (ArrayUtils.isEmpty(closeables)) {
            return;
        }
        for (AutoCloseable e : closeables) {
            if (e != null) {
                try {
                    e.close();
                } catch (Exception e1) {
                    log.warn("Close DB resource failed, message: {}", e1.getMessage());
                }
            }
        }
    }

    public static void cancelStatement(Statement... stmts) {
        if (ArrayUtils.isEmpty(stmts)) {
            return;
        }
        for (Statement stmt : stmts) {
            try {
                if (stmt != null && !stmt.isClosed()) {
                    stmt.cancel();
                }
            } catch (SQLException e) {
                log.warn("Cancel DB statement failed, message: {}", e.getMessage());
            }
        }
    }
}
