package top.yifan.validator;

import com.itextpdf.text.pdf.PdfReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.IOException;

/**
 * PDFValidator
 *
 * @author zhengyifan
 */
@Slf4j
class PDFValidator implements FileValidator {

    @Override
    public boolean isValid(byte[] fileData) {
        if (fileData == null) {
            return false;
        }
        // 打开 PDF 文件
        try (PDDocument document = PDDocument.load(fileData)) {
            int numberOfPages = document.getNumberOfPages();
            return numberOfPages > 0;
        } catch (IOException e) {
            // 如果发生异常，说明 PDF 文件无法正常打开
            log.error("PDF文件无法正常打开, message: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public FileType getType() {
        return FileType.PDF;
    }


    public static boolean isValid2(byte[] fileData) {
        if (fileData == null) {
            return false;
        }
        PdfReader reader = null;
        try {
            reader = new PdfReader(fileData);
            int numberOfPages = reader.getNumberOfPages();

            return numberOfPages > 0;
        } catch (IOException e) {
            log.error("PDF文件无法正常打开, message: {}", e.getMessage(), e);
            return false;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
}
