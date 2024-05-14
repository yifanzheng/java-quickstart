package top.yifan;

import top.yifan.exception.ZipException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * ZipUtil
 *
 * @author Star Zheng
 */

public class ZipUtil {

    private ZipUtil() {
    }

    /**
     * 压缩文件
     *
     * @param fileName     被压缩的文件名，包含后缀
     * @param srcFileBytes 被压缩的文件二进制内容
     * @return 返回压缩后的文件二进制内容
     */
    public static byte[] compress(String fileName, byte[] srcFileBytes) {
        try (ByteArrayOutputStream arrayOut = new ByteArrayOutputStream();
             ZipOutputStream zipOut = new ZipOutputStream(arrayOut)) {
            // 设置压缩文件名称：名称必须包含文件后缀.zip
            zipOut.putNextEntry(new ZipEntry(fileName));
            zipOut.write(srcFileBytes);
            zipOut.closeEntry();
            zipOut.finish();

            return arrayOut.toByteArray();
        } catch (IOException e) {
            throw new ZipException(String.format("文件[%s]压缩ZIP失败", fileName), e);
        }
    }

    /**
     * 解压文件
     *
     * @param zipFileBytes zip压缩文件二进制内容
     * @return 返回解压后的二进制文件内容
     * @throws IOException
     */
    public static byte[] uncompress(byte[] zipFileBytes) {
        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(zipFileBytes));
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ZipEntry nextEntry = zipInputStream.getNextEntry();
            // 文件名，包含文件后缀
            // String fileName = nextEntry.getName();

            byte[] bytes = new byte[1024];
            int readLen;
            while ((readLen = zipInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, readLen);
            }
            zipInputStream.closeEntry();

            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new ZipException("解压ZIP文件失败", e);
        }
    }
}
