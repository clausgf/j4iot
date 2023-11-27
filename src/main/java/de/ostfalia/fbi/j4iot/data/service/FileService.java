package de.ostfalia.fbi.j4iot.data.service;

import de.ostfalia.fbi.j4iot.data.entity.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileService {

    // ***********************************************************************

    private final Logger log = LoggerFactory.getLogger(FileService.class);
    private Path basePath;

    // ***********************************************************************

    public FileService(
            @Value("${j4iot.files.base-path}") String basePath
    ) {
        this.basePath = Paths.get(basePath).toAbsolutePath().normalize();
        String currentDir = new File("").getAbsolutePath();
        log.info("FileService currentDir={} basePath={}", currentDir, basePath);
        try {
            Files.createDirectories(this.basePath);
        } catch (IOException e) {
            log.error("Failure creating directory {} from j4iot.files.base-path - is there a permission problem?", this.basePath.toString());
        }
    }

    public Resource loadFileAsResource(Device d, String fileName) {
        Assert.notNull(d, "Must specify a valid device");
        Assert.notNull(fileName, "Must specify a valid filename");

        // determine path
        Path p = Paths.get(basePath.toString(), d.getProject().getName(), d.getName(), fileName).normalize();
        if (!Files.exists(p)) {
            p = Paths.get(basePath.toString(), d.getProject().getName(), fileName).normalize();
        }

        // leave fileNotFound exception handling to the caller
        Resource resource = new FileSystemResource(p);
        return resource;
    }

    // ***********************************************************************


    // ***********************************************************************

}
