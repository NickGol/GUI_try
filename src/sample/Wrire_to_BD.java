package sample;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class Wrire_to_BD implements Observer {
    Queue<Integer> block_queue_bd = new LinkedBlockingQueue<Integer>(500);
    @Override
    public void update(Observable o, Object arg) {
        block_queue_bd.add( (Integer)arg );
        System.out.println(block_queue_bd.size());
    }

    public Write_data_to_monitor()
    {
        while
    }

}
