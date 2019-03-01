package sample;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class Controller_Proc {
    Queue<String> interface_cmd_queue = new LinkedBlockingQueue<String>(55);

    public void setInterface_cmd_queue(Queue<String> interface_cmd_queue) {
        this.interface_cmd_queue = interface_cmd_queue;
    }
}
