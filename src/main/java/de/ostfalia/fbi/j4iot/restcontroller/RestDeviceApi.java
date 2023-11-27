package de.ostfalia.fbi.j4iot.restcontroller;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.ostfalia.fbi.j4iot.data.entity.Device;
import de.ostfalia.fbi.j4iot.data.service.DeviceService;
import de.ostfalia.fbi.j4iot.data.service.FileService;
import de.ostfalia.fbi.j4iot.data.service.ServiceUtils;
import de.ostfalia.fbi.j4iot.data.service.TimeseriesService;
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
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@RestController
@RequestMapping(value = "/api")
public class RestDeviceApi {

    // ***********************************************************************

    private final Logger log = LoggerFactory.getLogger(RestDeviceApi.class);
    private final DeviceService deviceService;
    private final FileService fileService;
    private final TimeseriesService timeseriesService;

    // ***********************************************************************

    RestDeviceApi(DeviceService deviceService, FileService fileService, TimeseriesService timeseriesService) {
        this.deviceService = deviceService;
        this.fileService = fileService;
        this.timeseriesService = timeseriesService;
    }

    // ***********************************************************************
    // TODO head requests
    @GetMapping("/file/{filename}")
    public String getFileDeviceFromAuth(
            @PathVariable String filename) {
        // determine the device from the security context
        return "Hello world!";
    }
    @Operation(summary = "Get a resource from the file system; if the device specific resource is not available, return a project wide default")
    @GetMapping("/file/{projectName}/{deviceName}/{filename}")
    public ResponseEntity<Resource> getFileWithNames(
            @PathVariable String projectName,
            @PathVariable String deviceName,
            @PathVariable String filename,
            HttpServletRequest request) {

        ServiceUtils.checkName( projectName );
        ServiceUtils.checkName( deviceName );
        ServiceUtils.checkName( filename );

        // determine the device
        Device device = deviceService.findByProjectNameAndName(projectName, deviceName)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Device not found: projectName=" + projectName + " deviceName=" + deviceName));

        // get the response
        Resource resource = fileService.loadFileAsResource(device, filename);

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

        // determine the media type of the response

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
        // TODO add etag
    }

    // ***********************************************************************

    @PostMapping(value = "/telemetry/{kind}")
    public ResponseEntity<?> postTelemetryDeviceFromAuth(
            @PathVariable String kind,
            @RequestBody String data) {
        return ResponseEntity.ok().body("");
    }


    @PostMapping(value = "/telemetry/{projectName}/{deviceName}/{kind}",
            consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> postTelemetryWithNames(
            @PathVariable String projectName,
            @PathVariable String deviceName,
            @PathVariable String kind,
            @RequestBody String data) {

        ServiceUtils.checkName( projectName );
        ServiceUtils.checkName( deviceName );
        ServiceUtils.checkName( kind );

        // determine the device
        Device device = deviceService.findByProjectNameAndName(projectName, deviceName)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Device not found: projectName=" + projectName + " deviceName=" + deviceName));

        try {
            timeseriesService.writeTelemetryJson(device, kind, data);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Bad json body: " + e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body("");
    }

    // ***********************************************************************

    @PostMapping(value = "/log")
    public ResponseEntity<?> postLogDeviceFromAuth(@RequestBody String[] request) {
        return ResponseEntity.status(HttpStatus.OK).body("");
    }


    @PostMapping(value = "/log/{projectName}/{deviceName}")
    public ResponseEntity<?> postLogWithNames(
            @PathVariable String projectName,
            @PathVariable String deviceName,
            @RequestBody String[] data) {

        ServiceUtils.checkName(projectName);
        ServiceUtils.checkName(deviceName);

        // determine the device
        Device device = deviceService.findByProjectNameAndName(projectName, deviceName)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Device not found: projectName=" + projectName + " deviceName=" + deviceName));

        timeseriesService.writeLog(device, data);
        return ResponseEntity.status(HttpStatus.OK).body("");
    }

    // ***********************************************************************

    @GetMapping(value = "/forward/{forwardConfig}")
    public ResponseEntity<?> apiForwarderWithDeviceAuth(
            @PathVariable String forwardConfig,
            @RequestBody String[] data) {

        return ResponseEntity.status(HttpStatus.OK).body("");
    }

    // ***********************************************************************

}
