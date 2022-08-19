package top.yifan;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.Assert;

/**
 * MD5Util
 *
 * @author Star Zheng
 */
public class MD5Util {

    private MD5Util() {
    }

    public static String toMD5(String... strings) {
        Assert.notNull(strings, "Original string is required");
        String finalString = String.join("", strings);
        return DigestUtils.md5Hex(finalString);

    }

}
