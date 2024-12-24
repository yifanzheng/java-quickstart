package top.yifan.pdf;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.utils.PdfMerger;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * PDFUtils
 *
 * @author zhengyifan
 */
@Slf4j
public class PDFUtils {

    private PDFUtils() {

    }

    /**
     * 合并pdf文件
     * <p>
     * 注意：如果合并的文件过多或过大，存在内存溢出问题
     *
     * @param srcPdfPaths Source PDF file paths
     * @param destPdfPath Destination PDF file path
     */
    public static void mergePDFNormal(List<String> srcPdfPaths, String destPdfPath) {
        PdfMerger merger = null;
        // 创建pdf输出流
        try (OutputStream outputStream = Files.newOutputStream(Paths.get(destPdfPath));
             PdfDocument mergePdfDoc = new PdfDocument(new PdfWriter(outputStream))) {
            merger = new PdfMerger(mergePdfDoc);
            for (String inputFile : srcPdfPaths) {
                // 读取pdf文件
                try (PdfDocument srcPdfDoc = new PdfDocument(new PdfReader(inputFile))) {
                    // 合并pdf文件
                    merger.merge(srcPdfDoc, 1, srcPdfDoc.getNumberOfPages());
                }
            }
            log.info("PDFs merged successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            log.error("PDFs merged failed, message: {}", e.getMessage());
        } finally {
            if (merger != null) {
                merger.close();
            }
        }
    }

    public static void mergePDFByStream(List<String> srcPdfPaths, String destPdfPath) throws IOException {
        WriterProperties writerProperties = new WriterProperties();
        writerProperties.setFullCompressionMode(true);
        writerProperties.setCompressionLevel(9);
        try (PdfWriter pdfWriter = new PdfWriter(new BufferedOutputStream(Files.newOutputStream(Paths.get(destPdfPath))), writerProperties);
             PdfDocument pdfWriteDoc = new PdfDocument(pdfWriter)) {
            for (String inputFile : srcPdfPaths) {
                try (PdfDocument pdfReadDoc = new PdfDocument(new PdfReader(inputFile))) {
                    int numPages = pdfReadDoc.getNumberOfPages();
                    for (int pageNumber = 1; pageNumber <= numPages; pageNumber++) {
                        // 在目标文档中创建新页面
                        PdfPage pdfPage = pdfWriteDoc.addNewPage(PageSize.A4);
                        PdfCanvas canvas = new PdfCanvas(pdfPage);
                        // 将源页面内容复制到新页面
                        canvas.addXObject(pdfReadDoc.getPage(pageNumber).copyAsFormXObject(pdfWriteDoc));
                    }
                }
            }
        }
    }

    public static void mergePDFEnhance(List<String> srcPdfPaths, String destPdfPath) throws IOException {
        int count = 0;
        PDFMergerUtility merger = new PDFMergerUtility();
        try (PDDocument pdfWriteDoc = new PDDocument(MemoryUsageSetting.setupTempFileOnly())) {
            pdfWriteDoc.setResourceCache(new PdfResourceCache());
            for (int i = 0, len = srcPdfPaths.size(); i < len; i++) {
                try (BufferedInputStream stream = new BufferedInputStream(Files.newInputStream(Paths.get(srcPdfPaths.get(i))));
                     PDDocument inputDocument = PDDocument.load(stream, MemoryUsageSetting.setupTempFileOnly())) {
                    inputDocument.setResourceCache(new PdfResourceCache());
                    merger.appendDocument(pdfWriteDoc, inputDocument);
                    // 每合并100个文件就将内存数据刷新到磁盘
                    if (i % 100 == 0) {
                        pdfWriteDoc.save(destPdfPath);
                    }
                }

                System.out.println(++count);
            }
            pdfWriteDoc.save(destPdfPath);
            log.info("PDF files merged successfully.");
        }
    }

    public static void main(String[] args) throws IOException {
        List<String> inputFilePaths = getInputFilePaths("D:/pdf");
        mergePDFEnhance(inputFilePaths, "merge.pdf");
    }

    public static List<String> getInputFilePaths(String directory) throws IOException {
        try (Stream<Path> paths = Files.walk(Paths.get(directory))) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".pdf"))
                    .map(Path::toString)
                    .collect(Collectors.toList());
        }
    }
}
