package com.oa.platform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "oa.file")
public class FileProperties {
    /** 存储类型 local */
    private String type = "local";
    /** 本地存储路径 */
    private String localPath = "./oa-files";
    /** 访问URL前缀 */
    private String urlPrefix = "/file";
}
