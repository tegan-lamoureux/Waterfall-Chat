import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.io.IOException;

/**
 * Created by Tegan on 5/11/2017.
 *
 * This is my client class, which handles all of the functionality of a client instantiation,
 * including window creation, user input and output, and chat functionality. All of the UI
 * for my chat is here.
 */

//TODO 1. order the users alphabetically
//TODO 2. get scrolling working (on user pane now), and fix double send issue
//TODO 3. get PM working
//TODO 4. get user history working
//TODO 5. get username display in chat working
//TODO 6. get login/new user window up and running

public class Client {

    //region ////////// Private Class Variables //////////
    //////////////////////////////////////////////////////
    private boolean connected = false;
    private boolean logged_in = false;
    private boolean login_ready = false;
    private int my_id = 0;
    private String my_username;

    private boolean message_waiting = false;
    private String message;
    //endregion


    //region ////////// SWING Window Components //////////
    //////////////////////////////////////////////////////
    private JFrame         messages_window = new JFrame("Chat Window"); // Jframe for main window.
    private JFrame         account_window  = new JFrame("Create New Account");
    private JTextArea      messages_pane   = new JTextArea(); // Main messages pane.
    private JScrollPane    messages_scroll = null;            // set up during window creation
    private JTextPane      send_pane       = new JTextPane(); // Text box to send messages.
    private JTextPane      user_pane       = new JTextPane(); // User list.
    private JMenuBar       main_menu       = new JMenuBar();  // Main menu bar.
    private JButton        login_button = null;
    private JButton        account_button = null;
    private JTextField     username = null;
    private JPasswordField password = null;
    //endregion

    //region ////////// Event Handler Classes ////////////
    //////////////////////////////////////////////////////
    private class HandleInputEnter implements KeyListener {
        @Override
        public void keyPressed(KeyEvent e) {
            if(e.getKeyChar() == '\n'){
                e.consume(); // get rid of my return so it doesn't muck up my send field

                messages_pane.setRows(1 + messages_pane.getRows());
                message_waiting = true;
                message = send_pane.getText();
                send_pane.setText("");

                messages_pane.append('\n' + message);
                messages_pane.selectAll();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {}
        @Override
        public void keyTyped(KeyEvent e) {}
    }

    private class Login implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            login_button.setEnabled(false);
            username.setEnabled(false);
            password.setEnabled(false);
            login_ready = true;
        }
    }

    private class Account implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            login_button.setEnabled(false);
            username.setEnabled(false);
            password.setEnabled(false);
            login_ready = true;

