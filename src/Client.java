import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.io.IOException;
import java.util.Vector;

/**
 * Created by Tegan on 5/11/2017.
 *
 * This is my client class, which handles all of the functionality of a client instantiation,
 * including window creation, user input and output, and chat functionality. All of the UI
 * for my chat is here.
 */

//TODO 6. get login/new user window up and running
//TODO 2. fix double send issue
//TODO 3. get PM working
//TODO 4. get user history working
//TODO 5. get username display in chat working
//TODO 7: get user to logout at window close

public class Client {

    //region ////////// Private Class Variables //////////
    //////////////////////////////////////////////////////
    // Identity Variables
    private boolean connected = false;
    private boolean logged_in = false;

    // Action Flags
    private boolean action_login_ready     = false;
    private boolean action_message_waiting = false;
    private boolean action_create_account  = false;

    // Misc. Variables
    private int my_id = 0;
    private String my_username = null;
    private String message;
    //endregion

    //region ////////// SWING Window Components //////////
    //////////////////////////////////////////////////////
    private JFrame         messages_window = new JFrame("Chat Window"); // Jframe for main window.
    private JFrame         account_window  = new JFrame("Create New Account");
    private JTextArea      messages_pane   = new JTextArea(); // Main messages pane.
    private JScrollPane    messages_scroll = null;            // set up during window creation
    private JTextPane      send_pane       = new JTextPane(); // Text box to send messages.
    private JMenuBar       main_menu       = new JMenuBar();  // Main menu bar.
    private JButton        login_button = null;
    private JButton        account_button = null;
    private JTextField     username = null;
    private JPasswordField password = null;
    JTextField c_username  = null;
    JTextField c_password  = null;
    JTextField c_password2 = null;

    DefaultListModel<String> users = new DefaultListModel<>();
    JList<String> user_list = null;
    JScrollPane scroll_users_list = null;
    //endregion

    //region ////////// Event Handler Classes ////////////
    //////////////////////////////////////////////////////
    private class HandleInputEnter implements KeyListener {
        @Override
        public void keyPressed(KeyEvent e) {
            if(e.getKeyChar() == '\n'){
                e.consume(); // get rid of my return so it doesn't muck up my send field

                messages_pane.setRows(1 + messages_pane.getRows());
                action_message_waiting = true;
                message = send_pane.getText();
                send_pane.setText("");

                messages_pane.append('\n' + my_username + ": " + message);
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
            action_login_ready = true;
        }
    }

