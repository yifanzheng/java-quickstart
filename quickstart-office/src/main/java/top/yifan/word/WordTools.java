package top.yifan.word;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @author zhengyifan
 */
public class WordTools {

    private WordTools() {
    }

    /**
     * 填充 Word 模板
     *
     * @param templateName 模板名称
     * @param data         数据
     * @return 填充后的 Word 文件字节数组
     * @throws IOException IO 异常
     */
    public static byte[] fillWordTemplate(String templateName, Map<String, String> data) throws IOException {
        try (InputStream fis = ClassLoader.getSystemResourceAsStream("word-template/" + templateName + ".docx")) {
            assert fis != null;
            try (XWPFDocument document = new XWPFDocument(fis);
                 ByteArrayOutputStream fos = new ByteArrayOutputStream()) {
                for (XWPFParagraph paragraph : document.getParagraphs()) {
                    for (XWPFRun run : paragraph.getRuns()) {
                        String text = run.getText(0);
                        if (text != null) {
                            for (Map.Entry<String, String> entry : data.entrySet()) {
                                text = text.replace(entry.getKey(), entry.getValue());
                            }
                            run.setText(text, 0);
                        }
                    }
                }

                document.write(fos);

                return fos.toByteArray();
            }
        }
    }
}
