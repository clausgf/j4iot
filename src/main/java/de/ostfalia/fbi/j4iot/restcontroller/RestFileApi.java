package de.ostfalia.fbi.j4iot.restcontroller;

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
public class RestFileApi {

    // ***********************************************************************

    private final Logger log = LoggerFactory.getLogger(RestFileApi.class);
    private final DeviceService deviceService;
    private final FileService fileService;

    // ***********************************************************************

    RestFileApi(DeviceService deviceService, FileService fileService) {
        this.deviceService = deviceService;
        this.fileService = fileService;
    }

    // ***********************************************************************

    @Operation(summary = "Get a resource from the file system; if the device specific resource is not available, return a project wide default")
    @RequestMapping (method = RequestMethod.HEAD, path = "/file/{projectName}/{deviceName}/{filename}")
    public ResponseEntity<String> headFileWithNames(
            @PathVariable String projectName,
            @PathVariable String deviceName,
            @PathVariable String filename,
            HttpServletRequest httpRequest,
            ServletWebRequest webRequest) {

        ServiceUtils.checkName(projectName);
        ServiceUtils.checkName(deviceName);

        // determine the device
        Device device = deviceService.findByProjectNameAndName(projectName, deviceName)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Device not found: projectName=" + projectName + " deviceName=" + deviceName));

        // get the resource, last modified and etag
        Resource resource = null;
        long lastModified;
        try {
            resource = fileService.loadFileAsResource(device, filename);
            lastModified = resource.lastModified();
        } catch (IOException e) {
            String msg = String.format("IO error (file not found?) (projectName=%s deviceName=%s filename=%s)",
                    projectName, deviceName, filename);
            log.info(msg);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, msg);
        }

        // check for possible not modified response
        String etag = fileService.calcEtag(resource);
        if (webRequest.checkNotModified(etag, lastModified)) {
            // shortcut exit - no further processing necessary
            return null;
        }

        // determine content type for the response
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

        return ResponseEntity
                .ok()
                .eTag(etag)
                .lastModified(lastModified)
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body("");
        // TODO add not modified response
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

        ServiceUtils.checkName(projectName);
        ServiceUtils.checkName(deviceName);

        // determine the device
        Device device = deviceService.findByProjectNameAndName(projectName, deviceName)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Device not found: projectName=" + projectName + " deviceName=" + deviceName));

        // get the resource, last modified and etag
        Resource resource;
        long lastModified;
        try {
            resource = fileService.loadFileAsResource(device, filename);
            lastModified = resource.lastModified();
        } catch (IOException e) {
            String msg = String.format("IO error (file not found?) (projectName=%s deviceName=%s filename=%s)",
                    projectName, deviceName, filename);
            log.info(msg);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, msg);
        }

        // check for possible not modified response
        String etag = fileService.calcEtag(resource);
        if (webRequest.checkNotModified(etag, lastModified)) {
            // shortcut exit - no further processing necessary
            return null;
        }

        // determine content type for the response
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

        return ResponseEntity
                .ok()
                .eTag(etag)
                .lastModified(lastModified)
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    // ***********************************************************************

}
