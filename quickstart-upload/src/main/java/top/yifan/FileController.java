package top.yifan;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;

    /**
     * 关于（文件）上传
     *
     * 一般来说，现在比较常用的是对象存储
     *
     * @param files
     */
    @PostMapping("/upload")
    public ResponseEntity<List<String>> upload(@RequestPart("files") MultipartFile[] files) {
        // 使用@RequestPart -- 解决(Swagger 3.0.0)——【文件上传，接收file类型时显示string】

        List<String> fileIdList = fileService.uploadImage(files);
        return ResponseEntity.ok(fileIdList);
    }

    @GetMapping("/{fileId}")
    public void view(HttpServletResponse response, @PathVariable("imageId") String imageId) {
        try {
            fileService.viewFile(response, imageId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
