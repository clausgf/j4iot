package de.ostfalia.fbi.j4iot.restservice;

import de.ostfalia.fbi.j4iot.data.service.IotService;
import elemental.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api")
public class RestDeviceApi {

    Logger log = LoggerFactory.getLogger(RestDeviceApi.class);
    IotService iotService;

    @GetMapping("/file/{projectName}/{deviceName}/{filename}")
    public String file() {
        return "Hello world!";
    }

    @PostMapping(value = "/telemetry/{projectName}/{deviceName}/{kind}")
    public ResponseEntity<?> telemetry(@RequestBody Json request) {
        return ResponseEntity.status(HttpStatus.OK).body("");
    }

    @PostMapping(value = "/log/{projectName}/{deviceName}")
    public ResponseEntity<?> log(@RequestBody String[] request) {
        return ResponseEntity.status(HttpStatus.OK).body("");
    }
}
