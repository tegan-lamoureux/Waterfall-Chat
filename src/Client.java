import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.io.IOException;

/**
 * Created by tegan on 5/11/2017.
 */

public class Client {
    ////////////////////////////////////////////////////
    // Private Class Variables   ///////////////////////
    ////////////////////////////////////////////////////
    private boolean connected = false;
    private int my_id = 0;

    private boolean message_waiting = false;
    private String message;

    private class HandleInputEnter implements KeyListener {
        @Override
        public void keyPressed(KeyEvent e) {
            if(e.getKeyChar() == '\n'){
                e.consume(); // get rid of my return so it doesn't muck up my send field
                message_waiting = true;
                message = send_pane.getText();
                send_pane.setText("");
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {}
        @Override
        public void keyTyped(KeyEvent e) {}
    }



    ////////////////////////////////////////////////////
    // SWING Window Components   ///////////////////////
    ////////////////////////////////////////////////////
    private JFrame    messages_window = new JFrame("Chat Window"); // Jframe for main window.
    private JTextPane messages_pane   = new JTextPane(); // Main messages pane.
    private JTextPane send_pane       = new JTextPane(); // Text box to send messages.
    private JTextPane user_pane       = new JTextPane(); // User list.
    private JMenuBar  main_menu       = new JMenuBar();  // Main menu bar.



    ////////////////////////////////////////////////////
    // Client Methods   ////////////////////////////////
    ////////////////////////////////////////////////////

    public void windowClosed(WindowEvent e){
        System.exit(0);
    }

    public Client() {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI

        Thread input_from_server_thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean alive = true;

                    Socket socket = new Socket("localhost", 6789);
                    DataOutputStream out_to_server = new DataOutputStream(socket.getOutputStream());
                    BufferedReader in_from_server = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    while (alive) {
                        if(message_waiting){
                            message_waiting = false;
                            out_to_server.writeBytes("__message\n");
                            Thread.sleep(50);
                            out_to_server.writeBytes(message + '\n');
                            message = "";
                            System.out.println("Cleared message.");
                        }

                        if(in_from_server.ready()) {
                            switch (in_from_server.readLine()) {
                                case "__user_list":
                                    String name = "";
                                    user_pane.setText("");

                                    // TODO: add timeout
                                    while (!name.equals("__finished")) {
                                        if (in_from_server.ready()) {
                                            name = in_from_server.readLine();
                                            if(!name.equals("__finished")) {
                                                user_pane.setText(user_pane.getText() + name + '\n');
                                            }
                                        }
                                    }
                                    break;

                                case "__message":
                                    // TODO: add timeout
                                    boolean message_recieved = false;

                                    while (!message_recieved) {
                                        if (in_from_server.ready()) {
                                            messages_pane.setText(messages_pane.getText() + '\n' + in_from_server.readLine());
                                            message_recieved = true;
                                        }
                                        Thread.sleep(50);
                                    }
                                    break;

                                case "__kill":
                                    alive = false;
                                    break;
                            }
                        }

                        Thread.sleep(50);

                        out_to_server.writeBytes("__get_users\n");
                    }
                    socket.close();
                }
                catch (Exception e) {}
            }
        });

        input_from_server_thread.start();

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                create_main_window();
            }
        });
    }

    public Client(int client_id) throws IOException{
        this();
        this.connect(client_id);
    }

    private void add_message(String message){
        messages_pane.setText(messages_pane.getText() + "\n" + message);
    }

    private void create_main_window(){
        // I need a place for login credentials at the top. If valid, logs in, if invalid, shows the new user
        // window signup.

        //Create and set up the window.
        messages_window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //Set up the content pane.
        Container pane = messages_window.getContentPane();

        messages_pane.setPreferredSize(new Dimension(450, 300));
        messages_pane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Messages"),
                        BorderFactory.createEmptyBorder(5,5,5,5)),
                messages_pane.getBorder()));
        messages_pane.setEditable(false);
        if(this.connected == false) {
            messages_pane.setText("Disconnected. Please log in.");
        }
        pane.add(messages_pane, BorderLayout.CENTER);

        // TODO: working on getting messages pane to scroll correctly.
        messages_window.setPreferredSize(new Dimension(450, 300));

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

        JMenu file_menu = new JMenu("File");
        JMenuItem login = new JMenuItem("Login");
        JMenuItem exit = new JMenuItem("Exit");

        file_menu.add(login);
        file_menu.add(exit);

        main_menu.add(file_menu);

        messages_window.setJMenuBar(main_menu);

        //Use the content pane's default BorderLayout. No need for
        //setLayout(new BorderLayout());
        //Display the window.
        messages_window.pack();
        messages_window.setVisible(true);

        // Default the cursor to the send text field.
        send_pane.requestFocusInWindow();
    }

    public void connect(int client_id){
        if(client_id != -1) {
            connected = true;
            my_id = client_id;
            System.out.println("Connected! cl_id:" + my_id);
            this.add_message("Connected! cl_id:" + my_id);
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

}
