import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;

/**
 * Created by tegan on 5/11/2017.
 */
public class Manager {
    public static void main(String[] args) throws IOException {
        Scanner input = new Scanner(System.in);
        Server test_server = new Server();

        System.out.print("How many clients would you like to spawn? : ");

        int number_clients = input.nextInt();

        for(int i = 0; i < number_clients; ++i){
            test_server.create_user_account("test_user" + i, "test_password");
        }

        Vector<Client> clients = new Vector<>();

        for(int i = 0; i < number_clients; ++i){
            clients.add(new Client(test_server.login("test_user" + i, "test_password")));
        }
    }
}
