package top.yifan.plugins;

import org.apache.commons.lang3.StringUtils;

/**
 * PhoneSensitiveStrategy
 *
 * @author zhengyifan
 */
class PhoneSensitiveStrategy implements SensitiveStrategy {

    @Override
    public SensitiveType getType() {
        return SensitiveType.PHONE;
    }

    @Override
    public String desensitize(String value) {
        if (StringUtils.isEmpty(value)) {
            return value;
        }
        return value.replaceAll("(\\d{3})\\d{4}(\\d+)", "$1****$2");
    }
}
