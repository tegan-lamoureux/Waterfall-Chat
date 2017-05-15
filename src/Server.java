import java.util.Random;
import java.util.Vector;

/**
 * Created by tegan on 5/11/2017.
 */
public class Server {
    private class User{
        private String username = null;
        private String password = null;
        private boolean logged_in = false;
        private int client_id = 0;

        public User(String username, String password){
            this.username = username;
            this.password = password;
        }

        public boolean set_username(String username){
            if(username != null){
                this.username = username;
                return true;
            }
            else
                return false;
        }
        public boolean set_password(String password){
            if(password != null) {
                this.password = password;
                return true;
            }
            else
                return false;
        }

        public boolean check_password(String password){
            return (password == this.password);
        }

        public String get_username(){
            return username;
        }
    }

    private Vector<Integer> connected_clients = new Vector<>();
    private Vector<User> user_list = new Vector<>();

    public boolean check_user_exists(String username){
        boolean user_found = false;

        for(User single: user_list)
            if(single.get_username().equals(username))
                user_found = true;

        return user_found;
    }

    public boolean create_user_account(String username, String password){
        if(username != null && password != null){
            user_list.add(new User(username, password));
            return true;
        }
        else
            return false;
    }

    public boolean login(){
        return false;
    }

    // Will "connect" a client to the server. What this entails is finding a unique 6-digit client ID and
    // adding that client ID to a list of "connected clients". It also returns the client ID, which the client
    // should keep and set internally for future communication with the server.
    public int connect(){
        Random random = new Random();

        int client_id = random.nextInt(999999) + 100000;
        boolean valid_id = false;

        // Loop through all of our connected clients and check to see if the new client ID is unique.
        // If so, great, use it. If not, generate a new one and restart the search.
        while(!valid_id){
            valid_id = true; // assume validity until proven otherwise
            for (int i: connected_clients) {
                if (i == client_id) { // if we found a duplicate ID
                    client_id = random.nextInt(999999) + 100000; // generate new ID

                }
            }
        }

        if(client_id != -1)
            connected_clients.add(client_id);

        return client_id;
    }

    public boolean disconnect(int client_id){
        boolean found = false;

        for(int i = 0; i < connected_clients.size(); ++i){
            if(connected_clients.get(i) == client_id){
                found = true;
                connected_clients.remove(i);
            }
        }

        return found;
    }
}
