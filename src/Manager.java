import java.util.Scanner;

/**
 * Created by tegan on 5/11/2017.
 */
public class Manager {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        Server test_server = new Server();

        System.out.println("How many clients would you like to spawn?");
        int number_of_clients = input.nextInt();

        Client[] test_clients = new Client[number_of_clients];

        for(int i = 0; i < number_of_clients; ++i)
            test_clients[i] = new Client(test_server.connect());

        for(int i = 0; i < number_of_clients; ++i)
            if(!test_server.disconnect(test_clients[i].get_my_id()))
                System.out.println("Disconnect error.");
    }
}
