package top.yifan;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import org.apache.commons.io.IOUtils;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import top.yifan.exception.NotFoundException;
import top.yifan.exception.ServiceException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * ExcelUtilX
 *
 * @author Star Zheng
 */
public class ExcelUtilX {

    private ExcelUtilX() {

    }

    /**
     * 写出到客户端下载
     *
     * @param response          客戶端响应对象
     * @param excelFileTemplate Excel 文件模板
     * @param data              数据
     */
    public static void flushData2ServletResponse(HttpServletResponse response, String excelFileTemplate, List<List<String>> data) {
        Optional<byte[]> excelFileOpt = ExcelUtilX.getExcelFileTemplateAsByte(excelFileTemplate);
        if (!excelFileOpt.isPresent()) {
            throw new NotFoundException("Not found excel template file");
        }
        // 生成Excel设置压缩率为零，不然会在生成文件的时候报相关异常
        ZipSecureFile.setMinInflateRatio(0);
        ByteArrayInputStream excelFileTemplateInStream = new ByteArrayInputStream(excelFileOpt.get());
        // 拿到writer并且设置从第二行开始写入
        ExcelWriter writer = ExcelUtil.getReader(excelFileTemplateInStream).getWriter();
        ServletOutputStream out = null;
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=case.xlsx");
            out = response.getOutputStream();

            writer.setSheet(0);
            writer.setCurrentRow(1);
            writer.write(data);
            writer.flush(out, true);
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        } finally {
            // 关闭两个流
            try {
                IOUtils.close(out, excelFileTemplateInStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            writer.close();
        }
    }

    /**
     * 以二进制形式获取Excel文件模板
     *
     * @param excelFileName Excel文件名
     * @return Optional<byte [ ]>
     */
    public static Optional<byte[]> getExcelFileTemplateAsByte(String excelFileName) {
        InputStream fileStream = null;
        ByteArrayOutputStream byteOutStream = null;
        try {
            // 读取文件流
            fileStream = ClassLoader.getSystemResourceAsStream("excel-template/" + excelFileName + ".xlsx");
            // 创建字节输出流
            byteOutStream = new ByteArrayOutputStream();
            // 将输入流拷贝到字节输出流中
            IOUtils.copy(fileStream, byteOutStream);
            byte[] fileBytes = byteOutStream.toByteArray();

            return Optional.of(fileBytes);
        } catch (IOException e) {
            return Optional.empty();
        } finally {
            try {
                IOUtils.close(byteOutStream, fileStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
