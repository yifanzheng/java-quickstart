package top.yifan.datasource;

import org.apache.commons.lang3.StringUtils;

/**
 * RDBMSType
 *
 * @author Star Zheng
 */
public enum RDBMSType {

    MySQL, SQLServer;

    RDBMSType of(String type) {
        for (RDBMSType value : RDBMSType.values()) {
            if (StringUtils.equalsIgnoreCase(value.toString(), type)) {
                return value;
            }
        }
        throw new IllegalArgumentException("No enum constant " + getClass().getName() + "." + type);
    }

}
