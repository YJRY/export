package com.yjry.util.export;

import com.alibaba.fastjson.JSONObject;
import com.yjry.bean.ExportBean;
import com.yjry.commUtils.FileUtil;
import com.yjry.commUtils.SimpleDateFormatUtil;
import org.springframework.util.ReflectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Date;
import java.util.List;

/**
 * 导出CSV工具类
 * @author xuqi
 * @date 2019-10-15 16:33:10
 */
public class ExportCsvUtil {

    public static String commonCSVExport(ExportBean exportBean, HttpServletRequest request, Object daoBean, String loginName, String downloadFilePath) throws IllegalAccessException, InstantiationException {
        exportBean.setFileName(exportBean.getFileName() + "_"
                + SimpleDateFormatUtil.getInstanceByValue("yyyyMMddHHmmss").format(new Date()) + "_" + loginName);
        if (daoBean != null) {
            for (Method method : daoBean.getClass().getDeclaredMethods()) {
                if (method.getName().equals(exportBean.getMethodName())) {
                    for (Parameter parameter : method.getParameters()) {
                        Class clazz = parameter.getType();
                        Object object = clazz.newInstance();
                        JSONObject conditions = exportBean.getConditions();
                        if (conditions != null && conditions.size() > 0) {
                            conditions.forEach((k, v) -> {
                                try {
                                    Field field = clazz.getDeclaredField(k);
                                    if (field != null) {
                                        field.setAccessible(true);
                                        field.set(object, v);
                                    }
                                } catch (NoSuchFieldException | IllegalAccessException e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                        Object result = ReflectionUtils.invokeMethod(method, daoBean, clazz.cast(object));
                        List list = (List) result;
                        return FileUtil.exportCSV(list, exportBean.getColumnNames(), request, exportBean.getFileName() + ".csv", downloadFilePath);
                    }
                }
            }
        }
        return FileUtil.exportCSV(null, null, request, exportBean.getFileName() + ".csv", downloadFilePath);
    }

}
