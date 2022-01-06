package nl.ricktebrake.homesensorhub;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.cloud.firestore.v1.FirestoreClient;
import com.google.cloud.firestore.v1.FirestoreSettings;
import io.quarkus.logging.Log;
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
    public Multi<Integer> hello() {

        log.info("Received request on /hello endpoint");



        return Multi.createFrom().emitter(emitter -> {
            emitter.emit(10000);

//            sensorMeasurements.orderBy("timestamp", Query.Direction.DESCENDING).limit(1).addSnapshotListener((value, error) -> {
//                if(error != null) {
//                    log.error("Error adding listener to query", error);
//                    emitter.complete();
//                    throw error;
//                }
//                value.getDocuments().stream()
//                        .map(document -> (Integer) document.getData().get("value"))
//                        .forEach(emitter::emit);
//            });
            CollectionReference sensorMeasurements = firestore.collection("moisture-sensor");
            try {
                QuerySnapshot query = sensorMeasurements.orderBy("timestamp", Query.Direction.DESCENDING).get().get();
                query.getDocuments().forEach(document -> {
                    emitter.emit((Integer) document.get("value"));
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            emitter.complete();
        });
    }
}