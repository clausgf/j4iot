package de.ostfalia.fbi.j4iot.controller;

import de.ostfalia.fbi.j4iot.data.entity.Device;
import de.ostfalia.fbi.j4iot.data.service.DeviceService;
import de.ostfalia.fbi.j4iot.data.service.FileService;
import de.ostfalia.fbi.j4iot.data.service.ServiceUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@RestController
@RequestMapping("${j4iot.api.path:/api}")
public class FileApi {

    // ***********************************************************************

    private final Logger log = LoggerFactory.getLogger(FileApi.class);
    private final DeviceService deviceService;
    private final FileService fileService;

    // ***********************************************************************

    FileApi(DeviceService deviceService, FileService fileService) {
        this.deviceService = deviceService;
        this.fileService = fileService;
    }

    // ***********************************************************************

    private class FileResourceCollector {
        String projectName;
        String deviceName;
        String filename;
        Resource resource;
        long lastModified;
        long contentLength;

        FileResourceCollector(String projectName, String deviceName, String filename) {
            this.projectName = projectName;
            this.deviceName = deviceName;
            this.filename = filename;

            // determine the device
            Device device = deviceService.findByProjectNameAndName(projectName, deviceName)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Device not found: projectName=" + projectName + " deviceName=" + deviceName));

            // get the resource, last modified and etag
            resource = null;
            lastModified = 0;
            contentLength = 0;
            try {
                resource = fileService.loadFileAsResource(device, filename);
                lastModified = resource.lastModified();
                contentLength = resource.contentLength();
            } catch (IOException e) {
                String msg = String.format("IO error (file not found?) (projectName=%s deviceName=%s filename=%s)",
                        projectName, deviceName, filename);
                log.info(msg);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, msg);
            }

        }

        public String getCheckEtag(ServletWebRequest webRequest) {
            String etag = fileService.calcEtag(resource);
            if (webRequest.checkNotModified(etag, lastModified)) {
                // shortcut exit - checkNotModified has prepared everything
                return null;
            }
            return etag;
        }

        public String getCheckContentType(HttpServletRequest httpRequest) {
            String contentType;
            try {
                contentType = httpRequest.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException e) {
                String msg = String.format("Error determining content type (projectName=%s deviceName=%s filename=%s): %s",
                        projectName, deviceName, filename, e.getMessage());
                log.info(msg);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
            }
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            return contentType;
        }
    }

    // ***********************************************************************

    @Operation(summary = "Get response header for a resource from the file system; if the device specific resource is not available, return a project wide default")
    @RequestMapping (method = RequestMethod.HEAD, path = "/file/{projectName}/{deviceName}/{filename}")
    public ResponseEntity<String> headFileWithNames(
            @PathVariable String projectName,
            @PathVariable String deviceName,
            @PathVariable String filename,
            HttpServletRequest httpRequest,
            ServletWebRequest webRequest) {

        ServiceUtils.checkAuthentication(projectName, deviceName);

        FileResourceCollector f = new FileResourceCollector(projectName, deviceName, filename);
        String etag = f.getCheckEtag(webRequest);
        String contentType = f.getCheckContentType(httpRequest);

        return ResponseEntity
                .ok()
                .eTag(etag)
                .lastModified(f.lastModified)
                .contentLength(f.contentLength)
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body("");
    }

    // ***********************************************************************

    @Operation(summary = "Get a resource from the file system; if the device specific resource is not available, return a project wide default")
    @GetMapping("/file/{projectName}/{deviceName}/{filename}")
    public ResponseEntity<Resource> getFileWithNames(
            @PathVariable String projectName,
            @PathVariable String deviceName,
            @PathVariable String filename,
            HttpServletRequest httpRequest,
            ServletWebRequest webRequest) {

        ServiceUtils.checkAuthentication(projectName, deviceName);

        FileResourceCollector f = new FileResourceCollector(projectName, deviceName, filename);
        String etag = f.getCheckEtag(webRequest);
        String contentType = f.getCheckContentType(httpRequest);

        return ResponseEntity
                .ok()
                .eTag(etag)
                .lastModified(f.lastModified)
                .contentLength(f.contentLength)
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(f.resource);
    }

    // ***********************************************************************

}
