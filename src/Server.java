import com.sun.xml.internal.ws.commons.xmlutil.Converter;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

//TODO: 2. Get sockets working.
//TODO: 3. Write/Read from file on startup and shutdown.

/**
 * Created by tegan on 5/11/2017.
 */
public class Server {
    ////////////////////////////////////////////////////
    // Private Class Variables   ///////////////////////
    ////////////////////////////////////////////////////

    // Define my data storage. Using two hash maps, one to keep a record of all created accounts
    // on the server (user account list; hashed by username to user account), and one to keep a
    // list of currently logged in users (connected clients; hashed by client ID to username).
    private ConcurrentHashMap<String, User_Account> user_account_list = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, String >     connected_clients = new ConcurrentHashMap<>();

    // Main server socket; used when clients connect.
    private ServerSocket my_server = null;



    ////////////////////////////////////////////////////
    // Private Sub-Classes and Constructors   //////////
    ////////////////////////////////////////////////////

    // Class to store the functionality of a single user's account.
    private class User_Account{
        private String username = null;
        private String password = null;
        private boolean logged_in = false;
        private int client_id = 0;
        private Vector<String> chat_history = new Vector<>();

        public User_Account(String username, String password){
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

    // Instance of server for a client. Multiple instances, each one spawned after a new
    // client connects. Handles communication.
    private class Single_Client_Server extends Thread{
        private Socket socket = null;

        public Single_Client_Server(Socket socket){
            super("single_client_server");
            this.socket = socket;
        }

        public void run (){
            try {
                // just ping back for now.
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                boolean alive = true;

                while(alive) {
                    if(in.ready()) {
                        switch (in.readLine()){
                            case "__get_users":
                                //System.out.println("in case");
                                out.writeBytes("__user_list" + '\n');

                                for(String name: connected_clients.values()){
                                    out.writeBytes(name + '\n');
                                    System.out.println(name);
                                }

                                out.writeBytes("__finished" + '\n');
                                break;

                            case "__kill":
                                alive = false;
                                break;
                        }
                    }
                    Thread.sleep(500);
                }
            }
            catch(Exception e){}
        }
    }

    // Main constructor. Contains main server thread/loop.
    public Server() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    my_server = new ServerSocket(6789);

                    boolean listen = true;
                    while (listen) { // TODO fix this true
                        Socket clientSocket = my_server.accept();
                        Single_Client_Server single = new Single_Client_Server(clientSocket);
                        single.start();
                        Thread.sleep(500);
                    }

                    my_server.close();
                }
                catch (Exception e){System.out.println("Server creation error!" + e.toString());}
            }
        });
        t.start();
    }



    ////////////////////////////////////////////////////
    // Server Methods    ///////////////////////////////
    ////////////////////////////////////////////////////

    // converted to int instead of bool, update required code
    public boolean check_client_connected(int client_id){
        return connected_clients.containsKey(client_id);
    }

    public boolean check_user_exists(String username){
        return user_account_list.containsKey(username);
    }

    public String[] get_current_users(){
        String[] users = new String[connected_clients.size()];

        int i = 0;
        for(String username: connected_clients.values()){
            users[i++] = username;
        }

        return users;
    }

    public boolean create_user_account(String username, String password){
        if(username != null && password != null){
            user_account_list.putIfAbsent(username, new User_Account(username, password));
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

            System.out.println("Test; clid generated: " + client_id);

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
