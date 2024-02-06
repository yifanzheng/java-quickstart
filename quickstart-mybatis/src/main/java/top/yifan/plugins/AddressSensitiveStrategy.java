package top.yifan.plugins;

import org.apache.commons.lang3.StringUtils;

/**
 * AddressSensitiveStrategy
 *
 * @author zhengyifan
 */
class AddressSensitiveStrategy implements SensitiveStrategy {

    @Override
    public SensitiveType getType() {
        return SensitiveType.ADDRESS;
    }

    @Override
    public String desensitize(String value) {
        if (StringUtils.isEmpty(value)) {
            return value;
        }
        return value.replaceAll("[(a-zA-Z0-9一二三四五六七八九十)]","*");
    }
}
