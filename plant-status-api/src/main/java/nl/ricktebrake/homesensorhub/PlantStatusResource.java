package nl.ricktebrake.homesensorhub;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.v1.FirestoreClient;
import com.google.cloud.firestore.v1.FirestoreSettings;
import io.smallrye.mutiny.Multi;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hello")
public class PlantStatusResource {

    @Inject
    private Firestore firestore;

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Multi<Integer> hello() {

        return Multi.createFrom().emitter(emitter -> {
            CollectionReference sensorMeasurements = firestore.collection("moisture-sensor");
            sensorMeasurements.orderBy("timestamp", Query.Direction.DESCENDING).limit(1).addSnapshotListener((value, error) -> {
                emitter.emit(value.getDocuments()
                        .stream()
                        .findFirst()
                        .map(document -> (Integer) document.getData().get("value")).orElse(100));
            });
        });
    }
}