import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Created by tegan on 5/11/2017.
 */

// TODO: find out why closing one of multiple clients causes the rest to close as well

public class Client {
    private boolean connected = false;
    private int my_id = 0;

    private class HandleInputEnter implements KeyListener {
        @Override
        public void keyPressed(KeyEvent e) {
            if(e.getKeyChar() == '\n'){
                e.consume(); // get rid of my return so it doesn't muck up my send field
                Client.this.send_message();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {}
        @Override
        public void keyTyped(KeyEvent e) {}
    }

    // *** Window stuff.
    private JFrame messages_window; // Jframe for main window.
    private JTextPane messages_pane; // Main messages pane.
    private JTextPane send_pane;     // Text box to send messages.
    private JTextPane user_pane;     // User list.
    private JMenuBar main_menu;     // Main menu bar.
    private JScrollPane messages_scrolling;
    // ** End window stuff.

    public Client(){
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                create_main_window();
            }
        });
    }

    public Client(int client_id){
        this();

        this.connect(client_id);
    }

    private void create_main_window(){
        // I need a place for login credentials at the top. If valid, logs in, if invalid, shows the new user
        // window signup.


        //Create and set up the window.
        messages_window = new JFrame("Chat Window");
        messages_window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Set up the content pane.
        Container pane = messages_window.getContentPane();

        messages_pane = new JTextPane();
        messages_pane.setPreferredSize(new Dimension(450, 300));
        messages_pane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Messages"),
                        BorderFactory.createEmptyBorder(5,5,5,5)),
                messages_pane.getBorder()));
        messages_pane.setEditable(false);
        messages_pane.setText("Disconnected. Please log in.");
        pane.add(messages_pane, BorderLayout.CENTER);

        // TODO: working on getting messages pane to scroll correctly.
        messages_window.setPreferredSize(new Dimension(450, 300));

        user_pane = new JTextPane();
        user_pane.setPreferredSize(new Dimension(150, 300));
        user_pane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Users"),
                        BorderFactory.createEmptyBorder(5,5,5,5)),
                user_pane.getBorder()));
        pane.add(user_pane, BorderLayout.LINE_END);

        send_pane = new JTextPane();
        send_pane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Input"),
                        BorderFactory.createEmptyBorder(1,1,1,1)),
                send_pane.getBorder()));
        send_pane.requestFocusInWindow();

        send_pane.addKeyListener(new HandleInputEnter());
        pane.add(send_pane, BorderLayout.PAGE_END);

        main_menu = new JMenuBar();
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
            this.messages_pane.setText("Connected! cl_id:" + my_id);
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
