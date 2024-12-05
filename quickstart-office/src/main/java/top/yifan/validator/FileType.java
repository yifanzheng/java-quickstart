package top.yifan.validator;

/**
 * 文件类型
 *
 * @author zhengyifan
 */
public enum FileType {

    PDF("PDF"),
    XML("XML"),
    ZIP("ZIP"),
    OFD("OFD");

    private final String fileTypeName;

    FileType(String fileTypeName) {
        this.fileTypeName = fileTypeName;
    }

    public String getFileTypeName() {
        return this.fileTypeName;
    }
}