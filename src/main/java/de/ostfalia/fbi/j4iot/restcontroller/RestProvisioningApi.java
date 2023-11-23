package de.ostfalia.fbi.j4iot.restcontroller;

import de.ostfalia.fbi.j4iot.data.service.IotService;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api")
public class RestProvisioningApi {

    private final Logger log = LoggerFactory.getLogger(RestProvisioningApi.class);
    private final IotService iotService;

    RestProvisioningApi(IotService iotService) {
        this.iotService = iotService;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello world!";
    }

    public record ProvisioningRequest (String projectName, String  deviceName, String provisioningToken) {}
    public record ProvisioningResponse (String bearer, String accessToken) {}

    @PermitAll
    @PostMapping(value = "/provision", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> provision(@RequestBody ProvisioningRequest request) {
        String token = iotService.provision(request.projectName, request.deviceName, request.provisioningToken);
        ProvisioningResponse response = new ProvisioningResponse("bearer", token);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
