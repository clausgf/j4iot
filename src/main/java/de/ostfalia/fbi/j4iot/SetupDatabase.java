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
    UserRepository userRepository;
    RoleRepository roleRepository;


    public SetupDatabase(DefaultConfiguration defaultConfiguration,
                         ProjectService projectService,
                         DeviceService deviceService,
                         ProjectRepository projectRepository,
                         ProvisioningTokenRepository provisioningTokenRepository,
                         ForwardingRepository forwardingRepository,
                         DeviceRepository deviceRepository,
                         DeviceTokenRepository deviceTokenRepository,
                         UserRepository userRepository,
                         RoleRepository roleRepository) {
        this.defaultConfiguration = defaultConfiguration;
        this.projectService = projectService;
        this.deviceService = deviceService;
        this.projectRepository = projectRepository;
        this.provisioningTokenRepository = provisioningTokenRepository;
        this.forwardingRepository = forwardingRepository;
        this.deviceRepository = deviceRepository;
        this.deviceTokenRepository = deviceTokenRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }


    @Override
    @Transactional
    public void run(String... args) throws Exception {
        loadDefaultRoles(defaultConfiguration, roleRepository);
        loadDefaultUser(defaultConfiguration, userRepository, roleRepository);
        if ( defaultConfiguration.getCreateExampleData() ) {
            try {
                loadExampleData(deviceService, projectService, projectRepository, provisioningTokenRepository, forwardingRepository, deviceRepository, deviceTokenRepository, userRepository, roleRepository);
            } catch (Exception e) {
                log.error("Exception creating example data: {}", e.getMessage());
            }
        }
    }


    @Transactional
    public void loadDefaultRoles(DefaultConfiguration defaultConfiguration, RoleRepository roleRepository) {
        if (roleRepository.findByName(Role.Roles.ADMIN.name()).isEmpty()) {
            log.info("Creating default data for role {}", Role.Roles.ADMIN.name());
            Role ra = new Role(Role.Roles.ADMIN.name());
            ra = roleRepository.save(ra);
        }
        if (roleRepository.findByName(Role.Roles.PROJECT.name()).isEmpty()) {
            log.info("Creating default data for role {}", Role.Roles.PROJECT.name());
            Role rpm = new Role(Role.Roles.PROJECT.name());
            rpm = roleRepository.save(rpm);
        }
    }

    @Transactional
    public void loadDefaultUser(DefaultConfiguration defaultConfiguration, UserRepository userRepository, RoleRepository roleRepository) {
        String username = defaultConfiguration.getUsername();
        if ( username != null && !username.isEmpty() && userRepository.findByName(username).isEmpty() ) {
            log.info("Creating default user");
            Role ra = roleRepository.findByName(Role.Roles.ADMIN.name()).get();
            Role rpm = roleRepository.findByName(Role.Roles.PROJECT.name()).get();
            User default_user = User.builder()
                    .name(username)
                    .password(defaultConfiguration.getPassword())
                    .addRole(rpm)
                    .addRole(ra)
                    .build();
            userRepository.save(default_user);
        }
    }

    @Transactional
    public void loadExampleData(DeviceService deviceService,
                                 ProjectService projectService,
                                 ProjectRepository projectRepository,
                                 ProvisioningTokenRepository provisioningTokenRepository,
                                 ForwardingRepository forwardingRepository,
                                 DeviceRepository deviceRepository,
                                 DeviceTokenRepository deviceTokenRepository,
                                 UserRepository userRepository,
                                 RoleRepository roleRepository)
    {
        log.info("Creating example data");
        Role ra = roleRepository.findByName(Role.Roles.ADMIN.name()).get();
        Role rpm = roleRepository.findByName(Role.Roles.PROJECT.name()).get();

        User user1 = User.builder().name("fuehner").password("fuehner").firstName("Claus").lastName("Fühner").addRole(rpm).addRole(ra).email("c.fuehner@ostfalia.de").build();
        user1 = userRepository.save(user1);
        log.info("Created username={} password={}", user1.getName(), user1.getEncodedPassword());

        User user2 = User.builder().name("user").password("user").firstName("Default").lastName("User").addRole(rpm).email("email@ostfalia.de").build();
        user2 = userRepository.save(user2);
        log.info("Created username={} password={}", user2.getName(), user2.getEncodedPassword());

        Project thingpulse = new Project("thingpulse", "My first IoT project", "first");
        thingpulse.addUser(user1);
        thingpulse.addUser(user2);
        thingpulse = projectRepository.save(thingpulse);
        Device device1 = deviceRepository.save(new Device(thingpulse, "thingpulse-0001", "", "Arbeitszimmer", "dev"));
        Device device2 = deviceRepository.save(new Device(thingpulse, "thingpluse-0002", "", "Bad", "dev lowBatt"));
        Device device3 = deviceRepository.save(new Device(thingpulse, "thingpluse-0003", "", "Balkon", ""));

        Project epaper = new Project("epaper", "EPaper management", "second");
        epaper.addUser(user1);
        epaper = projectRepository.save(epaper);
        deviceRepository.save(new Device(epaper, "epaper-0001", "", "Büro Köhler", ""));
        deviceRepository.save(new Device(epaper, "epaper-0002", "", "Kaminzimmer West", ""));
        deviceRepository.save(new Device(epaper, "epaper-0003", "", "Kaminzimmer Ost", ""));

        Project temperature = new Project("Temperaturmonitoring", "Temperaturmonitoring", "");
        temperature.addUser(user2);
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
