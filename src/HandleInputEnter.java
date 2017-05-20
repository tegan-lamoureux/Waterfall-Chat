import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Created by tegan on 5/4/2017.
 */
public class HandleInputEnter implements KeyListener{
    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyChar() == '\n'){
            e.consume(); // get rid of my return so it doesn't muck up my send field
            // Client.add_message(); add a message here
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}
}
