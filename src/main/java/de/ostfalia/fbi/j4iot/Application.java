package de.ostfalia.fbi.j4iot;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import de.ostfalia.fbi.j4iot.data.entity.Device;
import de.ostfalia.fbi.j4iot.data.entity.Project;
import de.ostfalia.fbi.j4iot.data.repository.DeviceRepository;
import de.ostfalia.fbi.j4iot.data.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */
@SpringBootApplication
@Theme(value = "j4iot")
public class Application implements AppShellConfigurator {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    @Bean
    public CommandLineRunner loadData(ProjectRepository projectRepository, DeviceRepository deviceRepository) {
        return (args -> {
            log.info("Creating example data");
            Project thingpulse = new Project("thingpulse", "My first IoT project", "first");
            thingpulse.setDeviceTagsAvailable("dev lowBatt");
            projectRepository.save(thingpulse);
            deviceRepository.save(new Device(thingpulse, "thingpluse-0001", "", "Arbeitszimmer", "dev"));
            deviceRepository.save(new Device(thingpulse, "thingpluse-0002", "", "Bad", "dev lowBatt"));
            deviceRepository.save(new Device(thingpulse, "thingpluse-0003", "", "Balkon", ""));
            Project epaper = new Project("epaper", "EPaper management", "second");
            projectRepository.save(epaper);
            deviceRepository.save(new Device(epaper, "epaper-0001", "", "Büro Köhler", ""));
            deviceRepository.save(new Device(epaper, "epaper-0002", "", "Kaminzimmer West", ""));
            deviceRepository.save(new Device(epaper, "epaper-0003", "", "Kaminzimmer Ost", ""));
        });
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
