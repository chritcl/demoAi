package com.oa.platform.file.controller;

import com.oa.platform.common.api.R;
import com.oa.platform.file.entity.SysFile;
import com.oa.platform.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * 文件上传/下载。
 */
@Tag(name = "文件服务")
@RestController
@RequestMapping("/file")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public R<SysFile> upload(@RequestParam("file") MultipartFile file) {
        return R.ok(fileService.upload(file));
    }

    @Operation(summary = "下载/预览文件")
    @GetMapping("/{id}")
    public void download(@PathVariable Long id, HttpServletResponse response,
                         @RequestParam(required = false, defaultValue = "false") boolean download) throws IOException {
        SysFile file = fileService.getById(id);
        java.nio.file.Path path = fileService.resolveLocalPath(file.getFilePath());
        if (!Files.exists(path)) {
            response.sendError(404, "文件不存在");
            return;
        }
        String contentType = Files.probeContentType(path);
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        response.setContentType(contentType);
        if (download) {
            String filename = URLEncoder.encode(file.getOriginalName() == null ? file.getFileName() : file.getOriginalName(),
                    StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        }
        try (InputStream in = Files.newInputStream(path)) {
            FileCopyUtils.copy(in, response.getOutputStream());
        }
    }
}
