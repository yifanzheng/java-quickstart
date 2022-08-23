package top.yifan;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * GetConnectionSupplier
 * 
 * @author kt94
 */
@FunctionalInterface
public interface GetConnectionSupplier {

    /**
     * @return 返回数据库连接
     * @throws SQLException
     */
    Connection get() throws SQLException;

}
