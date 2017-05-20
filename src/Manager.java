import java.util.Scanner;

/**
 * Created by tegan on 5/11/2017.
 */
public class Manager {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        Server test_server = new Server();

        test_server.create_user_account("test_user", "test_password");
        test_server.create_user_account("test_user2", "test_password");
        test_server.create_user_account("test_user3", "test_password");
        test_server.create_user_account("test_user4", "test_password");

        Client test_client = new Client(test_server.login("test_user", "test_password"));
        Client test_client2 = new Client(test_server.login("test_user2", "test_password"));
        Client test_client3 = new Client(test_server.login("test_user3", "test_password"));
        Client test_client4 = new Client(test_server.login("test_user4", "test_password"));
    }
}
