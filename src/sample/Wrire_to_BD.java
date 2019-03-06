package sample;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class Wrire_to_BD implements Observer {
/*    @FXML
    private Button but1_id;*/

    Queue<Integer[]> block_queue_bd = new LinkedBlockingQueue<Integer[]>();
    Runnable task;
    ScheduledExecutorService execute;
    private Controller controller;

    public Wrire_to_BD(Controller cntrl/*ScheduledExecutorService e1*/)
    {
        this.controller = cntrl;
        //execute = e1;
        //System.out.println(controller.but1_id.getText());
        //controller.but1_id.setText("12345");
        task = () ->{
            this.Write_data_to_monitor();
        };
    }
    @Override
    public void update(Observable o, Object arg) {
        block_queue_bd.add( (Integer[])arg );
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
    public void Write_data_to_monitor()
    {
        int kkk = 0, q_size=0;
        q_size = block_queue_bd.size();
        //System.out.print(block_queue_bd.size());
        //System.out.print("  qqqqq   ");
        //while (block_queue_bd.poll() != null)
        {kkk++;}
        System.out.println(q_size + "   qqqqq   " + kkk);
        kkk=555;
    }

}
