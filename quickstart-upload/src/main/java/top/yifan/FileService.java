package top.yifan;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


@Service
public class FileService {

    //spring.servlet.multipart.max-file-size=100MB
    //spring.servlet.multipart.max-request-size=100MB

    private static final String ROOT_PATH = "uploads";

    /**
     * 5*1024*1024 2MB
     */
    private static final long MAX_SIZE = 5242880;

    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd");

    private static final IdWorker idWorker = new IdWorker();



    /**
     * 上传的路径：ROOT_PATH
     * 上传的内容：命名--可以用ID，-->每天一个文件夹保存
     * 限制文件大小：MAX_SIZE
     * <p>
     * 保存记录到数据库
     * ID/存储路径/URL/原名称/用户ID/状态/创建日期/更新日期
     *
     * @param files
     * @return
     */
    public List<String> uploadImage(MultipartFile[] files) {
        //访问路径列表
        List<String> fileIdList = new ArrayList();
        try {
            for (MultipartFile file : files) {
                // 获取相关数据，比如说文件类型，文件名称
                String originalFilename = file.getOriginalFilename();
                // 判断文件类型，我们只支持图片上传，比如说：png，jpg，gif,jpeg
                String contentType = file.getContentType();

                if (StringUtils.isEmpty(contentType)) {
                    throw new RuntimeException("文件格式错误");
                }
                String type = getType(originalFilename, contentType);
                if (type == null) {
                    throw new RuntimeException("不支持此图片类型");
                }

                // 限制文件大小
                long size = file.getSize();
                if (size > MAX_SIZE) {
                    throw new RuntimeException("图片最大仅支持" + (MAX_SIZE / 1024 / 1024) + "Mb");
                }
                //创建图片的保存目录
                //规则：配置目录/日期/类型/ID.类型
                long currentTimeMillis = System.currentTimeMillis();
                String currentDay = simpleDateFormat.format(currentTimeMillis);

                String basePath = PathUtils.getClassLoadRootPath() + File.separator + ROOT_PATH;
                String dayPath = basePath + File.separator + currentDay;
                File dayPathFile = new File(dayPath);
                if (!dayPathFile.exists()) {
                    // 路径不存在则创建
                    dayPathFile.mkdirs();
                }
                // 根据我们定的规则命名
                String targetName = String.valueOf(idWorker.nextId());
                String targetPath = dayPath + File.separator + type + File.separator + targetName + "." + type;
                File targetFile = new File(targetPath);
                if (!targetFile.getParentFile().exists()) {
                    targetFile.mkdirs();
                }

                if (!targetFile.exists()) {
                    targetFile.createNewFile();
                }
                // 保存文件
                file.transferTo(targetFile);

                String fileId = currentTimeMillis + "_" + targetName + "." + type;

                // 记录文件
                // 保存记录到数据库里
                // 返回结果：包含图片的名称和访问路径
                fileIdList.add(fileId);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileIdList;
    }


    public void viewFile(HttpServletResponse httpServletResponse, String fileId) throws IOException {
        // 配置目录已知

        // 根据尺寸来动态返回图片给前端
        // 好处：减少宽带占用，传输速度快
        // 缺点：消耗后台cpu资源
        // 推荐做法：上传上来的时候，把图片复制成三个尺寸：大，中，小
        // 根据尺寸返回结果即可  自己找框架

        // 使用日期的时间戳_ID.类型
        // 需要日期
        if (StringUtils.isBlank(fileId)) {
            return;
        }
        String[] paths = fileId.split("_");
        String dayFormat = simpleDateFormat.format(Long.parseLong(paths[0]));

        // ID
        String name = paths[1];
        // 需要类型
        String type = name.substring(name.lastIndexOf(".") + 1);
        String basePath = PathUtils.getClassLoadRootPath() + File.separator + ROOT_PATH;
        String targetPath = basePath + File.separator + dayFormat + File.separator + type + File.separator + name;

        File file = new File(targetPath);
        OutputStream outputStream = null;
        FileInputStream fileInputStream = null;
        try {
            httpServletResponse.setContentType("image/jpg" );//设置内容类型
            outputStream = httpServletResponse.getOutputStream();
            //读取
            fileInputStream = new FileInputStream(file);
            byte[] buff = new byte[1024];
            int length;
            while ((length = fileInputStream.read(buff)) != -1) {
                outputStream.write(buff, 0, length);
            }
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();//关流
            }
            if (outputStream != null) {
                outputStream.close();//关流
            }
        }
    }

    /**
     * 获取文件类型
     *
     * @param fileName
     * @param contentType
     */
    private String getType(String fileName, String contentType) {
        String type = null;
        if ("image/png".equals(contentType) && fileName.toLowerCase().endsWith(".png")) {
            type = "png";
        } else if ("image/gif".equals(contentType) && fileName.toLowerCase().endsWith(".gif")) {
            type = "gif";
        } else if ("image/jpg".equals(contentType) && fileName.toLowerCase().endsWith(".jpg")) {
            type = "jpg";
        } else if ("image/jpeg".equals(contentType) && fileName.toLowerCase().endsWith(".jpg")) {
            type = "jpeg";   //.jpg格式 在上传的时候调用file.getContentType()的时候却是.jpeg格式  -->把这个存为jpeg
        } else if ("image/jpeg".equals(contentType) && fileName.toLowerCase().endsWith(".jpeg")) {
            type = "jpeg";
        }
        return type;
    }


}
