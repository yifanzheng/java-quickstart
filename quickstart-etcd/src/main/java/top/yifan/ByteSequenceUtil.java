package top.yifan;

import io.etcd.jetcd.ByteSequence;

import java.nio.charset.StandardCharsets;

/**
 * ByteSequenceUtil
 *
 * @author Star Zheng
 */
public class ByteSequenceUtil {

    private ByteSequenceUtil() {
    }

    public static ByteSequence fromString(String source) {
        return ByteSequence.from(source, StandardCharsets.UTF_8);
    }

    public static String toStringUtf8(ByteSequence source) {
        return source.toString(StandardCharsets.UTF_8);
    }

}
