package de.ostfalia.fbi.j4iot.data.service;

import de.ostfalia.fbi.j4iot.data.entity.Device;
import de.ostfalia.fbi.j4iot.data.entity.Project;
import org.reflections.vfs.Vfs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.ArrayList;

@Service
public class FileService {

    // ***********************************************************************

    private final Logger log = LoggerFactory.getLogger(FileService.class);
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();
    private final Path basePath;

    // ***********************************************************************

    public FileService(
            @Value("${j4iot.files.base-path:iot-data}") String basePath
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

    public boolean isProjectFile(Project project, Path path){
        if (project == null || path == null){
            return false;
        }
        Path projectBase = Paths.get(basePath.toString(), project.getName(), String.valueOf(path.getFileName()));
        return projectBase.equals(path);
    }

    public Resource loadFileAsResource(Device d, String fileName) throws FileNotFoundException {
        Assert.notNull(d, "Must specify a valid device");
        Assert.notNull(fileName, "Must specify a valid filename");

        // determine path
        Path projectBase = Paths.get(basePath.toString(), d.getProject().getName());
        Path p = Paths.get(projectBase.toString(), d.getName(), fileName).toAbsolutePath().normalize();
        if (!Files.exists(p)) {
            p = Paths.get(projectBase.toString(), fileName).toAbsolutePath().normalize();
        }

        // check that we did not break out of the project file system
        if (!p.startsWith(projectBase) || !Files.exists(p)) {
            throw new FileNotFoundException();
        }

        return new FileSystemResource(p);
    }

    public Resource loadFileAsResource(Project project, String fileName) throws FileNotFoundException{
        Assert.notNull(project, "Must specify a valid project");
        Assert.notNull(fileName, "Must specify a valid filename");

        Path projectBase = Paths.get(basePath.toString(), project.getName());
        Path p = Paths.get(projectBase.toString(), fileName).toAbsolutePath().normalize();

        if (!p.startsWith(projectBase) || !Files.exists(p)) {
            throw new FileNotFoundException();
        }

        return new FileSystemResource(p);
    }

    public boolean exists(Project project, String fileName){
        Assert.notNull(project, "Must specify a valid project");
        Assert.notNull(fileName, "Must specify a valid filename");

        Path projectBase = Paths.get(basePath.toString(), project.getName());
        Path p = Paths.get(projectBase.toString(), fileName).toAbsolutePath().normalize();

        return p.startsWith(projectBase) && Files.exists(p);
    }

    public boolean exists(Device device, String fileName){
        Assert.notNull(device, "Must specify a valid device");
        Assert.notNull(fileName, "Must specify a valid filename");

        // determine path
        Path projectBase = Paths.get(basePath.toString(), device.getProject().getName());
        Path p = Paths.get(projectBase.toString(), device.getName(), fileName).toAbsolutePath().normalize();

        return p.startsWith(projectBase) && Files.exists(p);
    }

    public List<Resource> loadFilesAsResources(Device device) throws IOException {
        Assert.notNull(device, "Must specify a valid device");

        Path projectBase = Paths.get(basePath.toString(), device.getProject().getName());
        Path p = Paths.get(projectBase.toString(), device.getName()).toAbsolutePath().normalize();

        if (!p.startsWith(projectBase) || !Files.exists(p)) {
            throw new FileNotFoundException();
        }

        List<Resource> result = new ArrayList<>();
        result.add(new FileSystemResource(p));
        for(File file : result.get(0).getFile().listFiles()){
            result.add(new FileSystemResource(file));
        }
        return result;
    }

    public List<Resource> loadFilesAsResources(Project project) throws IOException {
        Assert.notNull(project, "Must specify a valid device");

        Path projectBase = Paths.get(basePath.toString(), project.getName());
        Path p = Paths.get(projectBase.toString()).toAbsolutePath().normalize();

        if (!p.startsWith(projectBase) || !Files.exists(p)) {
            throw new FileNotFoundException();
        }

        List<Resource> result = new ArrayList<>();
        result.add(new FileSystemResource(p));
        for(File file : result.get(0).getFile().listFiles()){
            result.add(new FileSystemResource(file));
        }
        return result;
    }

    public boolean createFolderFor(Device device) throws IOException {
        Assert.notNull(device, "Must specify a valid device");

        Path projectBase = Paths.get(basePath.toString(), device.getProject().getName());
        Path p = Paths.get(projectBase.toString(), device.getName()).toAbsolutePath().normalize();

        if (!p.startsWith(projectBase)) {
            throw new FileNotFoundException();
        }
        if (Files.exists(p)){
            return false;
        }else{
            Files.createDirectory(p);
            return true;
        }
    }

    public boolean createFolderFor(Project project) throws IOException {
        Assert.notNull(project, "Must specify a valid device");

        Path projectBase = Paths.get(basePath.toString(), project.getName());
        Path p = Paths.get(projectBase.toString()).toAbsolutePath().normalize();

        if (!p.startsWith(projectBase)) {
            throw new FileNotFoundException();
        }
        if (Files.exists(p)){
            return false;
        }else{
            Files.createDirectory(p);
            return true;
        }
    }

    public boolean createFileFor(Device device, String fileName) throws IOException {
        Assert.notNull(device, "Must specify a valid device");

        Path projectBase = Paths.get(basePath.toString(), device.getProject().getName());
        Path p = Paths.get(projectBase.toString(), device.getName(), fileName).toAbsolutePath().normalize();

        if (!p.startsWith(projectBase) || !Files.exists(p)) {
            throw new FileNotFoundException();
        }
        if (Files.exists(p)){
            return false;
        }else{
            Files.createFile(p);
            return true;
        }
    }

    public boolean createFileFor(Project project, String fileName) throws IOException {
        Assert.notNull(project, "Must specify a valid device");

        Path projectBase = Paths.get(basePath.toString(), project.getName());
        Path p = Paths.get(projectBase.toString(), fileName).toAbsolutePath().normalize();

        if (!p.startsWith(projectBase) || !Files.exists(p)) {
            throw new FileNotFoundException();
        }
        if (Files.exists(p)){
            return false;
        }else{
            Files.createFile(p);
            return true;
        }
    }

    public String calcEtag(Resource r) {
        Assert.notNull(r, "Must specify a resource");

        String etag = null;
        long len = 0;
        long lastModified = 0;
        try {
            len = r.contentLength();
            lastModified = r.lastModified();
        } catch (IOException e) {
            return null;
        }
        String fingerprint = Long.toString(len) + "-" + Long.toString(lastModified);
        etag = base64Encoder.encodeToString( DigestUtils.md5Digest(fingerprint.getBytes()) );

        return etag;
    }

    // ***********************************************************************

}
