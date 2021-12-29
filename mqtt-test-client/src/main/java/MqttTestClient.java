import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.sql.Date;
import java.time.Instant;
import java.util.Properties;
import java.util.stream.IntStream;

public class MqttTestClient {
    public static void main(String[] args) throws MqttException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        try(MqttClient mqttClient = new MqttClient("ssl://mqtt.googleapis.com:8883", "projects/home-sensor-hub/locations/europe-west1/registries/sensor-registry/devices/test-device", new MemoryPersistence())) {
            mqttClient.connect(createMqttOptions(args[0]));

            var mqttTopic = String.format("/devices/%s/%s", "test-device", "events");
            IntStream.rangeClosed(0, Integer.parseInt(args[1])).forEach(i -> {
                var payload = String.format("message-%d", i);
                var message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
                message.setQos(0);
                try {
                    mqttClient.publish(mqttTopic, message);
                    Thread.sleep(1000);
                } catch (MqttException | InterruptedException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            });
            if (mqttClient.isConnected()) {
                mqttClient.disconnect();
            }
        } catch (MqttException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static MqttConnectOptions createMqttOptions(String publicKeyLocation) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        var options = new MqttConnectOptions();
        options.setUserName("undefined");
        options.setPassword(createJwt(publicKeyLocation).toCharArray());
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
        Properties sslProps = new Properties();
        sslProps.setProperty("com.ibm.ssl.protocol", "TLSv1.2");
        options.setSSLProperties(sslProps);
        return options;
    }

    public static String createJwt(String keyFilePath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        Instant now = Instant.now();
        JwtBuilder jwtBuilder = Jwts.builder()
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(1200)))
                .setAudience("home-sensor-hub");

        byte[] keyBytes = Files.readAllBytes(Paths.get(keyFilePath));
        var spec = new PKCS8EncodedKeySpec(keyBytes);
        var keyFactory = KeyFactory.getInstance("RSA");

        return jwtBuilder.signWith(keyFactory.generatePrivate(spec), SignatureAlgorithm.RS256).compact();
    }
}