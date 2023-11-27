package de.ostfalia.fbi.j4iot.data.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ServiceUtils {

    private static final Logger log = LoggerFactory.getLogger(ServiceUtils.class);

    // ***********************************************************************

    public static Boolean isNamePatternValid(String name) {
        String namePattern = "^[a-zA-Z0-9][a-zA-Z0-9_\\-+]*$";
        return name.matches(namePattern);
    }

    public static void checkName(String name) {
        if ( !isNamePatternValid(name) ) {
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
        if ( !isBase64Valid(base64) ) {
            String msg = String.format( "REST API parameter invalid base64=%s", base64 );
            log.info( msg );
            throw new ResponseStatusException( HttpStatus.BAD_REQUEST, msg );
        }
    }

    // ***********************************************************************

}
