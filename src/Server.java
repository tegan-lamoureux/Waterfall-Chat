import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;
import java.rmi.server.ExportException;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

//TODO: 2. Get sockets working.
//TODO: 3. Write/Read from file on startup and shutdown.

/**
 * Created by tegan on 5/11/2017.
 */
public class Server {
    private class User{
        private String username = null;
        private String password = null;
        private boolean logged_in = false;
        private int client_id = 0;
        private Vector<String> chat_history = new Vector<>();

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

        public void disconnect(){
            this.client_id = 0;
            this.logged_in = false;
        }
    }

    private ConcurrentHashMap<Integer, String > connected_clients = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, User> user_account_list = new ConcurrentHashMap<>();
    private ServerSocket my_server = null;

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

    // converted to int instead of bool, update required code
    public boolean check_client_connected(int client_id){
        return connected_clients.containsKey(client_id);
    }

    public boolean check_user_exists(String username){
        return user_account_list.containsKey(username);
    }

    public boolean create_user_account(String username, String password){
        if(username != null && password != null){
            user_account_list.putIfAbsent(username, new User(username, password));
            return true;
        }
        else
            return false;
    }

    public int login(String username, String password){
        boolean user_exists = user_account_list.containsKey(username);
        boolean valid_credentials = user_account_list.get(username).check_password(password);

        // if the user exists in the account database and the password matches the stored password, then proceed
        if(user_exists && valid_credentials) {
            Random random = new Random();

            int client_id = random.nextInt(999999) + 100000;
            boolean valid_id = false;

            // Loop through all of our connected clients and check to see if the new client ID is unique.
            // If so, great, use it. If not, generate a new one and restart the search.
            while (!valid_id) {
                if (connected_clients.containsKey(client_id)) {
                    client_id = random.nextInt(999999) + 100000;
                }
                else {
                    valid_id = true;
                }
            }

            // now that we have a valid client id, let's add it to the list of connected clients...
            connected_clients.putIfAbsent(client_id, username);

            // and update the user account info
            user_account_list.get(username).connect(client_id);

            return client_id;
        }
        else if(user_exists) // password doesn't match
            return -1;
        else // user doesn't exist
            return -2;
    }


    public boolean disconnect(int client_id){
        String username = null;

        if(connected_clients.containsKey(client_id)){
            username = connected_clients.get(client_id);
            connected_clients.remove(client_id);
            user_account_list.get(username).disconnect();

            return true;
        }
        else
            return false;
    }

    public boolean send_all(int client_id, String message){
        // send a message here
        return true;
    }
}
