package top.yifan.plugins;

import org.apache.commons.lang3.StringUtils;

/**
 * UserNameSensitiveStrategy
 *
 * @author zhengyifan
 */
class UserNameSensitiveStrategy implements SensitiveStrategy {

    @Override
    public SensitiveType getType() {
        return SensitiveType.USERNAME;
    }

    @Override
    public String desensitize(String value) {
        if (StringUtils.isEmpty(value)) {
            return value;
        }
        return null;
    }
}
