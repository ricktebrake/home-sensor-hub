package nl.ricktebrake.homesensorhub;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import io.smallrye.mutiny.Multi;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.ExecutionException;

@Path("/hello")
public class PlantStatusResource {

    @Inject
    private Firestore firestore;

    private static final Logger log = Logger.getLogger(PlantStatusResource.class);

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Multi<Long> hello() {

        log.info("Received request on /hello endpoint");


        return Multi.createFrom().emitter(emitter -> {
            emitter.emit(10000L);
            CollectionReference sensorMeasurements = firestore.collection("moisture-sensor");
            sensorMeasurements.orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener((value, error) -> {;
                if (error != null) {
                    log.error("Error adding listener to query", error);
                    emitter.complete();
                    throw error;
                }
                log.info(String.format("New database event"));
                value.getDocuments().stream()
                        .map(document -> document.getLong("value"))
                        .forEach(emitter::emit);
            });
        });
    }
}