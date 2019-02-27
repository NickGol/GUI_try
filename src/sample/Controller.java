package sample;

import java.net.URL;
import java.util.*;

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

import java.io.IOException;
import java.util.concurrent.*;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.modbusclient.ModbusClient;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;

public class Controller implements Observer {
    private ModbusClient modbusClient;
    ScheduledExecutorService execute;
    Runnable task;
    XYChart.Series series;
    Double x_val = 6.0, y_val = 35.0;
    Queue<Integer> block_queue_plot = new LinkedBlockingQueue<Integer>(500);

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button but1_id;
    @FXML
    private Label label1_id;
    @FXML
    private TextField id_IP;
    @FXML
    private TextField id_Port;
    @FXML
    private Button ReadInp_but_id;

    @FXML
    private TextField id_Input_str;

    @FXML
    private Button WriteHold_but_id;

    @FXML
    private TextField id_Holding_str;
    @FXML
    private ToggleButton id_Draw_but;
    @FXML
    private LineChart<Integer, Integer> id_chart;
    @FXML
    private CategoryAxis id_X;

    @FXML
    private NumberAxis id_Y;

    @FXML
    void initialize() throws InterruptedException {
        //but1_id.setOnAction();
        boolean success = false;
        modbusClient = new ModbusClient(id_IP.getText(),Integer.parseInt(id_Port.getText()));
        System.out.println(modbusClient.Available(500));
        System.out.println("1234567890");
        Thread t = Thread.currentThread(); // получаем главный поток
        System.out.println(t.getName()); // main
        label1_id.setText(t.getName());
        task = () ->{
            fffff();
        };
        series = new XYChart.Series();

        for(int i=0; i< 500; i++) {
            series.getData().add(new XYChart.Data( String.valueOf(i), 0));
            x_val = Double.valueOf(i);
        }
        id_chart.getData().addAll(series);
        //id_chart.getData().
    }

    void fffff()
    {
        //Platform.runLater(()-> {Controller.this.label1_id.setText(String.valueOf(System.nanoTime()));});
        Platform.runLater(()-> {
            for(int i=0; i<50; i++) {
                series.getData().remove(0);
                //series.getData().add(new XYChart.Data("11", 105));});
                series.getData().add(new XYChart.Data(x_val.toString(), y_val));
                //x_val++; y_val++;
                x_val++;
                y_val = Math.sin(6.28 * x_val / 360);
            }
        });

    }
    @FXML
    void asd_func(MouseEvent event) throws IOException {
        System.out.println("12345");
        /*System.out.println(event.getEventType());
        label1_id.setText(new Date().toString());*/
        modbusClient.Connect(id_IP.getText(),Integer.parseInt(id_Port.getText()));
    }

    @FXML
    void Read_Input_regs(MouseEvent event) throws IOException, SerialPortTimeoutException, SerialPortException, ModbusException {
        int[] Input_regs = modbusClient.ReadInputRegisters(0,4);
        String str = Input_regs[0]+" "+Input_regs[1]+" "+Input_regs[2]+" "+Input_regs[3];
        id_Input_str.setText(str);
    }

    @FXML
    void Write_Holding_regs(MouseEvent event) throws IOException, SerialPortTimeoutException, SerialPortException, ModbusException {
        int[] Holding_regs;// = Integer.parseInt(id_Holding_str.getText().split(" "));
        int i=0;
        String[] s = id_Holding_str.getText().trim().split(" ");
        Holding_regs = new int[s.length];
        for (String str :id_Holding_str.getText().split(" "))
        {
            Holding_regs[i] = Integer.parseInt(str);
            i++;
        }
        modbusClient.WriteMultipleRegisters(0,Holding_regs);
    }

    @FXML
    void Toggle_click(MouseEvent event) {
        //fffff();
        //execute.scheduleAtFixedRate(task,0, 100, TimeUnit.MILLISECONDS);
    }

    @FXML
    void Draw_chart(MouseEvent event) {
        if(id_Draw_but.getText().equals("Draw_chart_play")) {
            id_Draw_but.setText("Draw_chart_stop");
            execute = Executors.newScheduledThreadPool(1);
            execute.scheduleAtFixedRate(task, 0, 50, TimeUnit.MILLISECONDS);
            //execute.scheduleWithFixedDelay(task, 0, 200, TimeUnit.MILLISECONDS);
        }
        else {
            id_Draw_but.setText("Draw_chart_play");
            execute.shutdown();
        }
    }

    @Override
    public void update(Observable source, Object arg) {
        /*if(source instanceof Publisher) {

            System.out.println((String) newsItem);

        }*/
    }
}