package top.yifan;

import org.apache.commons.lang3.StringUtils;

/**
 * StringHelper
 *
 * @author Star Zheng
 */
public class StringHelper {

    private StringHelper() {
    }

    public static String normalize(String value, String nullOrEmptyValue) {
        if (isNullOrWhiteSpace(value)) return nullOrEmptyValue;
        return value;
    }

    public static boolean isNullOrWhiteSpace(String value) {
        if (value == null) return true;
        return StringUtils.isBlank(value);
    }

    public static String clearWhiteSpace(String value) {
        if (value == null) {
            return null;
        }
        return StringUtils.deleteWhitespace(value);
    }
}
