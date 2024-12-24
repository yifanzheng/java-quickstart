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
 * Word2PDF
 *
 * @author zhengyifan
 */
public class Word2PDF {

    /**
     * 转换为 PDF
     * <p>
     * documents4j 是一个跨平台的文档转换库，并且可以在 Linux 上进行 Word 转 PDF 的操作。
     * 它利用 Microsft Office 的 APIs 来进行文档转换，因此需要在Linux上安装 OpenOffice/LibreOffice 编辑器。
     *
     * @param wordPath word 文件路径
     * @param pdfPath  pdf 文件路径
     */
    public void convert(String wordPath, String pdfPath) throws IOException {
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