    private class Show_Create_Account implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            javax.swing.SwingUtilities.invokeLater(Client.this::create_account_window);
        }
    }

    private class Create_Account implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(!c_password.getText().equals("") && c_password.getText().equals(c_password2.getText())) {
                action_create_account = true;
            }
            else
                JOptionPane.showMessageDialog(null, "Passwords must match and be not empty.", "Error", JOptionPane.INFORMATION_MESSAGE);
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
                int user_timer = 0;

                String command = null;

                Socket socket = new Socket("localhost", 6789);
                DataOutputStream out_to_server = new DataOutputStream(socket.getOutputStream());
                BufferedReader in_from_server = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                connected = true;

                // Main client loop.
                while (alive) {
                    if(in_from_server.ready())
                        command = in_from_server.readLine();

                    if(command != null && command.equals("__message")) {
                        while (!in_from_server.ready())
                            Thread.sleep(1);

                        String mess = in_from_server.readLine();

                        if(!mess.equals("__message")) {
                            messages_pane.append('\n' + mess);
                            messages_pane.setRows(1 + messages_pane.getRows());
                        }
                    }

                    if(command != null && command.equals("__message_private")) {
                        while (!in_from_server.ready())
                            Thread.sleep(1);

                        String user = in_from_server.readLine();

                        while (!in_from_server.ready())
                            Thread.sleep(1);

                        String mess = in_from_server.readLine();

                        if(this.my_username.equals(user)) {
                            messages_pane.append('\n' + "PRIVATE MESSAGE: " + mess);
                            messages_pane.setRows(1 + messages_pane.getRows());
                        }
                    }

                    if(command != null && command.equals("__kill")){
                        alive = false;
                    }

                    // if we hit out update user timer..
                    if(user_timer > 20) {
                        out_to_server.writeBytes("__get_users\n");

                        while (!in_from_server.ready())
                            Thread.sleep(1);

                        String temp = "";
                        Vector<String> names = new Vector<>();

                        // TODO: add timeout
                        while (!temp.equals("__finished")) {
                            if (in_from_server.ready()) {
                                temp = in_from_server.readLine();
                                if (!temp.equals("__finished") && !temp.equals("__user_list")) {
                                    names.add(temp);
                                }
                            } else
                                Thread.sleep(10);
                        }

                        for (String name : names) {
                            if (!users.contains(name))
                                users.addElement(name);
                        }

                        for(int i = 0; i < users.size(); ++i)
                            if(!names.contains(users.get(i)))
                                users.remove(i);

                        user_timer = 0;
                    }
                    else
                        ++ user_timer;

                    if (action_message_waiting) {
                        action_message_waiting = false;

                        if(user_list.getMinSelectionIndex() == -1) {
                            out_to_server.writeBytes("__message\n");
                            out_to_server.flush();
                            Thread.sleep(100);
                            out_to_server.writeBytes(my_username + ": " + message + '\n');
                            out_to_server.flush();
                            message = "";
                            System.out.println("Cleared message.");
                        }
                        else {
                            out_to_server.writeBytes("__message_private\n");
                            out_to_server.flush();
                            Thread.sleep(100);
                            out_to_server.writeBytes(my_username + ": " + message + '\n');
                            out_to_server.flush();
                            Thread.sleep(100);
                            out_to_server.writeBytes(users.get(user_list.getMinSelectionIndex()) + '\n');
                            out_to_server.flush();
                            message = "";
                            System.out.println("Cleared message.");
                        }
                    }

                    if (action_create_account) {
                        action_create_account = false;
                        out_to_server.writeBytes("__new_account\n");
                        out_to_server.flush();
                        Thread.sleep(100);
                        out_to_server.writeBytes(c_username.getText() + '\n');
                        out_to_server.flush();
                        Thread.sleep(100);
                        out_to_server.writeBytes(c_password.getText() + '\n');
                        out_to_server.flush();

                        JOptionPane.showMessageDialog(null, "New account created for " + c_username.getText() + "!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    }

                    if (action_login_ready) {
                        action_login_ready = false;

                        if(!logged_in) {
                            out_to_server.writeBytes("__login\n");
                            out_to_server.flush();
                            Thread.sleep(100);
                            out_to_server.writeBytes(username.getText() + '\n');
                            out_to_server.flush();
                            Thread.sleep(100);
                            out_to_server.writeBytes(password.getText() + '\n');
                            out_to_server.flush();

                            while (!in_from_server.ready())
                                Thread.sleep(10);

                            String valid = in_from_server.readLine();
                            System.out.println(valid);
                            if (valid.equals("__valid_credentials")) {
                                username.setEnabled(false);
                                password.setEnabled(false);
                                this.my_username = username.getText();
                                this.logged_in = true;
                            } else {
                                JOptionPane.showMessageDialog(null, "Login credentials are not valid.", "Warning", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                        else{
                            out_to_server.writeBytes("__logout\n");
                            out_to_server.flush();
                            Thread.sleep(100);
                            out_to_server.writeBytes(username.getText() + '\n');
                            out_to_server.flush();
                            Thread.sleep(100);

                            while (!in_from_server.ready())
                                Thread.sleep(10);

                            String valid = in_from_server.readLine();
                            System.out.println(valid);
                            if (valid.equals("__valid_credentials")) {
                                username.setEnabled(true);
                                password.setEnabled(true);
                                this.my_username = "";
                                this.logged_in = false;
                            }
                        }
                    }

                    Thread.sleep(100);
                }
                socket.close();
            }
            catch (Exception e) {}
        });

        javax.swing.SwingUtilities.invokeLater(this::create_main_window);

        input_from_server_thread.start();
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
        account_button.addActionListener(new Show_Create_Account());

        login.add(new JLabel("Username:"));
        login.add(username);
        login.add(new JLabel("Password:"));
        login.add(password);
        login.add(login_button);
        login.add(account_button);

        pane.add(login, BorderLayout.NORTH);
        // end login shit



        messages_pane.setPreferredSize(new Dimension(200, 300));

        messages_pane.setEditable(false);
        if(!this.connected) {
            messages_pane.setText("Disconnected. Please log in.");
        }

        DefaultCaret caret = (DefaultCaret)messages_pane.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        messages_scroll = new JScrollPane(messages_pane);
        messages_scroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Messages"),
                        BorderFactory.createEmptyBorder(5,5,5,5)),
                messages_pane.getBorder()));

        pane.add(messages_scroll, BorderLayout.CENTER);

        // TODO: working on getting messages pane to scroll correctly.
        messages_window.setPreferredSize(new Dimension(600, 375));

        // setup user list
        user_list = new JList<>(users);
        user_list.setSelectionModel(new DefaultListSelectionModel() {
            private static final long serialVersionUID = 1L;

            boolean gestureStarted = false;

            @Override
            public void setSelectionInterval(int index0, int index1) {
                if(!gestureStarted){
                    if (isSelectedIndex(index0)) {
                        super.removeSelectionInterval(index0, index1);
                    } else {
                        super.addSelectionInterval(index0, index1);
                    }
                }
                gestureStarted = true;
            }

            @Override
            public void setValueIsAdjusting(boolean isAdjusting) {
                if (isAdjusting == false) {
                    gestureStarted = false;
                }
            }
        });

        user_list.setLayoutOrientation(JList.VERTICAL);
        user_list.setVisibleRowCount(-1);

        scroll_users_list = new JScrollPane(user_list);
        scroll_users_list.setPreferredSize(new Dimension(150, 300));
        scroll_users_list.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Users"),
                        BorderFactory.createEmptyBorder(5,5,5,5)),
                scroll_users_list.getBorder()));

        pane.add(scroll_users_list, BorderLayout.LINE_END);

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
        c_username  = new JTextField();
        c_password  = new JPasswordField();
        c_password2 = new JPasswordField();

        create.addActionListener(new Create_Account());

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
