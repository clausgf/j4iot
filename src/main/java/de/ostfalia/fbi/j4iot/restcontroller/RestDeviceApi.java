package de.ostfalia.fbi.j4iot.restcontroller;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.ostfalia.fbi.j4iot.data.entity.Device;
import de.ostfalia.fbi.j4iot.data.service.DeviceService;
import de.ostfalia.fbi.j4iot.data.service.ServiceUtils;
import de.ostfalia.fbi.j4iot.data.service.TimeseriesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("${j4iot.api.path:/api}")
public class RestDeviceApi {

    // ***********************************************************************

    private final Logger log = LoggerFactory.getLogger(RestDeviceApi.class);
    private final DeviceService deviceService;
    private final TimeseriesService timeseriesService;

    // ***********************************************************************

    RestDeviceApi(DeviceService deviceService, TimeseriesService timeseriesService) {
        this.deviceService = deviceService;
        this.timeseriesService = timeseriesService;
    }

    // ***********************************************************************

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

    @GetMapping(value = "/forward/{projectName}/{forwardConfig}")
    public ResponseEntity<?> apiForwarderWithDeviceAuth(
            @PathVariable String forwardConfig,
            @RequestBody String[] data) {

        return ResponseEntity.status(HttpStatus.OK).body("");
    }

    // ***********************************************************************

}
