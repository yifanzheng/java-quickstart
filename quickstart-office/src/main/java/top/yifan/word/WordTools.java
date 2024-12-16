package top.yifan.word;

import com.deepoove.poi.XWPFTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * WordTools
 *
 * @author zhengyifan
 */
public class WordTools {

    private WordTools() {
    }

    /**
     * 填充 Word 模板，支持文本，表格
     *
     * @param templateName 模板名称
     * @param data         数据
     * @return 填充后的 Word 文件字节数组
     * @throws IOException IO 异常
     */
    public static byte[] fillWordTemplate(String templateName, Map<String, String> data) throws IOException {
        try (InputStream fis = ClassLoader.getSystemResourceAsStream("word-template/" + templateName + ".docx")) {
            assert fis != null;

            try (XWPFTemplate template = XWPFTemplate.compile(fis).render(data);
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                template.write(out);

                return out.toByteArray();
            }
        }
    }
}
