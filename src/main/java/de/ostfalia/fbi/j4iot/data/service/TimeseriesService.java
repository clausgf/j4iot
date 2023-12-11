package de.ostfalia.fbi.j4iot.data.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.influxdb.client.*;
import com.influxdb.client.domain.Bucket;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import de.ostfalia.fbi.j4iot.configuration.InfluxdbConfiguration;
import de.ostfalia.fbi.j4iot.data.entity.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TimeseriesService {

    // ************************************************************************

    private Logger log = LoggerFactory.getLogger(TimeseriesService.class);

    private final InfluxDBClient clientApi;
    private final BucketsApi bucketsApi;
    private final WriteApiBlocking writeApi;

    private final Map<String, String> ESP_LOG_LEVELS = Map.of(
            "E", "error",
            "W", "warning",
            "I", "info",
            "D", "debug",
            "V", "verbose"
    );
    private final String ESP_LOG_REGEX = "^(?<level>[EWIDV]) \\((?<ts>\\d*)\\) (?<tag>\\w+): (?<message>.*)$";

    private final String orgName;
    private final Set<String> bucketsKnown = new HashSet<>();
    private final Pattern espLogPattern;

    // ************************************************************************

    public TimeseriesService(InfluxdbConfiguration config) {
        log.info("Using influxdb org={} url={}", config.getOrg(), config.getUrl());
        this.orgName = config.getOrg();
        clientApi = InfluxDBClientFactory.create(config.getUrl(), config.getToken().toCharArray(), orgName);
        bucketsApi = clientApi.getBucketsApi();
        writeApi = clientApi.getWriteApiBlocking();
        espLogPattern = Pattern.compile(ESP_LOG_REGEX);
    }

    // ************************************************************************

    private Organization getOrganization(String organizationName) {
        OrganizationsApi orgApi = clientApi.getOrganizationsApi();
        List<Organization> orgs = orgApi.findOrganizations();
        log.info("influxdb organizations: looking for orgName={} available: {}", organizationName, orgs);
        return orgs.stream()
                .filter(org -> org.getName().equals(organizationName))
                .findFirst()
                .orElse(null);
    }

    // ************************************************************************

    private String getBucketName(String projectName) {
        String bucketName = projectName;

        if (!bucketsKnown.contains(bucketName)) {

            Bucket bucket = bucketsApi.findBucketByName(bucketName);
            log.info("Bucket name={} orgId={}", bucketName, bucket == null ? "null" : bucket.getOrgID());

            if (bucket == null) {
                log.info("Creating bucket {}", bucketName);

                Organization org = getOrganization(orgName);

                bucket = new Bucket();
                bucket.setName(bucketName);
                bucket.setOrgID(org.getId());
                bucket.setDescription( "j4iot bucket for projectName=" + projectName );
                // bucket.getRetentionRules().add(bucketRetentionRules);
                //buckets.createBucket(bucketName, org);
                bucketsApi.createBucket(bucket);
            }
            bucketsKnown.add(bucketName);
        }
        return bucketName;
    }

    // ************************************************************************

    public Boolean ping() {
        return clientApi.ping();
    }

    // ************************************************************************

    private void collectJsonInPoint(Point point, String key, JsonNode jsonNode) {
        if (jsonNode.isObject()) { // recursively process object nodes
            for (Iterator<Map.Entry<String, JsonNode>> it = jsonNode.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = it.next();
                String newKey = key + ( key.isEmpty() ? "" : "_" ) + entry.getKey();
                collectJsonInPoint(point, newKey, entry.getValue());
            }
        } else if (jsonNode.isArray()) {
            log.error("processJsonNode cannot process arrays");
        } else if (jsonNode.isBoolean()) {
            point.addField(key, jsonNode.asBoolean());
        } else if (jsonNode.isNumber()) {
            point.addField(key, jsonNode.asDouble());
        } else if (jsonNode.isValueNode()) {
            point.addField(key, jsonNode.asText());
        }
    }

    public void writeTelemetryJson(Device device, String kind, String jsonStr) throws JsonProcessingException {
        Instant time = Instant.now();
        String bucketName = getBucketName(device.getProject().getName());
        log.debug(jsonStr);

        // add the datapoint
        Point point = new Point(kind); // use kind as a measurement identifier
        point
                .time(time, WritePrecision.MS)
                .addTag("device_id", device.getId().toString())
                .addTag("device_name", device.getName())
                .addTag("project_id", device.getProject().getId().toString())
                .addTag("project_name", device.getProject().getName());

        // collect json fields into influxdb point fields
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(jsonStr);
        collectJsonInPoint(point, "", jsonNode);

        writeApi.writePoint(bucketName, orgName, point);
    }

    // ************************************************************************

    private String addEspTags(Point point, String line) {
        Matcher matcher = espLogPattern.matcher(line);

        if (matcher.matches()) {
            String espLevel = matcher.group("level");
            String level = ESP_LOG_LEVELS.get(espLevel);
            point.addTag("level", level);
            //point.addField("ts", matcher.group("ts"));
            point.addTag("tag", matcher.group("tag"));
            return matcher.group("message");
        }
        return line;
    }

    public void writeLog(Device device, String line) {
        Instant time = Instant.now();
        String bucketName = getBucketName(device.getProject().getName());

        Point point = new Point("log");
        point
                .time(time, WritePrecision.NS)
                .addTag("device_id", device.getId().toString())
                .addTag("device_name", device.getName())
                .addTag("project_id", device.getProject().getId().toString())
                .addTag("project_name", device.getProject().getName());

        line = addEspTags(point, line);
        point.addField("message", line);
        log.info("Logging t={} project={} device={} message={}", time, device.getName(), device.getProject().getName(), line);
        writeApi.writePoint(bucketName, orgName, point);
    }

    // ************************************************************************

}
