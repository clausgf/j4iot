package de.ostfalia.fbi.j4iot;

import de.ostfalia.fbi.j4iot.data.entity.*;
import de.ostfalia.fbi.j4iot.data.repository.DeviceRepository;
import de.ostfalia.fbi.j4iot.data.repository.ProjectRepository;
import de.ostfalia.fbi.j4iot.data.repository.UserRepository;
import de.ostfalia.fbi.j4iot.data.service.IotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadDatabase {

    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    public CommandLineRunner loadData(IotService iotService, ProjectRepository projectRepository, DeviceRepository deviceRepository, UserRepository userRepository) {
        return (args -> {
            log.info("Creating example data");
            User user1 = User.builder().name("fuehner").password("fuehner").firstName("Claus").lastName("Fühner").email("c.fuehner@ostfalia.de").build();
            user1 = userRepository.save(user1);
            log.info("Created username={} password={}", user1.getName(), user1.getEncodedPassword());
            User user2 = User.builder().name("user").password("user").firstName("Default").lastName("User").email("email@ostfalia.de").build();
            user2 = userRepository.save(user2);
            log.info("Created username={} password={}", user2.getName(), user2.getEncodedPassword());
            User user3 = User.builder().name("admin").password("admin").firstName("Admin").lastName("User").email("email@ostfalia.de").build();
            user3 = userRepository.save(user3);
            log.info("Created username={} password={}", user3.getName(), user3.getEncodedPassword());

            Project thingpulse = new Project("thingpulse", "My first IoT project", "first");
            thingpulse.setDeviceTagsAvailable("dev lowBatt");
            projectRepository.save(thingpulse);
            Device device1 = deviceRepository.save(new Device(thingpulse, "thingpluse-0001", "", "Arbeitszimmer", "dev"));
            Device device2 = deviceRepository.save(new Device(thingpulse, "thingpluse-0002", "", "Bad", "dev lowBatt"));
            Device device3 = deviceRepository.save(new Device(thingpulse, "thingpluse-0003", "", "Balkon", ""));

            Project epaper = new Project("epaper", "EPaper management", "second");
            projectRepository.save(epaper);
            deviceRepository.save(new Device(epaper, "epaper-0001", "", "Büro Köhler", ""));
            deviceRepository.save(new Device(epaper, "epaper-0002", "", "Kaminzimmer West", ""));
            deviceRepository.save(new Device(epaper, "epaper-0003", "", "Kaminzimmer Ost", ""));

            ProvisioningToken provisioningToken1 = iotService.createProvisioningToken(thingpulse);
            log.info("project=thingpulse provisioningToken1={}", provisioningToken1.getToken());
            ProvisioningToken provisioningToken2 = iotService.createProvisioningToken(thingpulse);
            log.info("project=thingpulse provisioningToken2={}", provisioningToken2.getToken());

            DeviceToken deviceToken1 = iotService.createDeviceToken(provisioningToken1, device1);
            log.info("device={} provisioningToken1={}", device1.getName(), deviceToken1.getToken());
            DeviceToken deviceToken2 = iotService.createDeviceToken(provisioningToken1, device2);
            log.info("device={} provisioningToken1={}", device2.getName(), deviceToken2.getToken());
        });
    }
}
