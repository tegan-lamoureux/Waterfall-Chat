import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

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
    private Vector<String> connected_clients = new Vector<>();

    // Main server socket; used when clients connect.
    private ServerSocket my_server = null;

    private boolean public_message_ready = false;
    private String public_message;

    private boolean private_message_ready = false;
    private String private_message;
    private String private_user;

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
            System.out.println("in here");
            return (password.equals(this.password));
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

                String username;
                String password;

                while(alive) {
                    if(public_message_ready){
                        out.writeBytes("__message\n");
                        out.flush();
                        Thread.sleep(100);
                        out.writeBytes(public_message + '\n');
                        out.flush();
                        public_message_ready = false;
                    }

                    if(private_message_ready){
                        out.writeBytes("__message_private\n");
                        out.flush();
                        Thread.sleep(100);
                        out.writeBytes(private_user + '\n');
                        out.flush();
                        Thread.sleep(100);
                        out.writeBytes(private_message + '\n');
                        out.flush();
                        private_message_ready = false;
                    }

                    if(in.ready()) {
                        switch (in.readLine()){
                            case "__get_users":
                                out.writeBytes("__user_list" + '\n');
                                out.flush();
                                for(String name: connected_clients){
                                    out.writeBytes(name + '\n');
                                    out.flush();
                                }

                                out.writeBytes("__finished" + '\n');
                                out.flush();
                                break;

                            case "__message":
                                System.out.println("in message");
                                public_message_ready = true;

                                boolean message_recieved = false;

                                while (!message_recieved) {
                                    if (in.ready()) {
                                        public_message = in.readLine();
                                        message_recieved = true;
                                    }
                                    Thread.sleep(100);
                                }
                                break;

                            case "__message_private":
                                private_message_ready = true;

                                message_recieved = false;
                                while (!message_recieved) {
                                    if (in.ready()) {
                                        private_message = in.readLine();
                                        message_recieved = true;
                                    }
                                    Thread.sleep(100);
                                }

                                message_recieved = false;
                                while (!message_recieved) {
                                    if (in.ready()) {
                                        private_user = in.readLine();
                                        message_recieved = true;
                                    }
                                    Thread.sleep(100);
                                }
                                break;

                            case "__new_account":
                                System.out.println("in new acct");
                                username = null;
                                password = null;

                                while(!in.ready()) {
                                    Thread.sleep(10);
                                }
                                username = in.readLine();

                                while(!in.ready()) {
                                    Thread.sleep(10);
                                }
                                password = in.readLine();

                                create_user_account(username, password);

                                System.out.println("New Account Created for " + username + ".");
                                break;

                            case "__login":
                                username = null;
                                password = null;

                                while(!in.ready()) {
                                    Thread.sleep(10);
                                }
                                username = in.readLine();

                                while(!in.ready()) {
                                    Thread.sleep(10);
                                }
                                password = in.readLine();

                                Random random = new Random();

                                if(user_account_list != null &&
                                        user_account_list.containsKey(username) &&
                                        user_account_list.get(username).check_password(password) &&
                                        !connected_clients.contains(username)) {
                                    out.writeBytes("__valid_credentials" + '\n');
                                    public_message_ready = true;
                                    public_message = "User " + username + " has logged on!\n";
                                    connected_clients.add(username);
                                }
                                else {
                                    out.writeBytes("__invalid_credentials" + '\n');
                                }
                                break;

                            case "__logout":
                                username = null;

                                while(!in.ready()) {
                                    Thread.sleep(10);
                                }
                                username = in.readLine();

                                if(user_account_list != null &&
                                        user_account_list.containsKey(username) &&
                                        connected_clients.contains(username)) {
                                    out.writeBytes("__valid_credentials" + '\n');
                                    public_message_ready = true;
                                    public_message = "User " + username + " has logged off!\n";
                                    connected_clients.remove(username);
                                }
                                else {
                                    out.writeBytes("__invalid_credentials" + '\n');
                                }
                                break;

                            case "__kill":
                                alive = false;
                                break;
                        }
                    }
                    Thread.sleep(100);
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
                        Thread.sleep(100);
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

    public boolean check_user_exists(String username){
        return user_account_list.containsKey(username);
    }

    public String[] get_current_users(){
        String[] users = new String[connected_clients.size()];

        int i = 0;
        for(String username: connected_clients){
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


    public boolean send_all(int client_id, String message){
        // send a message here
        return true;
    }
}
