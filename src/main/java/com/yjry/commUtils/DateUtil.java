package com.yjry.commUtils;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * date工具类
 * @author xuqi
 * @date 2019-10-15 16:47:58
 */
public class DateUtil {
    private static final String DEFAULT_DATETIME = "yyyy-MM-dd HH:mm:ss";
    private static final String DEFAULT_DATETIME_NULL = "yyyyMMddHHmmss";
    private static final String DEFAULT_DATETIME_S = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final String DEFAULT_DATE = "yyyy-MM-dd";
    private static final String DEFAULT_MONTH = "yyyy-MM";


    public static Calendar getCalendar() {
        return Calendar.getInstance();
    }

    /**
     * 获取当前时间
     *
     * @return
     */
    public static Date getCurrentTime() {
        return getCalendar().getTime();
    }

    public static Calendar getCalendar(String strDate) {
        Calendar calendar = getCalendar();
        try {
            calendar.setTime(parse(strDate));
        } catch (ParseException e) {
        }
        return calendar;
    }

    public static Calendar getCalendar(String strDate, String format) {
        Calendar calendar = getCalendar();
        try {
            calendar.setTime(parse(strDate, format));
        } catch (ParseException e) {
        }
        return calendar;
    }

    public static String format(Date date) {
        return format(date, DEFAULT_DATETIME);
    }

    public static String format(long date) {
        return format(date, DEFAULT_DATETIME);
    }

    public static String format(Calendar date) {
        return format(date, DEFAULT_DATETIME);
    }

    public static String format(Date date, String pattern) {
        return DateFormatUtils.format(date, pattern);
    }

    public static String format(long date, String pattern) {
        return DateFormatUtils.format(date, pattern);
    }

    public static String format(Calendar date, String pattern) {
        return DateFormatUtils.format(date, pattern);
    }

    public static Date parse(String strDate) throws ParseException {
        return parse(strDate, DEFAULT_DATETIME);
    }

    public static Date parse(String strDate, String pattern) throws ParseException {
        return DateUtils.parseDate(strDate, pattern);
    }

    public static Integer getWeekDay(String datetime, String pattern) {
        Calendar calendar = getCalendar();
        try {
            calendar.setTime(parse(datetime, pattern));
        } catch (ParseException e) {
            return 0;
        }
        int day = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        return day % Calendar.DAY_OF_WEEK == 0 ? Calendar.DAY_OF_WEEK : day;
    }
}
