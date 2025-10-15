package com.yedam.scm.img.service.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import com.yedam.scm.img.mapper.ImgMapper;
import com.yedam.scm.img.service.ImgService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImgServiceImpl implements ImgService {

    private final ImgMapper mapper;

    // 지원하는 확장자들
    private static final String[] SUPPORTED_EXTS = { ".png", ".jpg", ".jpeg", ".gif", ".bmp" };

    @Override
    public Resource getDefectImage(String inLogId) throws IOException {
        String dbPath = mapper.getLogimg(inLogId);
        
        String fixedPath = removeDuplicateExtension(dbPath);

        Path imagePath = findExistingFile(fixedPath);

        if (imagePath == null) {
            throw new FileNotFoundException("Image not found for ID: " + inLogId);
        }

        return new UrlResource(imagePath.toUri());
    }

    private String removeDuplicateExtension(String path) {
        for (String ext : SUPPORTED_EXTS) {
            String doubleExt = ext + ext;
            if (path.toLowerCase().endsWith(doubleExt)) {
                return path.substring(0, path.length() - ext.length());
            }
        }
        return path;
    }

    private Path findExistingFile(String basePath) {
        Path path = Paths.get(basePath);
        if (Files.exists(path)) {
            return path;
        }

        for (String ext : SUPPORTED_EXTS) {
            Path pathWithExt = Paths.get(basePath + ext);
            if (Files.exists(pathWithExt)) {
                return pathWithExt;
            }
        }

        return null;
    }
}
