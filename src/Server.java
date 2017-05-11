import java.util.Random;
import java.util.Vector;

/**
 * Created by tegan on 5/11/2017.
 */
public class Server {
    private Vector<Integer> connected_clients = new Vector<>();

    public int connect(){
        Random random = new Random();

        int client_id = random.nextInt(999999) + 100000;

        // Loop through all of our connected clients and check to see if the new client ID is unique.
        // If so, great, use it. If not, generate a new one and restart the search.
        // Also watch out for an error condition, and if no result in 100000 tries, exit.
        for(int i = 0, timeout = 100000; i < connected_clients.size() && timeout > 0; ++i, --timeout){
            if(connected_clients.get(i) == client_id){ // if we found a duplicate ID
                i = 0; // restart search
                client_id = random.nextInt(999999) + 100000; // generate new ID
                //System.out.println("ERROR: clid match; restarting.");
            }

            if(timeout == 0)
                client_id = -1;
        }

        if(client_id != -1)
            connected_clients.add(client_id);

        return client_id;
    }
}
