package de.ostfalia.fbi.j4iot;

import de.ostfalia.fbi.j4iot.data.entity.*;
import de.ostfalia.fbi.j4iot.data.repository.*;
import de.ostfalia.fbi.j4iot.data.service.DeviceService;
import de.ostfalia.fbi.j4iot.data.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SetupDatabaseLoader {

    private static final Logger log = LoggerFactory.getLogger(SetupDatabaseLoader.class);

    @Bean
    public CommandLineRunner loadData(DefaultConfiguration defaultConfiguration,
                                      DeviceService deviceService,
                                      ProjectService projectService,
                                      ProjectRepository projectRepository,
                                      ProvisioningTokenRepository provisioningTokenRepository,
                                      DeviceRepository deviceRepository,
                                      DeviceTokenRepository deviceTokenRepository,
                                      UserRepository userRepository,
                                      RoleRepository roleRepository) {
        return (args -> {
            log.info("Creating default data: critical roles");
            Role ra = new Role("ADMIN");
            ra = roleRepository.save(ra);
            Role rpm = new Role("PROJECT_MANAGER");
            rpm = roleRepository.save(rpm);

            String username = defaultConfiguration.getUsername();
            if (username != null && !username.isEmpty()) {
                log.info("Creating default data: default user");
                User default_user = User.builder().name(username).password(defaultConfiguration.getPassword()).addRole(rpm).addRole(ra).build();
                userRepository.save(default_user);
            }

            log.info("Creating example data");
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
            provisioningToken1.setToken("secret-provisioning-token");
            provisioningToken1 = provisioningTokenRepository.save(provisioningToken1);
            log.info("project=thingpulse provisioningToken1={}", provisioningToken1.getToken());
            ProvisioningToken provisioningToken2 = projectService.addNewProvisioningToken(thingpulse);
            provisioningToken2 = provisioningTokenRepository.save(provisioningToken2);
            log.info("project=thingpulse provisioningToken2={}", provisioningToken2.getToken());

            DeviceToken deviceToken1 = deviceService.addNewDeviceToken(device1);
            deviceToken1.setToken("secret-device-token");
            deviceToken1 = deviceTokenRepository.save(deviceToken1);
            log.info("device={} deviceToken1={}", device1.getName(), deviceToken1.getToken());
            DeviceToken deviceToken2 = deviceService.addNewDeviceToken(device2);
            deviceToken2 = deviceTokenRepository.save(deviceToken2);
            log.info("device={} deviceToken1={}", device2.getName(), deviceToken2.getToken());
        });
    }
}
