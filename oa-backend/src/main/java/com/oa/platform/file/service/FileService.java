package com.oa.platform.file.service;

import com.oa.platform.common.api.ResultCode;
import com.oa.platform.common.exception.BusinessException;
import com.oa.platform.common.util.SecurityUtils;
import com.oa.platform.config.FileProperties;
import com.oa.platform.file.entity.SysFile;
import com.oa.platform.file.mapper.SysFileMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件存储服务（本地）。
 */
@Service
public class FileService {

    private final FileProperties properties;
    private final SysFileMapper fileMapper;

    public FileService(FileProperties properties, SysFileMapper fileMapper) {
        this.properties = properties;
        this.fileMapper = fileMapper;
    }

    public SysFile upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "上传文件不能为空");
        }
        String original = file.getOriginalFilename();
        String suffix = "";
        if (original != null && original.contains(".")) {
            suffix = original.substring(original.lastIndexOf('.'));
        }
        String day = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;
        String relative = "/" + day + "/" + fileName;

        try {
            Path dir = Paths.get(properties.getLocalPath(), day);
            Files.createDirectories(dir);
            Path target = dir.resolve(fileName);
            file.transferTo(target.toFile());
        } catch (IOException e) {
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }

        SysFile record = new SysFile();
        record.setOriginalName(original);
        record.setFileName(fileName);
        record.setFilePath(relative);
        record.setUrl(properties.getUrlPrefix() + relative);
        record.setSize(file.getSize());
        record.setContentType(file.getContentType());
        record.setCreateBy(SecurityUtils.getCurrentUserId());
        record.setUploadDay(LocalDate.now());
        record.setCreateTime(LocalDateTime.now());
        fileMapper.insert(record);
        return record;
    }

    /** 本地物理路径 */
    public Path resolveLocalPath(String relative) {
        return Paths.get(properties.getLocalPath()).resolve(relative.replaceFirst("^/", "")).normalize();
    }

    public SysFile getById(Long id) {
        SysFile f = fileMapper.selectById(id);
        if (f == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS, "文件不存在");
        }
        return f;
    }
}
