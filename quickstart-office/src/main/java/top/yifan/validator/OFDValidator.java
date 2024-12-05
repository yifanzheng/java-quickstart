package top.yifan.validator;


import lombok.extern.slf4j.Slf4j;
import org.ofdrw.reader.DLOFDReader;
import org.ofdrw.reader.OFDReader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * OFDValidator
 *
 * @author zhengyifan
 */
@Slf4j
class OFDValidator implements FileValidator {

    @Override
    public boolean isValid(byte[] fileData) {
        if (fileData == null) {
            return false;
        }
        try (InputStream ofdInStream = new ByteArrayInputStream(fileData);
             OFDReader dlofdReader = new DLOFDReader(ofdInStream)) {
            int numberOfPages = dlofdReader.getNumberOfPages();
            return numberOfPages > 0;
        } catch (Exception e) {
            log.error("OFD文件校验异常, message: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public FileType getType() {
        return FileType.OFD;
    }
}
