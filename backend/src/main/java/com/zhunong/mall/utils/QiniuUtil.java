package com.zhunong.mall.utils;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.zhunong.mall.common.ResultCode;
import com.zhunong.mall.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 七牛云工具类
 */
@Component
public class QiniuUtil {

    private static final Logger log = LoggerFactory.getLogger(QiniuUtil.class);

    @Value("${qiniu.access-key}")
    private String accessKey;

    @Value("${qiniu.secret-key}")
    private String secretKey;

    @Value("${qiniu.bucket}")
    private String bucket;

    @Value("${qiniu.domain}")
    private String domain;

    @Value("${qiniu.region:z0}")
    private String region;

    /**
     * 允许上传的文件类型
     */
    private static final String[] ALLOWED_TYPES = {"image/jpeg", "image/png", "image/gif", "image/webp"};

    /**
     * 最大文件大小 10MB
     */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * 获取域名
     *
     * @return 七牛云域名
     */
    public String getDomain() {
        return domain;
    }

    /**
     * 获取上传凭证
     *
     * @return 上传凭证
     */
    public String getUploadToken() {
        Auth auth = Auth.create(accessKey, secretKey);
        return auth.uploadToken(bucket);
    }

    /**
     * 上传文件到七牛云
     *
     * @param file 文件
     * @return 文件访问URL
     */
    public String uploadFile(MultipartFile file) {
        // 验证文件
        validateFile(file);

        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            originalFilename = "unknown.jpg";
        }
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String key = "zhunong/" + UUID.randomUUID().toString().replace("-", "") + suffix;

        // 上传配置
        Configuration cfg = getConfiguration();
        UploadManager uploadManager = new UploadManager(cfg);

        try {
            Response response = uploadManager.put(file.getBytes(), key, getUploadToken());
            DefaultPutRet putRet = response.jsonToObject(DefaultPutRet.class);
            log.info("文件上传成功，key: {}", putRet.key);
            return domain + "/" + putRet.key;
        } catch (QiniuException e) {
            log.error("七牛云上传失败: {}", e.getMessage());
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAILED);
        } catch (IOException e) {
            log.error("文件读取失败: {}", e.getMessage());
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 删除文件
     *
     * @param fileUrl 文件URL
     * @return 是否删除成功
     */
    public boolean deleteFile(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith(domain)) {
            return false;
        }

        String key = fileUrl.replace(domain + "/", "");
        Configuration cfg = getConfiguration();
        Auth auth = Auth.create(accessKey, secretKey);
        BucketManager bucketManager = new BucketManager(auth, cfg);

        try {
            bucketManager.delete(bucket, key);
            log.info("文件删除成功，key: {}", key);
            return true;
        } catch (QiniuException e) {
            log.error("文件删除失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取私有空间文件访问URL（带过期时间）
     *
     * @param fileUrl 文件URL
     * @param expires 过期时间（秒）
     * @return 带签名的URL
     */
    public String getPrivateUrl(String fileUrl, long expires) {
        if (fileUrl == null || !fileUrl.startsWith(domain)) {
            return fileUrl;
        }

        String key = fileUrl.replace(domain + "/", "");
        Auth auth = Auth.create(accessKey, secretKey);
        return auth.privateDownloadUrl(domain + "/" + key, expires);
    }

    /**
     * 验证文件
     *
     * @param file 文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_NULL, "文件不能为空");
        }

        // 验证文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ResultCode.FILE_SIZE_EXCEED);
        }

        // 验证文件类型
        String contentType = file.getContentType();
        boolean allowed = false;
        for (String type : ALLOWED_TYPES) {
            if (type.equals(contentType)) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            throw new BusinessException(ResultCode.FILE_TYPE_ERROR);
        }
    }

    /**
     * 获取区域配置
     *
     * @return Configuration
     */
    private Configuration getConfiguration() {
        return new Configuration(Region.autoRegion());
    }
}