            javax.swing.SwingUtilities.invokeLater(Client.this::create_account_window);
        }
    }

    private class Create implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            //JOptionPane.showMessageDialog(null, "message here", "InfoBox: " + "title here", JOptionPane.INFORMATION_MESSAGE);
            /*messages_pane.setRows(1 + messages_pane.getRows());
            message_waiting = true;
            message = send_pane.getText();
            send_pane.setText("");

            messages_pane.append('\n' + message);
            messages_pane.selectAll();*/

            //TODO: left off here, get account message to send to server
        }
    }
    //endregion

    //region ////////// Constructors /////////////////////
    public Client() {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI

        Thread input_from_server_thread = new Thread(() -> {
            try {
                boolean alive = true;

                Socket socket = new Socket("localhost", 6789);
                DataOutputStream out_to_server = new DataOutputStream(socket.getOutputStream());
                BufferedReader in_from_server = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                connected = true;

                while (alive) {
                    if(logged_in) {
                        if (message_waiting) {
                            message_waiting = false;
                            out_to_server.writeBytes("__message\n");
                            Thread.sleep(100);
                            out_to_server.writeBytes(message + '\n');
                            message = "";
                            System.out.println("Cleared message.");
                        }

                        if (in_from_server.ready()) {
                            switch (in_from_server.readLine()) {
                                case "__user_list":
                                    String name = "";
                                    user_pane.setText("");

                                    // TODO: add timeout
                                    while (!name.equals("__finished")) {
                                        if (in_from_server.ready()) {
                                            name = in_from_server.readLine();
                                            if (!name.equals("__finished")) {
                                                user_pane.setText(user_pane.getText() + name + '\n');
                                            }
                                        }
                                    }
                                    break;

                                case "__message":
                                    // TODO: add timeout
                                    boolean message_received = false;

                                    while (!message_received) {
                                        if (in_from_server.ready()) {
                                            messages_pane.append('\n' + in_from_server.readLine());
                                            messages_pane.setRows(1 + messages_pane.getRows());
                                            message_received = true;
                                        }
                                        Thread.sleep(100);
                                    }
                                    break;

                                case "__kill":
                                    alive = false;
                                    break;
                            }
                        }

                        out_to_server.writeBytes("__get_users\n");
                    }
                    else{
                        logged_in = true;
                    }

                    Thread.sleep(100);
                }
                socket.close();
            }
            catch (Exception e) {}
        });

        input_from_server_thread.start();

        javax.swing.SwingUtilities.invokeLater(this::create_main_window);
    }

    public Client(int client_id) throws IOException{
        this();
        this.connect(client_id);
    }
    //endregion

    //region ////////// Private Methods //////////////////


    private void create_main_window(){
        // I need a place for login credentials at the top. If valid, logs in, if invalid, shows the new user
        // window signup.

        //Create and set up the window.
        messages_window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //Set up the content pane.
        Container pane = messages_window.getContentPane();

        // login shit
        JPanel login = new JPanel(new FlowLayout());

        login_button = new JButton("Login");
        account_button = new JButton("New Account");
        username = new JTextField(10);
        password = new JPasswordField(10);

        login_button.addActionListener(new Login());
        account_button.addActionListener(new Account());

        login.add(new JLabel("Username:"));
        login.add(username);
        login.add(new JLabel("Password:"));
        login.add(password);
        login.add(login_button);
        login.add(account_button);

        pane.add(login, BorderLayout.NORTH);
        // end login shit



        messages_pane.setPreferredSize(new Dimension(200, 300));
        messages_pane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Messages"),
                        BorderFactory.createEmptyBorder(5,5,5,5)),
                messages_pane.getBorder()));
        messages_pane.setEditable(false);
        if(!this.connected) {
            messages_pane.setText("Disconnected. Please log in.");
        }

        DefaultCaret caret = (DefaultCaret)messages_pane.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        messages_scroll = new JScrollPane(messages_pane);

        pane.add(messages_scroll, BorderLayout.CENTER);

        // TODO: working on getting messages pane to scroll correctly.
        messages_window.setPreferredSize(new Dimension(600, 375));

        user_pane.setPreferredSize(new Dimension(150, 300));
        user_pane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Users"),
                        BorderFactory.createEmptyBorder(5,5,5,5)),
                user_pane.getBorder()));
        pane.add(user_pane, BorderLayout.LINE_END);

        send_pane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Input"),
                        BorderFactory.createEmptyBorder(1,1,1,1)),
                send_pane.getBorder()));
        send_pane.requestFocusInWindow();

        send_pane.addKeyListener(new HandleInputEnter());
        pane.add(send_pane, BorderLayout.PAGE_END);

        //Use the content pane's default BorderLayout. No need for
        //setLayout(new BorderLayout());
        //Display the window.
        messages_window.pack();
        messages_window.setVisible(true);

        // Default the cursor to the send text field.
        send_pane.requestFocusInWindow();
    }

    private void create_account_window(){
        //Create and set up the window.
        account_window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //Set up the content pane.
        Container pane = account_window.getContentPane();

        // login shit
        JPanel login = new JPanel();

        login.setLayout(new BoxLayout(login, BoxLayout.Y_AXIS));
        login.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JButton create = new JButton("Create Account");
        JTextField c_username  = new JTextField(15);
        JTextField c_password  = new JPasswordField();
        JTextField c_password2 = new JPasswordField();

        create.addActionListener(new Create());

        login.add(new JLabel("Username:"));
        login.add(c_username);
        login.add(new JLabel(" "));
        login.add(new JLabel("Password:"));
        login.add(c_password);
        login.add(new JLabel("Confirm Password:"));
        login.add(c_password2);
        login.add(new JLabel(" "));
        login.add(create);

        pane.add(login, BorderLayout.CENTER);
        // end login shit

        // TODO: working on getting messages pane to scroll correctly.
        account_window.setPreferredSize(new Dimension(300, 230));


        //Use the content pane's default BorderLayout. No need for
        //setLayout(new BorderLayout());
        //Display the window.
        account_window.pack();
        account_window.setVisible(true);
    }
    //endregion

    //region ////////// Public Methods ///////////////////
    public void connect(int client_id){
        if(client_id != -1) {
            connected = true;
            my_id = client_id;
            System.out.println("Connected! cl_id:" + my_id);
        }
        else
            System.out.println("Connect Error!");
    }

    public int get_my_id(){
        return my_id;
    }

    public void send_message(){
        String to_send = null;

        if(connected) {
            to_send = '\n' + send_pane.getText();
        }
        else{
            to_send = "\nPlease connect and login first.";
        }

        send_pane.setText(""); // Clear my sent message pane.
        messages_pane.setText(messages_pane.getText() + to_send);

        // TODO: also handle socket send to server here.
    }
    //endregion

}
