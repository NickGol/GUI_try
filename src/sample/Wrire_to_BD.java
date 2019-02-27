package sample;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Wrire_to_BD implements Observer {
    Queue<Integer> block_queue_bd = new LinkedBlockingQueue<Integer>(500);
    Runnable task;
    ScheduledExecutorService execute;
    public void Wrire_to_BD()
    {
        task = () ->{
            this.Write_data_to_monitor();
        };
    }
    @Override
    public void update(Observable o, Object arg) {
        block_queue_bd.add( (Integer)arg );
        System.out.println(block_queue_bd.size());
    }
    public void Start_writing_to_monitor()
    {
        execute = Executors.newScheduledThreadPool(2);
        execute.scheduleAtFixedRate(task, 0, 500, TimeUnit.MILLISECONDS);
    }
    public void Stop_writing_to_monitor()
    {
        execute.shutdown();
    }
    public void Write_data_to_monitor()
    {
        int kkk = 0;
        System.out.print(block_queue_bd.size());
        System.out.print("     ");
        while (block_queue_bd.poll() != null)
        {kkk++;}
        System.out.println(kkk);
    }

}
