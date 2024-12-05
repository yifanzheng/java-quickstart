package top.yifan;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * URLUtil
 *
 * @author Star Zheng
 */
@Slf4j
public class URLUtil {

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    private URLUtil() {
    }

    public static String fullURL(String endpoint, String uri) {
        Assert.notNull(endpoint, "Endpoint can't be null");
        Assert.notNull(uri, "URI can't be null");

        if (endpoint.endsWith("/")) {
            if (uri.startsWith("/")) {
                uri = uri.substring(1);
                return endpoint + uri;
            }
            return endpoint + uri;
        }
        if (uri.startsWith("/")) {
            return endpoint + uri;
        }

        return endpoint + "/" + uri;
    }

    /**
     * 从网络Url中下载文件
     */
    public static byte[] getBytesFromUrl(String url) throws IOException {
        URL urlObj = new URL(url);
        HttpURLConnection urlConnection = (HttpURLConnection) urlObj.openConnection();
        // 设置超时间为3秒
        urlConnection.setConnectTimeout(5 * 1000);
        // 防止屏蔽程序抓取而返回403错误
        urlConnection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        log.info("文件下载链接: {}, 下载状态: {}", url, urlConnection.getResponseCode());
        // 获取输入流
        if (urlConnection.getResponseCode() == 200) {
            byte[] buffer = new byte[1024];
            int len;
            try (InputStream urlInputStream = urlConnection.getInputStream();
                 ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                while (-1 != (len = urlInputStream.read(buffer))) {
                    bos.write(buffer, 0, len);
                }
                return bos.toByteArray();
            }
        }
        return EMPTY_BYTE_ARRAY;
    }



}
