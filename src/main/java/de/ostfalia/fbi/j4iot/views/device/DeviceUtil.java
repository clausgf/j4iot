package de.ostfalia.fbi.j4iot.views.device;

import de.ostfalia.fbi.j4iot.data.entity.Device;

public class DeviceUtil {
    public static String getPageTitle(String page, Device device){
        String title = String.format("%s for unknown", page);
        if (device != null) {
            title = String.format("%s: %s (%s)", page, device.getName(), device.getProject().getName());
        }
        return title;
    }
}
