package de.ostfalia.fbi.j4iot.data.service;

import de.ostfalia.fbi.j4iot.data.entity.Device;
import de.ostfalia.fbi.j4iot.security.TokenAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

public class ServiceUtils {

    private static final Logger log = LoggerFactory.getLogger(ServiceUtils.class);

    // ***********************************************************************

    public static Boolean isNamePatternValid(String name) {
        String namePattern = "^[a-zA-Z0-9][a-zA-Z0-9_\\-+]*$";
        return name.matches(namePattern);
    }

    public static void checkName(String name) {
        if ( name == null || name.isEmpty() || !isNamePatternValid(name) ) {
            String msg = String.format( "REST API parameter invalid name=%s", name );
            log.info( msg );
            throw new ResponseStatusException( HttpStatus.BAD_REQUEST, msg );
        }
    }

    public static Boolean isBase64Valid(String base64) {
        String base64Pattern = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$";
        return base64.matches(base64Pattern);
    }

    public static void checkBase64(String base64) {
        if ( base64 == null || base64.isEmpty() || !isBase64Valid(base64) ) {
            String msg = String.format( "REST API parameter invalid base64=%s", base64 );
            log.info( msg );
            throw new ResponseStatusException( HttpStatus.BAD_REQUEST, msg );
        }
    }

    // ***********************************************************************

    public static void checkAuthentication(String projectName)
    {
        ServiceUtils.checkName(projectName);

        try {
            TokenAuthenticationToken token = (TokenAuthenticationToken) SecurityContextHolder.getContext().getAuthentication().getDetails();
            Device device = (Device) token.getDetails();
            if ( !projectName.equals(device.getProject().getName()) ) {
                throw new Exception();
            }
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Device/project authentication failure: projectName=" + projectName);
        }
    }

    public static void checkAuthentication(String projectName, String deviceName)
    {
        ServiceUtils.checkName(projectName);
        ServiceUtils.checkName(deviceName);

        try {
            TokenAuthenticationToken token = (TokenAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            Device device = (Device) token.getDetails();
            if ( !deviceName.equals(device.getName()) ) {
                throw new Exception();
            }
            if ( !projectName.equals(device.getProject().getName()) ) {
                throw new Exception();
            }
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Device/project authentication failure: projectName=" + projectName + " deviceName=" + deviceName);
        }
    }

}
