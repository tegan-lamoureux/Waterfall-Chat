/**
 * Created by tegan on 5/11/2017.
 */
public class Client {
    private boolean connected = false;
    private int my_id = 0;

    public Client(int client_id){
        this.connect(client_id);
    }

    public void connect(int client_id){
        if(client_id != -1) {
            connected = true;
            my_id = client_id;
            //System.out.println("Connected! cl_id:" + my_id);
        }
        else
            System.out.println("Connect Error!");
    }

    public int get_my_id(){
        return my_id;
    }

}
