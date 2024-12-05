package top.yifan.validator;


import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * XMLValidator
 *
 * @author zhengyifan
 */
@Slf4j
class XMLValidator implements FileValidator {

    @Override
    public boolean isValid(byte[] fileData) {
        if (fileData == null) {
            return false;
        }
        try (InputStream xmlInStream = new ByteArrayInputStream(fileData)) {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setValidating(false);
            documentBuilderFactory.setExpandEntityReferences(false);

            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(xmlInStream);
            document.getDocumentElement().normalize();
            return true;
        } catch (IOException | ParserConfigurationException | SAXException e) {
            log.error("XML文件校验异常, message: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public FileType getType() {
        return FileType.XML;
    }
}
