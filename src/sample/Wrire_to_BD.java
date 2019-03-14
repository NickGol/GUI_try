package sample;
import java.sql.*;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Wrire_to_BD implements Observer {
/*    @FXML
    private Button but1_id;*/

    Queue<int[]> block_queue_bd = new LinkedBlockingQueue<int[]>(100);
    Runnable task;
    ScheduledExecutorService execute;
    private Controller controller;
    private Connection conn;
    private Statement stmt;

    public Wrire_to_BD(Controller cntrl/*ScheduledExecutorService e1*/)
    {
        this.controller = cntrl;
        Connect_to_DB();
        task = () ->{
            this.Write_data_to_DB();
        };
        Thread thread = new Thread(task);
        thread.start();
    }
    private void Connect_to_DB() {

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/example", "postgres", "admin1");
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("-- Opened database successfully");
    }

    public void Write_data_to_DB()
    {   int qqq123=0;
        try {
        stmt = conn.createStatement();
    } catch (SQLException e) {
        e.printStackTrace();
    }
        while(true) {
           /* try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            int q_size = 0, affected_lines;
            int[] input_regs_arr;
            String sql = "INSERT INTO table_12345 VALUES";
/*            try {
                stmt = conn.createStatement();
            } catch (SQLException e) {
                e.printStackTrace();
            }*/
            q_size = block_queue_bd.size();

            //while (block_queue_bd.poll() != null)
            if (q_size > 0) {
                input_regs_arr = block_queue_bd.poll();
                System.out.println("Queue size before write to DB = " + block_queue_bd.size());
                //for (int i = 0; i < q_size; i++)
                {
                    //input_regs_arr = block_queue_bd.poll();
                    //if (i > 0) sql += ",";
                    for (int cnt = 0; cnt < input_regs_arr.length; cnt += 5) {
                        sql = sql + "(" + input_regs_arr[cnt] + "," + input_regs_arr[cnt + 1] + "," + input_regs_arr[cnt + 2] + "," + input_regs_arr[cnt + 3] + "," + input_regs_arr[cnt + 4] + "),";
                    }
                }
                sql = sql + "(5,5,5,5,5);";
                try {
                    qqq123++;
                    affected_lines = stmt.executeUpdate(sql);
                    affected_lines = stmt.executeUpdate(sql);
                    affected_lines = stmt.executeUpdate(sql);
                    affected_lines = stmt.executeUpdate(sql);
                    qqq123 = qqq123 + 4*affected_lines;
                    //stmt.close();
                    //conn.commit();
                    System.out.println("number of affected lines = " + affected_lines + " Time = " + System.currentTimeMillis()+ " qqq123 = " + qqq123);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        block_queue_bd.add( (int[])arg );
        //System.out.print(block_queue_bd.size());
    }
    public void Start_writing_to_monitor()
    {
        execute = Executors.newScheduledThreadPool(2);
        execute.scheduleAtFixedRate(task, 0, 250, TimeUnit.MILLISECONDS);
    }
    public void Stop_writing_to_monitor()
    {
        execute.shutdown();
    }
    public void Write_data_to_monitor(int[] qqq)
    {
        block_queue_bd.add( qqq );
        /*int kkk = 0, q_size=0;
        q_size = block_queue_bd.size();*/
        //System.out.print(block_queue_bd.size());
        //System.out.print("  qqqqq   ");
        //while (block_queue_bd.poll() != null)
        /*{kkk++;}
        System.out.println(q_size + "   qqqqq   " + kkk);
        kkk=555;*/
    }

}
