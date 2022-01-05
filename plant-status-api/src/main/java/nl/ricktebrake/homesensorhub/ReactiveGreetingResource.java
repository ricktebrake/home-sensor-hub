package nl.ricktebrake.homesensorhub;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hello")
public class ReactiveGreetingResource {

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Multi<Integer> hello() {
        return Multi.createFrom().generator(() -> 1, (i, emitter) -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            emitter.emit(i+1);
            return i+1;
        });
    }
}