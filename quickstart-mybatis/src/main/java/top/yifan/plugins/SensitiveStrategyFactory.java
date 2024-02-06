package top.yifan.plugins;

import org.reflections.Reflections;
import org.springframework.util.CollectionUtils;
import top.yifan.UnsupportedTypeException;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * SensitiveStrategyFactory
 *
 * @author zhengyifan
 */
public class SensitiveStrategyFactory {

    private static final String BASE_PACKAGE = "top.yifan.plugins";

    private static final Map<SensitiveType, SensitiveStrategy> SENSITIVE_STRATEGY_MAP = new EnumMap<>(SensitiveType.class);

    private SensitiveStrategyFactory() {
    }

    static {
        registerSensitiveStrategy();
    }

    public static SensitiveStrategy getStrategy(SensitiveType type)  {
        SensitiveStrategy sensitiveStrategy = SENSITIVE_STRATEGY_MAP.get(type);
        if (Objects.isNull(sensitiveStrategy)) {
            throw new UnsupportedTypeException("Unsupported sensitive strategy type: " + type.name());
        }
        return sensitiveStrategy;
    }

    private static void registerSensitiveStrategy() {
        Reflections reflections = new Reflections(BASE_PACKAGE);
        Set<Class<? extends SensitiveStrategy>> classSet = reflections.getSubTypesOf(SensitiveStrategy.class);
        if (CollectionUtils.isEmpty(classSet)) {
            return;
        }
        classSet.forEach(v -> {
            try {
                SensitiveStrategy sensitiveStrategy = v.newInstance();
                SENSITIVE_STRATEGY_MAP.put(sensitiveStrategy.getType(), sensitiveStrategy);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Sensitive strategy load error");
            }
        });
    }

}
