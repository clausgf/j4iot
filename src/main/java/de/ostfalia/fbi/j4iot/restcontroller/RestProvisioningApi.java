package de.ostfalia.fbi.j4iot.restcontroller;

import de.ostfalia.fbi.j4iot.data.service.DeviceService;
import de.ostfalia.fbi.j4iot.data.service.ProjectService;
import de.ostfalia.fbi.j4iot.data.service.ServiceUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    private final ProjectService projectService;
    private final DeviceService deviceService;

    // ***********************************************************************

    RestProvisioningApi(ProjectService projectService, DeviceService deviceService) {
        this.projectService = projectService;
        this.deviceService = deviceService;
    }

    // ***********************************************************************

    @GetMapping(value = "/hello")
    public String hello() {
        return "Hello world!";
    }

    // ***********************************************************************

    public record ProvisioningRequest (String projectName, String  deviceName, String provisioningToken) {}
    public record ProvisioningResponse (String tokenType, String accessToken) {}

    @Operation(summary = "Provision (and optionally create) a device")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Provisioning successful",
            content = {@Content(mediaType = "application/json",
            schema = @Schema(implementation = ProvisioningResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Project or device not found", content = @Content),
        @ApiResponse(responseCode = "403", description = "Invalid or expired provisioning token", content = @Content)
    })
    @PermitAll
    @PostMapping(value = "/provision", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> provision(@RequestBody ProvisioningRequest request)
    {
        ServiceUtils.checkName( request.projectName );
        ServiceUtils.checkName( request.deviceName );

        String token = deviceService.provision(request.projectName, request.deviceName, request.provisioningToken);
        ProvisioningResponse response = new ProvisioningResponse("bearer", token);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // ***********************************************************************

}
