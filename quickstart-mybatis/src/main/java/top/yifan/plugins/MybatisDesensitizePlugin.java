package top.yifan.plugins;

import com.mysql.jdbc.Statement;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * MybatisDesensitizePlugin
 *
 * @author zhengyifan
 */
@Intercepts({@Signature(
        type = ResultSetHandler.class,
        method = "handleResultSets",
        args = {Statement.class}
)})
public class MybatisDesensitizePlugin implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object result = invocation.proceed();
        Class<?> resultClass = result.getClass();
        if (Collection.class.isAssignableFrom(resultClass)) {
            Collection<?> resultList = (Collection<?>) result;
            if (CollectionUtils.isEmpty(resultList)) {
                return resultList;
            }
            resultList.forEach(this::sensitiveData);
        } else {
            sensitiveData(result);
        }
        return result;
    }

    private void sensitiveData(Object data) {
        if (Objects.isNull(data)) {
            return;
        }
        Class<?> dataClass = data.getClass();
        MetaObject metaObject = SystemMetaObject.forObject(data);
        Field[] declaredFields = dataClass.getDeclaredFields();
        Stream.of(declaredFields)
                .filter(field -> field.isAnnotationPresent(Sensitive.class))
                .forEach(field -> {
                    String fieldName = field.getName();
                    if (Objects.equals(String.class, metaObject.getGetterType(fieldName))) {
                        Sensitive sensitive = field.getAnnotation(Sensitive.class);
                        SensitiveStrategy sensitiveStrategy = SensitiveStrategyFactory.getStrategy(sensitive.type());
                        String desensitize = sensitiveStrategy.desensitize((String) metaObject.getValue(fieldName));
                        // 设置脱敏后的值
                        metaObject.setValue(fieldName, desensitize);
                    }
                });
    }

}
