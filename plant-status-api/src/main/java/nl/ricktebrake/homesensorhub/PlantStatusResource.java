package nl.ricktebrake.homesensorhub;

import com.google.cloud.firestore.*;
import io.smallrye.mutiny.Multi;
import lombok.Data;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

@Path("/plantstatus")
public class PlantStatusResource {

    @Inject
    private Firestore firestore;

    private static final Logger log = Logger.getLogger(PlantStatusResource.class);

    @GET
    @Path("/{deviceId}")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Multi<Long> getCurrentValueForDevice(@PathParam("deviceId") String deviceId) {

        log.info("Received request on /hello endpoint");

        return Multi.createFrom().emitter(emitter -> {
            CollectionReference sensorMeasurements = firestore.collection("moisture-sensor");
            ListenerRegistration listenerRegistration = sensorMeasurements
                    .whereEqualTo("deviceId", deviceId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(1)
                    .addSnapshotListener((value, error) -> {

                if (error != null) {
                    log.error("Error adding listener to query", error);
                    emitter.complete();
                    throw error;
                }
                log.info(String.format("New database event"));
                value.getDocuments().stream()
                        .findFirst()
                        .map(document -> document.getLong("value"))
                        .ifPresent(emitter::emit);
            });
            emitter.onTermination(listenerRegistration::remove);
        });
    }

    @GET
    @Path("/{deviceId}/historical")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Multi<MoistureValue> getValuesForTimeRange(@PathParam("deviceId") String deviceId, @QueryParam("start") String start, @QueryParam("end") String end) {
        var startDate = LocalDate.parse(start).atStartOfDay();
        var endDate = LocalDate.parse(end).atStartOfDay().plusDays(1);

        return Multi.createFrom().emitter(emitter -> {
            CollectionReference sensorMeasurements = firestore.collection("moisture-sensor");
            ListenerRegistration listenerRegistration = sensorMeasurements
                    .whereEqualTo("deviceId", deviceId)
                    .whereGreaterThan("timestamp", Date.from(startDate.toInstant(ZoneOffset.UTC)))
                    .whereLessThan("timestamp", Date.from(endDate.toInstant(ZoneOffset.UTC)))
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener((value, error) -> {

                        if (error != null) {
                            log.error("Error adding listener to query", error);
                            emitter.complete();
                            throw error;
                        }
                        log.info(String.format("New database event"));
                        value.getDocumentChanges().stream()
                                .map(document -> new MoistureValue(document.getDocument().getLong("value"),
                                        ZonedDateTime.ofInstant(document.getDocument().getDate("timestamp").toInstant(), ZoneId.of("CET"))))
                                .forEach(emitter::emit);
                    });
            emitter.onTermination(listenerRegistration::remove);
        });
    }
}

@Data
class MoistureValue {
    private final Long value;
    private final ZonedDateTime timestamp;
}