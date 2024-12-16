package top.yifan.word.doc4j;

import com.documents4j.api.DocumentType;
import com.documents4j.api.IConverter;
import com.documents4j.job.LocalConverter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * convert
 *
 * @author zhengyifan
 */
public class Word2PDF {

    /**
     * 转换为 PDF
     *
     * @param wordPath word 文件路径
     * @param pdfPath  pdf 文件路径
     */
    public void convertInWindows(String wordPath, String pdfPath) throws IOException {
        try (InputStream docxInputStream = Files.newInputStream(Paths.get(wordPath));
             OutputStream outputStream = Files.newOutputStream(Paths.get(pdfPath))) {
            // 构建本地office转换器
            IConverter converter = LocalConverter.builder().build();
            converter.convert(docxInputStream).as(DocumentType.DOCX)
                    .to(outputStream).as(DocumentType.PDF).execute();
            converter.shutDown();
        }

    }
}
