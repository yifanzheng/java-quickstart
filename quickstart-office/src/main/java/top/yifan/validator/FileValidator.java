package top.yifan.validator;



/**
 * FileValidator
 *
 * @author zhengyifan
 */
public interface FileValidator {

    boolean isValid(byte[] fileData);

    FileType getType();
}
