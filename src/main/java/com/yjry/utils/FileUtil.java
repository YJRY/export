package com.yjry.utils;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.*;
import com.yjry.bean.ExportBean;
import com.yjry.bean.exception.GlobalException;
import com.yjry.common.GlobalProperty;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.DateUtil;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 文件工具类
 * @author xuqi
 * @date 2019-10-15 16:48:11
 */
public class FileUtil {

    public static String commonCSVExport(ExportBean exportBean, HttpServletRequest request, Object daoBean, String loginName, String downloadFilePath, String mappingPath) throws IllegalAccessException, InstantiationException {
        exportBean.setFileName(exportBean.getFileName() + "_"
                + SimpleDateFormatUtil.getInstanceByValue(GlobalProperty.DATETIME_NON_JOINER).format(new Date()) + "_" + loginName);
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
                        return exportCSV(list, exportBean.getColumnNames(), request, exportBean.getFileName() + ".csv", downloadFilePath, mappingPath);
                    }
                }
            }
        }
        return exportCSV(null, null, request, exportBean.getFileName() + ".csv", downloadFilePath, mappingPath);
    }

    /**
     * 导出CSV文件
     * @author xuqi
     * @date 2019-07-31 10:35:35
     */
    private static String exportCSV(List list, List<Map<String, String>> columnNames, HttpServletRequest request, String fileName, String downloadFilePath, String mappingPath) {
        BufferedWriter out = null;
        String path = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        try {
            File filePath = new File(downloadFilePath);
            if (!filePath.exists()) {
                filePath.mkdirs();
            }
            File csvFile = new File(downloadFilePath + fileName);
            if (!csvFile.exists()) {
                csvFile.createNewFile();
            }
            // UTF-8使正确读取分隔符","
            byte[] uft8bom = {(byte) 0xef, (byte) 0xbb, (byte) 0xbf};
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile), StandardCharsets.UTF_8), 1024);
            out.write(new String(uft8bom));
            if (CollectionUtils.isEmpty(list)) {
                out.write("导出数据为空");
            } else {
                List<String> columnKeys = new ArrayList<>();
                List<String> columnValues = new ArrayList<>();
                columnNames.forEach(columnMap -> {
                    columnKeys.add(columnMap.get("title"));
                    columnValues.add(columnMap.get("key"));
                });
                for (String key : columnKeys) {
                    out.write(key);
                    out.write(",");
                }
                out.newLine();
                // 写入文件内容
                for (Object object : list) {
                    Class objectClass = object.getClass();
                    for (String value : columnValues) {
                        Field field = objectClass.getDeclaredField(value);
                        if (field != null) {
                            field.setAccessible(true);
                            String typeName = field.getGenericType().getTypeName();
                            if (typeName.equals(Date.class.getTypeName())) {
                                out.write(field.get(object) == null ? "," : "\"" + SimpleDateFormatUtil.formatDate((Date) field.get(object)) + "\t\",");
                            } else if (typeName.equals(String.class.getTypeName())) {
                                String newFieldValue = handleString((String) field.get(object));
                                out.write(newFieldValue == null ? "," : newFieldValue + ",");
                            } else {
                                out.write(field.get(object) == null ? "," : field.get(object) + ",");
                            }
                        }
                    }
                    out.newLine();
                }
            }
            out.flush();
            System.out.println(csvFile.exists());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return path + mappingPath + fileName;
    }

    /**
     * 处理字符串类型数据，防止逗号分隔
     * @author xuqi
     * @date 2019-07-31 13:19:07
     */
    private static String handleString(String originalStr) {
        if (originalStr == null) {
            return null;
        }
        if (originalStr.contains("\"")) {
            originalStr = originalStr.replace("\"", "\"\"");
        }
        if (Pattern.matches("^[0-9]{11,}$", originalStr)) {
            originalStr += "\t";
        }
        return "\"" + originalStr + "\"";
    }

    /**
     * 获取excel数据
     * @author xuqi
     * @date 2019-09-10 20:57:16
     */
    public static DataResult getExcelData(MultipartFile file) {
        Sheet sheet;
        Row row;
        String cellData;
        Map<Integer, Map<Integer, String>> dataMap = new HashMap<>();
        DataResult result = new DataResult();
        try {
            Workbook workbook = WorkbookFactory.create(file.getInputStream());
            if (workbook != null) {
                //获取第二个sheet
                if (workbook.getNumberOfSheets() < 2) {
                    throw new GlobalException("该文件内容不符合要求");
                }
                sheet = workbook.getSheetAt(1);
                //获取行数
                int rowNum = sheet.getLastRowNum();
                if (rowNum > 0) {
                    //获取第一行
                    row = sheet.getRow(0);
                    //获取最大列数
                    int colNum = row.getPhysicalNumberOfCells();
                    for (int i = 1; i <= rowNum; i++) {
                        Map<Integer, String> map = new HashMap<>();
                        row = sheet.getRow(i);
                        if (row != null) {
                            for (int j = 0; j < colNum; j++) {
                                cellData = getCellFormatValue(row.getCell(j));
                                if (StringUtils.isNotEmpty(cellData)) {
                                    map.put(j, cellData);
                                }
                            }
                        } else {
                            continue;
                        }
                        if (MapUtils.isNotEmpty(map)) {
                            dataMap.put(i + 1, map);
                        }
                    }
                }
            }
        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
        }
        if (MapUtils.isNotEmpty(dataMap)) {
            result.setStatus(0);
            result.setSuccessData(dataMap);
            return result;
        } else {
            return null;
        }
    }

    /**
     * 获取单元格内容
     * @author xuqi
     * @date 2019-10-15 17:20:35
     */
    private static String getCellFormatValue(Cell cell) {
        String cellValue = "";
        if (cell != null) {
            //判断cell类型
            switch (cell.getCellTypeEnum()) {
                case NUMERIC:
                    //若为日期格式
                    if (DateUtil.isCellDateFormatted(cell)) {
                        cellValue = SimpleDateFormatUtil.formatDate(cell.getDateCellValue());
                    } else {
                        BigDecimal decimalCellValue = new BigDecimal(cell.getNumericCellValue());
                        cellValue = decimalCellValue.toString();
                        if (null != cellValue && !"".equals(cellValue.trim()) && cellValue.trim().endsWith(".0")) {
                            cellValue = cellValue.substring(0, cellValue.length() - 2);
                        }
                    }
                    break;
                //公式
                case FORMULA:
                    //数字
                    cellValue = String.valueOf(cell.getNumericCellValue());
                    break;
                case STRING:
                    cellValue = cell.getRichStringCellValue().getString();
                    break;
                default:
                    cellValue = "";
            }
        }
        return cellValue == null ? "" : cellValue.trim().replaceAll("\\n", "");
    }

    /**
     * 导出pdf
     * @author xuqi
     * @date 2019-10-15 17:01:21
     */
    public static String exportPdf(String templateFileName, String staticTemplateFilePath, Integer pageNum, HttpServletRequest request, List<Map<String, Object>> list, String fileName, String loginName, String downloadFilePath) {
        FileOutputStream out = null;
        String downloadPath = "/downloadFile/";
        String path = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        StringBuilder sb = new StringBuilder();
        fileName = sb.append(fileName).append("_").append(SimpleDateFormatUtil.getInstanceByValue(GlobalProperty.DATETIME_NON_JOINER).format(new Date())).append("_").append(loginName).append(".pdf").toString();
        String newFilePath = downloadFilePath + "/" + fileName;
        String templateFilePath = staticTemplateFilePath + "/" + templateFileName;
        try {
            File fileDir = new File(downloadFilePath);
            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }
            File pdfFile = new File(newFilePath);
            if (!pdfFile.exists()) {
                pdfFile.createNewFile();
            }
            ByteArrayOutputStream[] bosOut = new ByteArrayOutputStream[list.size()];
            // 输出流
            out = new FileOutputStream(newFilePath);
            // 新建一个文档
            Document doc = new Document();
            // 用于保存原页面内容,然后输出
            PdfCopy copy = new PdfCopy(doc, out);
            doc.open();
            PdfStamper stamper;
            AcroFields form;
            PdfImportedPage page;
            BaseFont bf = BaseFont.createFont(staticTemplateFilePath + "/simsun.ttc,1", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);//加载字体
            Font font = new Font(bf, 12, Font.NORMAL);
            PdfReader reader;
            for (int i = 0; i < list.size(); i++) {
                reader = new PdfReader(templateFilePath);
                bosOut[i] = new ByteArrayOutputStream();
                stamper = new PdfStamper(reader, bosOut[i]);
                form = stamper.getAcroFields();
                for (String name : form.getFields().keySet()) {
                    form.setFieldProperty(name, "textfont", bf, null);//设置字体
                    form.setField(name, list.get(i).containsKey(name) ? list.get(i).get(name).toString() : "");
                }
                stamper.setFormFlattening(true);
                stamper.close();
                for (int j = 1; j < pageNum + 1; j++) {
                    page = copy.getImportedPage(new PdfReader(bosOut[i].toByteArray()), j);
                    copy.addPage(page);
                }
            }
            doc.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return path + downloadPath + fileName;
    }

    /**
     * excel解析结果类
     * @author xuqi
     * @date 2019-10-15 17:20:42
     */
    public static class DataResult {
        private Integer status;//解析状态 0-成功 1-失败 2- 无数据
        private int successCount = 0;//解析成功条数
        private int failCount = 0;//解析失败条数
        private String resultText;//解析结果文本信息
        @JsonSerialize(using = ToStringSerializer.class)
        private Long resultId;//解析结果唯一id
        private Map<Integer, Map<Integer, String>> successData = new HashMap<>();//正常数据
        private List<String> wrongData = new ArrayList<>();//异常数据

        DataResult() {
        }

        public static DataResult success(Map<Integer, Map<Integer, String>> successData) {
            DataResult dataResult = new DataResult();
            dataResult.setStatus(0);
            dataResult.setSuccessData(successData);
            return dataResult;
        }

        public void handleResult(Map<Integer, Map<Integer, String>> resultData, List<String> wrongData, int wrongCount) {
            this.setSuccessData(resultData);
            this.setWrongData(wrongData);
            this.setSuccessCount(this.getSuccessData().size());
            this.setFailCount(wrongCount);
            String resultText = "本次解析共" + (this.getSuccessCount() + this.getFailCount()) + "条，解析成功"
                    + this.getSuccessCount() + "条，解析失败" + this.getFailCount() + "条。";
            this.setResultText(resultText);
            this.setResultId(resultId);
            if (this.getFailCount() > 0) {
                this.setStatus(1);
            } else {
                this.resultId = new SnowFlakeIdUtil(8, 8).nextId();//解析结果id
            }
        }

        public static DataResult error(Integer status) {
            DataResult dataResult = new DataResult();
            dataResult.setStatus(status);
            dataResult.setResultText("该文件无数据");
            dataResult.setWrongData(new ArrayList<>());
            return dataResult;
        }

        public void clearAllData() {
            this.successData = null;
            this.wrongData = new ArrayList<>();
        }

        public void clearSuccessData() {
            this.successData = null;
        }

        public Long getResultId() {
            return resultId;
        }

        public void setResultId(Long resultId) {
            this.resultId = resultId;
        }

        public List<String> getWrongData() {
            return wrongData;
        }

        public void setWrongData(List<String> wrongData) {
            this.wrongData = wrongData;
        }

        public String getResultText() {
            return resultText;
        }

        public void setResultText(String resultText) {
            this.resultText = resultText;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public Map<Integer, Map<Integer, String>> getSuccessData() {
            return successData;
        }

        public void setSuccessData(Map<Integer, Map<Integer, String>> successData) {
            this.successData = successData;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public void setSuccessCount(int successCount) {
            this.successCount = successCount;
        }

        public int getFailCount() {
            return failCount;
        }

        public void setFailCount(int failCount) {
            this.failCount = failCount;
        }
    }

}
