package com.agileboot.domain.factorylink.plc.util;

import io.minio.*;
import io.minio.errors.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

/**
 * MinIO 文件上传工具类
 */
public class MinioUploadUtil {

    // MinIO 配置（建议从配置文件或环境变量读取）
    private static final String ENDPOINT = "http://10.0.100.28:12000";  // MinIO 服务地址
    private static final String ACCESS_KEY = "BzrMkXwxB5G3afC9JU6m";          // 访问密钥
    private static final String SECRET_KEY = "BrFLl9Cjka1mdT0EwXOZuJyuPOfz4IJOIHvtF0vg";          // 秘密密钥
    private static final String BUCKET_NAME = "fangke";          // 默认桶名称

    // 私有静态客户端（线程安全，可复用）
    private static MinioClient minioClient;

    static {
        minioClient = MinioClient.builder()
                .endpoint(ENDPOINT)
                .credentials(ACCESS_KEY, SECRET_KEY)
                .build();
        // 初始化桶（不存在则创建）
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET_NAME).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET_NAME).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("MinIO 桶初始化失败", e);
        }
    }

    /**
     * 上传文件到 MinIO
     * @param baseDir 存储目录（例如："images/2025/"），可为空字符串表示桶根目录
     * @param file    待上传的 MultipartFile 文件
     * @return 上传后的对象路径（包含目录和文件名），可用于生成访问 URL
     */
    public static String upload(String baseDir, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        // 提取文件名主体和扩展名
        String baseName = "file";          // 默认前缀
        String extension = "";
        if (originalFilename != null && !originalFilename.isEmpty()) {
            int dotIndex = originalFilename.lastIndexOf(".");
            if (dotIndex > 0) {
                baseName = originalFilename.substring(0, dotIndex);
                extension = originalFilename.substring(dotIndex);
            } else {
                baseName = originalFilename;
                extension = "";
            }
        }
        // 清理文件名中可能影响路径的字符（可选，保留下划线、字母数字等）
        baseName = baseName.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5_\\-]", "_");
        // 生成唯一标识（UUID前8位或完全体，这里使用8位短码）
        String uniqueId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String objectName = baseName + "_" + uniqueId + extension;

        // 拼接目录
        if (baseDir != null && !baseDir.trim().isEmpty()) {
            String normalizedDir = baseDir.replaceAll("^/", "");
            if (!normalizedDir.endsWith("/")) {
                normalizedDir += "/";
            }
            objectName = normalizedDir + objectName;
        }

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            return objectName;
        } catch (Exception e) {
            throw new RuntimeException("文件上传到 MinIO 失败", e);
        }
    }

    public static String uploadBase64ImageDirect(String baseDir, String base64Image) {
        if (base64Image == null || base64Image.isEmpty()) {
            throw new IllegalArgumentException("Base64 图片字符串不能为空");
        }

        // 1. 解析 data URL，提取 MIME 类型和纯 Base64
        String base64Data;
        String contentType = "image/png";
        if (base64Image.startsWith("data:")) {
            int commaIndex = base64Image.indexOf(",");
            if (commaIndex == -1) throw new IllegalArgumentException("无效的 data URL");
            String prefix = base64Image.substring(0, commaIndex);
            if (prefix.contains(";base64")) {
                int mimeStart = "data:".length();
                int mimeEnd = prefix.indexOf(";");
                if (mimeEnd > mimeStart) {
                    contentType = prefix.substring(mimeStart, mimeEnd);
                }
            }
            base64Data = base64Image.substring(commaIndex + 1);
        } else {
            base64Data = base64Image;
        }
        base64Data = base64Data.replaceAll("\\s", "");

        byte[] imageBytes = Base64.getDecoder().decode(base64Data);

        // 2. 构建对象名（复用原来的命名规则）
        String extension = getExtensionByContentType(contentType);
        String baseName = "image";
        String uniqueId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String objectName = baseName + "_" + uniqueId + "." + extension;

        if (baseDir != null && !baseDir.trim().isEmpty()) {
            String normalizedDir = baseDir.replaceAll("^/", "");
            if (!normalizedDir.endsWith("/")) {
                normalizedDir += "/";
            }
            objectName = normalizedDir + objectName;
        }

        // 3. 直接上传字节数组到 MinIO（无需文件和 MultipartFile）
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes)) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(objectName)
                    .stream(inputStream, imageBytes.length, -1)
                    .contentType(contentType)
                    .build());
            return objectName;
        } catch (Exception e) {
            throw new RuntimeException("上传到 MinIO 失败", e);
        }
    }

    /**
     * 根据 MIME 类型获取文件扩展名
     */
    private static String getExtensionByContentType(String contentType) {
        if (contentType == null) return "png";
        switch (contentType.toLowerCase()) {
            case "image/jpeg": return "jpg";
            case "image/png":  return "png";
            case "image/gif":  return "gif";
            case "image/bmp":  return "bmp";
            case "image/webp": return "webp";
            default:
                // 从 content/type 中提取后半段作为扩展名
                int slashIndex = contentType.indexOf("/");
                if (slashIndex != -1) {
                    return contentType.substring(slashIndex + 1);
                }
                return "png";
        }
    }

    /**
     * 根据对象路径生成公开访问 URL（需桶策略为公开读）
     * @param objectName upload 方法返回的对象路径
     * @return 完整访问 URL
     */
    public static String getPublicUrl(String objectName) {
        return ENDPOINT + "/" + BUCKET_NAME + "/" + objectName;
    }
}