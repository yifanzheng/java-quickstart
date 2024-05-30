package top.yifan;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.io.StringWriter;

public class XStreamUitl {

    private XStreamUitl() {
    }

    public static String toXml(Object obj) {
        XStream xstream = new XStream(new DomDriver("utf8"));
        xstream.processAnnotations(obj.getClass()); // 识别obj类中的注解
        // 以格式化的方式输出XML
        return xstream.toXML(obj);
    }

    public static String toCompressedXml(Object obj) {
        // 以压缩的方式输出XML
        XStream xstream = new XStream(new DomDriver("utf8"));
        xstream.processAnnotations(obj.getClass()); // 识别obj类中的注解
        StringWriter sw = new StringWriter();
        xstream.marshal(obj, new CompactWriter(sw));
        return sw.toString();
    }

    public static <T> T toBean(String xmlStr, Class<T> cls) {
        XStream xstream = new XStream(new DomDriver());
        xstream.processAnnotations(cls);
        xstream.ignoreUnknownElements();
        @SuppressWarnings("unchecked")
        T t = (T) xstream.fromXML(xmlStr);
        return t;
    }

}
