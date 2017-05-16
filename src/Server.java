import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;
import java.rmi.server.ExportException;
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

        public boolean connect(int client_id){
            if(client_id > 0){
                this.client_id = client_id;
                this.logged_in = true;

                return true;
            }
            else
                return false;
        }
    }

    ServerSocket my_server = null;

    public Server(){
        try{
            my_server = new ServerSocket(2000);
        }
        catch (Exception e){System.out.println("Server creation error!");}

        Runnable r = new Runnable() {
            @Override
            public void run() {
                boolean kill_server = false;

                while(!kill_server){
                    // server io here
                    try {
                        Socket connection_socket = my_server.accept();

                        BufferedReader in_from_client = new BufferedReader(new InputStreamReader(connection_socket.getInputStream()));
                    }
                    catch (Exception e){System.out.println("Socket IO error!");}

                    // parse input from clients here
                }
            }
        };
    }

    // TODO: Write these to file and read in on shutdown/startup.
    private Vector<Integer> connected_clients = new Vector<>();

    // TODO: fix this, maybe convert to hash table
    private Vector<User> user_list = new Vector<>();

    // converted to int instead of bool, update required code
    public int check_client_connected(int client_id){
        for(int i: connected_clients)
            if(i == client_id)
                return i;

        return -1;
    }

    public int check_user_exists(String username){
        for(int i = 0; i < user_list.size(); ++i)
            if(user_list.get(i).get_username().equals(username))
                return i;

        return -1;
    }

    public boolean create_user_account(String username, String password){
        if(username != null && password != null){
            user_list.add(new User(username, password));
            return true;
        }
        else
            return false;
    }

    public boolean login(int client_id, String username){
        int user = this.check_user_exists(username);
        int client = this.check_client_connected(client_id);

        if(user != -1 && client != -1){
            user_list.get(user).connect(client_id);
            this.send_all(client_id, user_list.get(user).get_username() + " has signed in!");
            return true;
        }
        else
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
        // FIXME: this code looks bad/is awkward. re-write.
        while(!valid_id){
            valid_id = true; // assume validity until proven otherwise
            for (int i: connected_clients) {
                if (i == client_id) { // if we found a duplicate ID
                    client_id = random.nextInt(999999) + 100000; // generate new ID
                    valid_id = false;
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

    public boolean send_all(int client_id, String message){
        // send a message here
        return true;
    }
}
