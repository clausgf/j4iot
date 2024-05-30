package de.ostfalia.fbi.j4iot;

import de.ostfalia.fbi.j4iot.configuration.DefaultConfiguration;
import de.ostfalia.fbi.j4iot.data.entity.*;
import de.ostfalia.fbi.j4iot.data.repository.*;
import de.ostfalia.fbi.j4iot.data.service.DeviceService;
import de.ostfalia.fbi.j4iot.data.service.ProjectService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SetupDatabase implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SetupDatabase.class);

    DefaultConfiguration defaultConfiguration;
    ProjectService projectService;
    DeviceService deviceService;
    ProjectRepository projectRepository;
    ProvisioningTokenRepository provisioningTokenRepository;
    ForwardingRepository forwardingRepository;
    DeviceRepository deviceRepository;
    DeviceTokenRepository deviceTokenRepository;


    public SetupDatabase(DefaultConfiguration defaultConfiguration,
                         ProjectService projectService,
                         DeviceService deviceService,
                         ProjectRepository projectRepository,
                         ProvisioningTokenRepository provisioningTokenRepository,
                         ForwardingRepository forwardingRepository,
                         DeviceRepository deviceRepository,
                         DeviceTokenRepository deviceTokenRepository) {
        this.defaultConfiguration = defaultConfiguration;
        this.projectService = projectService;
        this.deviceService = deviceService;
        this.projectRepository = projectRepository;
        this.provisioningTokenRepository = provisioningTokenRepository;
        this.forwardingRepository = forwardingRepository;
        this.deviceRepository = deviceRepository;
        this.deviceTokenRepository = deviceTokenRepository;
    }


    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if ( defaultConfiguration.getCreateExampleData() ) {
            try {
                loadExampleData(deviceService, projectService, projectRepository, provisioningTokenRepository, forwardingRepository, deviceRepository, deviceTokenRepository);
            } catch (Exception e) {
                log.error("Exception creating example data: {}", e.getMessage());
            }
        }
    }

    @Transactional
    public void loadExampleData(DeviceService deviceService,
                                 ProjectService projectService,
                                 ProjectRepository projectRepository,
                                 ProvisioningTokenRepository provisioningTokenRepository,
                                 ForwardingRepository forwardingRepository,
                                 DeviceRepository deviceRepository,
                                 DeviceTokenRepository deviceTokenRepository)
    {
        log.info("Creating example data");

        Project thingpulse = new Project("thingpulse", "My first IoT project", "first");
        thingpulse = projectRepository.save(thingpulse);
        Device device1 = deviceRepository.save(new Device(thingpulse, "thingpulse-0001", "", "Arbeitszimmer", "dev"));
        Device device2 = deviceRepository.save(new Device(thingpulse, "thingpluse-0002", "", "Bad", "dev lowBatt"));
        Device device3 = deviceRepository.save(new Device(thingpulse, "thingpluse-0003", "", "Balkon", ""));

        Project epaper = new Project("epaper", "EPaper management", "second");
        epaper = projectRepository.save(epaper);
        deviceRepository.save(new Device(epaper, "epaper-0001", "", "Büro Köhler", ""));
        deviceRepository.save(new Device(epaper, "epaper-0002", "", "Kaminzimmer West", ""));
        deviceRepository.save(new Device(epaper, "epaper-0003", "", "Kaminzimmer Ost", ""));

        Project temperature = new Project("Temperaturmonitoring", "Temperaturmonitoring", "");
        projectRepository.save(temperature);
        deviceRepository.save(new Device(temperature, "temperature-0001", "", "Büro Köhler", ""));
        deviceRepository.save(new Device(temperature, "temperature-0002", "", "Kaminzimmer West", ""));

        ProvisioningToken provisioningToken1 = projectService.addNewProvisioningToken(thingpulse);
        provisioningToken1.setToken("P-102-FX61O_ZLAuxWUFao55HPVyqtD5SPOkkRNxZOAn0k0Kv8USj_wJCqFGRRNZCNRm4NnxMOQ8bpJ0LFnWjTWdNt_w==");
        provisioningToken1 = provisioningTokenRepository.save(provisioningToken1);
        log.info("project=thingpulse provisioningToken1={}", provisioningToken1.getToken());
        ProvisioningToken provisioningToken2 = projectService.addNewProvisioningToken(thingpulse);
        provisioningToken2 = provisioningTokenRepository.save(provisioningToken2);
        log.info("project=thingpulse provisioningToken2={}", provisioningToken2.getToken());
        Forwarding forwarding1 = new Forwarding(thingpulse);
        thingpulse.getForwardings().add(forwarding1);
        forwarding1.setName("fwd");
        forwarding1.setForwardToUrl("https://httpbin.org/headers");
        forwarding1 = forwardingRepository.save(forwarding1);
        log.info("project=thingpulse Forwarding1={}", forwarding1.getName());

        DeviceToken deviceToken1 = deviceService.addNewDeviceToken(device1);
        deviceToken1.setToken("secret-device-token");
        deviceToken1 = deviceTokenRepository.save(deviceToken1);
        log.info("device={} deviceToken1={}", device1.getName(), deviceToken1.getToken());
        DeviceToken deviceToken2 = deviceService.addNewDeviceToken(device2);
        deviceToken2 = deviceTokenRepository.save(deviceToken2);
        log.info("device={} deviceToken1={}", device2.getName(), deviceToken2.getToken());
    }

}
