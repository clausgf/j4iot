package de.ostfalia.fbi.j4iot.restcontroller;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.ostfalia.fbi.j4iot.data.entity.Device;
import de.ostfalia.fbi.j4iot.data.entity.Forwarding;
import de.ostfalia.fbi.j4iot.data.service.DeviceService;
import de.ostfalia.fbi.j4iot.data.service.ProjectService;
import de.ostfalia.fbi.j4iot.data.service.ServiceUtils;
import de.ostfalia.fbi.j4iot.data.service.TimeseriesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("${j4iot.api.path:/api}")
public class RestDeviceApi {

    // ***********************************************************************

    private final Logger log = LoggerFactory.getLogger(RestDeviceApi.class);
    private final ProjectService projectService;
    private final DeviceService deviceService;
    private final TimeseriesService timeseriesService;
    private final RestTemplate restTemplate;

    // ***********************************************************************

    RestDeviceApi(
            ProjectService projectService,
            DeviceService deviceService,
            TimeseriesService timeseriesService,
            RestTemplate restTemplate)
    {
        this.projectService = projectService;
        this.deviceService = deviceService;
        this.timeseriesService = timeseriesService;
        this.restTemplate = restTemplate;
    }

    // ***********************************************************************

    @PostMapping(value = "/telemetry/{projectName}/{deviceName}/{kind}",
            consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> postTelemetryWithNames(
            @PathVariable String projectName,
            @PathVariable String deviceName,
            @PathVariable String kind,
            @RequestBody String data) {

        ServiceUtils.checkAuthentication( projectName, deviceName );
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

        ServiceUtils.checkAuthentication( projectName, deviceName );

        // determine the device
        Device device = deviceService.findByProjectNameAndName(projectName, deviceName)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Device not found: projectName=" + projectName + " deviceName=" + deviceName));

        timeseriesService.writeLog(device, data);
        return ResponseEntity.status(HttpStatus.OK).body("");
    }

    // ***********************************************************************

    @GetMapping(value = "/forward/{projectName}/{forwardingName}/{remainingUrl}")
    public ResponseEntity<?> getForward(
            @PathVariable String projectName,
            @PathVariable String forwardingName,
            @PathVariable String remainingUrl,
            HttpRequest request)
    {
        ServiceUtils.checkAuthentication( projectName );
        ServiceUtils.checkName(forwardingName);

        Forwarding f = projectService.findForwardingByProjectNameAndForwardName(projectName, forwardingName)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Forwarding not found: projectName=" + projectName + " forwarding=" + forwardingName));
        if (!f.getEnableMethodGet()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Method not enabled: projectName=" + projectName + " forwarding=" + forwardingName + " method=get");
        }

        String targetUrl = f.getForwardToUrl();
        if (f.getExtendUrl()) {
            targetUrl = targetUrl + remainingUrl;
        }
        log.info("Forwarding project={} forward={} url={}", projectName, forwardingName, targetUrl);

        HttpEntity<Object> requestEntity = new HttpEntity<>(request.getHeaders());
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                targetUrl,
                HttpMethod.GET,
                requestEntity,
                String.class
        );

        return ResponseEntity
                .status(responseEntity.getStatusCode())
                .headers(requestEntity.getHeaders())
                .body(responseEntity.getBody());
    }

    // ***********************************************************************

}
