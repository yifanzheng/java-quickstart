package top.yifan.plugins;

/**
 * SensitiveStrategy
 *
 * @author zhengyifan
 */
public interface SensitiveStrategy {

    SensitiveType getType();

    String desensitize(String value);

}
