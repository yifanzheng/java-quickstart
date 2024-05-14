package top.yifan;


import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author zhengyifan
 */
public class DateUtils {

    private DateUtils() {
    }

    /**
     * 获取按天拆分的时间区间集合
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 返回按天拆分的时间区间集合
     */
    public static List<Pair<Date, Date>> getIntervalDateByDaily(Date start, Date end) {
        List<Pair<Date, Date>> dateIntervalList = Lists.newArrayList();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (calendar.getTimeInMillis() < end.getTime()) {
            Date leftDate = calendar.getTime();

            // 设置一天的结束时间
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            Date rightDate = new Date(end.getTime());
            if (calendar.getTimeInMillis() < end.getTime()) {
                rightDate = calendar.getTime();
            }
            dateIntervalList.add(Pair.of(leftDate, rightDate));
            // 将时间加一天
            calendar.add(Calendar.DATE, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
        }

        return dateIntervalList;
    }
}
