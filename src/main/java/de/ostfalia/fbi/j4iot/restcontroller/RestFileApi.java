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
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.FileNotFoundException;
import java.io.IOException;

@RestController
@RequestMapping(value = "/api")
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
            HttpServletRequest request) {

        ServiceUtils.checkName(projectName);
        ServiceUtils.checkName(deviceName);

        // determine the device
        Device device = deviceService.findByProjectNameAndName(projectName, deviceName)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Device not found: projectName=" + projectName + " deviceName=" + deviceName));

        // get the response and check its existence
        Resource resource = null;
        try {
            resource = fileService.loadFileAsResource(device, filename);
        } catch (FileNotFoundException e) {
            String msg = String.format("File not found (projectName=%s deviceName=%s filename=%s)",
                    projectName, deviceName, filename);
            log.info(msg);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, msg);
        }
        if (!resource.exists()) {  // double check the resource
            String msg = String.format("File not found (projectName=%s deviceName=%s filename=%s)",
                    projectName, deviceName, filename);
            log.info(msg);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, msg);
        }

        String etag = fileService.calcEtag(resource);

        return ResponseEntity.ok()
                .eTag(etag)
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
            HttpServletRequest request) {

        ServiceUtils.checkName(projectName);
        ServiceUtils.checkName(deviceName);

        // determine the device
        Device device = deviceService.findByProjectNameAndName(projectName, deviceName)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Device not found: projectName=" + projectName + " deviceName=" + deviceName));

        // get the response and check its existence
        Resource resource = null;
        try {
            resource = fileService.loadFileAsResource(device, filename);
        } catch (FileNotFoundException e) {
            String msg = String.format("File not found (projectName=%s deviceName=%s filename=%s)",
                    projectName, deviceName, filename);
            log.info(msg);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, msg);
        }
        if (!resource.exists()) {  // double check the resource
            String msg = String.format("File not found (projectName=%s deviceName=%s filename=%s)",
                    projectName, deviceName, filename);
            log.info(msg);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, msg);
        }

        // determine content type for the response
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException e) {
            String msg = String.format("Error determining content type (projectName=%s deviceName=%s filename=%s): %s",
                    projectName, deviceName, filename, e.getMessage());
            log.info(msg);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
        }
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        String etag = fileService.calcEtag(resource);

        return ResponseEntity.ok()
                .eTag(etag)
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
        // TODO add not modified response
    }

    // ***********************************************************************

}
