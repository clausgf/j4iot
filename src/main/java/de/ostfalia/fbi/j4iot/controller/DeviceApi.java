package de.ostfalia.fbi.j4iot.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.ostfalia.fbi.j4iot.configuration.ApiConfiguration;
import de.ostfalia.fbi.j4iot.data.entity.Device;
import de.ostfalia.fbi.j4iot.data.entity.Forwarding;
import de.ostfalia.fbi.j4iot.data.service.DeviceService;
import de.ostfalia.fbi.j4iot.data.service.ProjectService;
import de.ostfalia.fbi.j4iot.data.service.ServiceUtils;
import de.ostfalia.fbi.j4iot.data.service.TimeseriesService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerMapping;

@RestController

@RequestMapping("${j4iot.api.path:/api}")
public class DeviceApi {

    // ***********************************************************************

    private final Logger log = LoggerFactory.getLogger(DeviceApi.class);
    private final ApiConfiguration apiConfiguration;
    private final ProjectService projectService;
    private final DeviceService deviceService;
    private final TimeseriesService timeseriesService;
    private final RestTemplate restTemplate;

    // ***********************************************************************

    DeviceApi(
            ApiConfiguration apiConfiguration,
            ProjectService projectService,
            DeviceService deviceService,
            TimeseriesService timeseriesService,
            RestTemplate restTemplate)
    {
        this.apiConfiguration = apiConfiguration;
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

    @PostMapping(value = "/log/{projectName}/{deviceName}", consumes = "text/plain")
    public ResponseEntity<?> postLogWithNames(
            @PathVariable String projectName,
            @PathVariable String deviceName,
            @RequestBody String body) {

        ServiceUtils.checkAuthentication( projectName, deviceName );

        // determine the device
        Device device = deviceService.findByProjectNameAndName(projectName, deviceName)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Device not found: projectName=" + projectName + " deviceName=" + deviceName));

        timeseriesService.writeLog(device, body);
        return ResponseEntity.status(HttpStatus.OK).body("");
    }

    // ***********************************************************************

    @GetMapping(value = "/forward/{projectName}/{forwardingName}/**")
    public ResponseEntity<?> getForward(
            @PathVariable String projectName,
            @PathVariable String forwardingName,
            @RequestHeader MultiValueMap<String, String> headers,
            @RequestBody String body,
            final HttpServletRequest request)
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
            String path = apiConfiguration.getServletContextPath() + request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString();
            String remainingPath = new AntPathMatcher().extractPathWithinPattern(path, request.getRequestURI());
            log.info("path={} remainingPath={}", path, remainingPath);
            targetUrl = targetUrl + remainingPath;
        }
        log.info("Forwarding project={} forward={} url={}", projectName, forwardingName, targetUrl);

        try {
            HttpEntity<Object> requestEntity = new HttpEntity<>(body, headers);
            return restTemplate.exchange(
                    targetUrl,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );
        } catch (Exception e) {
            log.info("Error forwarding request to url={}: {}", targetUrl, e.getMessage());
            if (e instanceof HttpClientErrorException ex) {
                return ResponseEntity
                        .status(ex.getStatusCode())
                        .headers(ex.getResponseHeaders())
                        .body(ex.getResponseBodyAsString());
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unknown error forwarding the request (is the url valid?)");
    }

    // ***********************************************************************

}
