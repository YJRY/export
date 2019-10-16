package com.yjry.commUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * simpleDateFormat工具类
 * @author xuqi
 * @date 2019-10-15 16:47:38
 */
public class SimpleDateFormatUtil {
    private volatile static SimpleDateFormat simpleDateFormat;

    public static SimpleDateFormat getDefaultInstance() {
        if (simpleDateFormat == null) {
            synchronized (SimpleDateFormatUtil.class) {
                if (simpleDateFormat == null) {
                    simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                }
            }
        }
        return simpleDateFormat;
    }

    public static SimpleDateFormat getInstanceByValue(String value){
        return new SimpleDateFormat(value);
    }

    public static String formatDate(Date date) {
        return getDefaultInstance().format(date);

    }

    public static Date parse(String strDate) throws ParseException {

        return getDefaultInstance().parse(strDate);

    }

    public static String formatLong(Long l) {

        return getDefaultInstance().format(l);

    }

    public static Integer getWeekDay(String datetime) {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date parse = f.parse(datetime);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(parse);
            switch (calendar.get(Calendar.DAY_OF_WEEK)) {
                case Calendar.MONDAY:
                    return 1;
                case Calendar.TUESDAY:
                    return 2;
                case Calendar.WEDNESDAY:
                    return 3;
                case Calendar.THURSDAY:
                    return 4;
                case Calendar.FRIDAY:
                    return 5;
                case Calendar.SATURDAY:
                    return 6;
                case Calendar.SUNDAY:
                    return 7;
                default:
                    return 0;
            }

        } catch (ParseException e) {
            return 0;
        }
    }
}
