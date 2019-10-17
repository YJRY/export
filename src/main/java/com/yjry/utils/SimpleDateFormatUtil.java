package com.yjry.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * simpleDateFormat工具类
 * @author xuqi
 * @date 2019-10-15 16:47:38
 */
public class SimpleDateFormatUtil {
    private volatile static SimpleDateFormat simpleDateFormat;

    /**
     * 获取SimpleDateFormat单例对象
     * @author xuqi
     * @date 2019-10-17 14:12:44
     */
    private static SimpleDateFormat getDefaultInstance() {
        if (simpleDateFormat == null) {
            synchronized (SimpleDateFormatUtil.class) {
                if (simpleDateFormat == null) {
                    simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                }
            }
        }
        return simpleDateFormat;
    }

    /**
     * 根据传入的字符串获取SimpleDateFormat对象
     * @author xuqi
     * @date 2019-10-17 14:12:02
     */
    public static SimpleDateFormat getInstanceByValue(String value) {
        return new SimpleDateFormat(value);
    }

    /**
     * 将传入的Date对象转换为对应时间串
     * @author xuqi
     * @date 2019-10-17 14:10:49
     */
    public static String getDateTimeString(Date date) {
        return getDefaultInstance().format(date);
    }

    /**
     * 将传入的毫秒值转换为对应时间串
     * @author xuqi
     * @date 2019-10-17 14:08:28
     */
    public static String getDateTimeString(Long l) {
        return getDefaultInstance().format(l);
    }

}
