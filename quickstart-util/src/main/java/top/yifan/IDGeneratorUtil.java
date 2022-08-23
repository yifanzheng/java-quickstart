package top.yifan;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 用于 ID 生成的工具
 * 
 * @author Star Zheng
 */
public class IDGeneratorUtil {

    private static final String[] chars = new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n",
            "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8",
            "9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
            "U", "V", "W", "X", "Y", "Z" };
    
    private IDGeneratorUtil() {}
    
    /**
     * @return 返回一个8位长度的字符串类型的ID
     */
    public static String generateShortUUID() {
        StringBuilder shortBuilder = new StringBuilder();
        String uuid = generateUUID().replace("-", "");
        for (int i = 0; i < 8; i++) {
            String str = uuid.substring(i * 4, i * 4 + 4);
            int x = Integer.parseInt(str, 16);
            shortBuilder.append(chars[x % 0x3E]);
        }
        return shortBuilder.toString().toLowerCase();
    }
    
    /**
     * @return 返回18位长度的long类型ID
     */
    public static long generateLongId() {
        long t1 = 0x7FFFFFFF & System.currentTimeMillis();
        return t1 << 32 | Math.abs(ThreadLocalRandom.current().nextInt());
    }
    
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }
    
}
