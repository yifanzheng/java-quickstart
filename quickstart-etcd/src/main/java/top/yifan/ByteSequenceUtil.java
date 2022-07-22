package top.yifan;

import io.etcd.jetcd.ByteSequence;

import static com.google.common.base.Charsets.UTF_8;

/**
 * ByteSequenceUtil
 * 
 * @author Star Zheng
 */
public class ByteSequenceUtil {
    
    private ByteSequenceUtil() {}
    
    public static ByteSequence fromString(String source) {
        return ByteSequence.from(source, UTF_8);
    }
    
    public static String toStringUtf8(ByteSequence source) {
        return source.toString(UTF_8);
    }
    
}
