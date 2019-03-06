package sample;

import java.net.URL;
import java.util.*;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
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

public class Controller extends Observable implements Observer {
    private ModbusClient modbusClient;
    ScheduledExecutorService execute;
    Runnable task;
    XYChart.Series series;
    Double x_val = 6.0, y_val = 35.0;
    Queue<Integer> block_queue_plot = new LinkedBlockingQueue<Integer>(500);
    Wrire_to_BD Wr_bd;// = new Wrire_to_BD();
    Controller_Proc controller_proc;// = new Controller_Proc(this);

// Controls
    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private Button id_mb_connect_btn;
    @FXML
    private Label label1_id;
    @FXML
    private TextField id_mb_IP_text;
    private String id_mb_IP_text_str_val;
    public String get_id_mb_IP_text_str() {
        return id_mb_IP_text_str_val;
    }

    @FXML
    private TextField id_mb_port_text;
    private Integer id_mb_port_text_int_val;
    public Integer get_Id_mb_port_text_int() {
        return id_mb_port_text_int_val;
    }
    ChangeListener<Boolean> Check_if_focused = (obs, oldVal, newVal) ->{
        if(newVal){
            id_mb_port_text_int_val = Integer.parseInt(id_mb_port_text.getText());
        }
    };

    @FXML
    private ToggleButton id_Draw_but;
    @FXML
    private Button id_mb_read_input_btn;
    @FXML
    private TextField id_Input_str;
    @FXML
    private Button id_mb_write_hold_btn;
    @FXML
    private TextField id_Holding_str;
    @FXML
    private Button WriteHold_but_id1;
    @FXML
    private LineChart<?, ?> id_chart;
    @FXML
    private CategoryAxis id_X;
    @FXML
    private NumberAxis id_Y;
    @FXML
    private TextField id_mb_timeout_text;

    public TextField getId_timeout() {
        //if(id_timeout.focusedProperty())
        return id_mb_timeout_text;
    }
    private static Integer static_integer = 100;
    public Integer getId_timeout_val() {
        //Integer static_integer = 100;
        //id_mb_timeout_text.focusedProperty().addListener();
        id_mb_port_text.focusedProperty().addListener((obs, oldVal, newVal) ->
                System.out.println(newVal ? "Focused" : "Unfocused"));
        if(id_mb_timeout_text.focusedProperty().getValue())
            return static_integer;
        static_integer = Integer.parseInt(id_mb_timeout_text.getText());
        return static_integer;
    }

// Interrupts

    @FXML
    void initialize() throws InterruptedException {
        modbusClient = new ModbusClient(id_mb_IP_text.getText(),Integer.parseInt(id_mb_port_text.getText()));
        id_mb_port_text.focusedProperty().addListener(Check_if_focused);
        //System.out.println(modbusClient.Available(500));
        //System.out.println("1234567890");
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
        controller_proc = new Controller_Proc(this);

    }

    void fffff()
    {
        //Platform.runLater(()-> {Controller.this.label1_id.setText(String.valueOf(System.nanoTime()));});
        Platform.runLater(()-> {
            for(int i=0; i<10; i++) {
                series.getData().remove(0);
                //series.getData().add(new XYChart.Data("11", 105));});
                series.getData().add(new XYChart.Data(x_val.toString(), y_val));
                //x_val++; y_val++;
                x_val++;
                y_val = Math.sin(6.28 * x_val / 360);
                addNews(y_val.intValue());
            }
        });

    }
    @FXML
    void Connect_but(MouseEvent event) throws IOException {
        //controller_proc.Set_ui_cmd("Modbus_connect");
        modbusClient.Connect(id_mb_IP_text.getText(),Integer.parseInt(id_mb_port_text.getText()));
    }

    @FXML
    void Read_Input_but(MouseEvent event) throws IOException, SerialPortTimeoutException, SerialPortException, ModbusException {
        //controller_proc.Set_ui_cmd("Read_Input_regs");
        int[] Input_regs = modbusClient.ReadInputRegisters(0,4);
        String str = Input_regs[0]+" "+Input_regs[1]+" "+Input_regs[2]+" "+Input_regs[3];
        id_Input_str.setText(str);
    }

    @FXML
    void Write_Holding_but(MouseEvent event) throws IOException, SerialPortTimeoutException, SerialPortException, ModbusException {
        //controller_proc.Set_ui_cmd("Write_Holding_regs");
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
            execute = Executors.newScheduledThreadPool(2);
            execute.scheduleWithFixedDelay(task, 0, 50, TimeUnit.MILLISECONDS);
            //execute.scheduleWithFixedDelay(task, 0, 200, TimeUnit.MILLISECONDS);
            Wr_bd = new Wrire_to_BD(this/*execute*/);
            this.register(Wr_bd);
            Wr_bd.Start_writing_to_monitor();
        }
        else {
            id_Draw_but.setText("Draw_chart_play");
            execute.shutdown();
            Wr_bd.Stop_writing_to_monitor();
        }
    }

    @FXML
    void Draw_chart_12345(MouseEvent event) {
        controller_proc.Set_ui_cmd("cmd1");
    }

    @Override
    public void update(Observable source, Object arg) {
        /*if(source instanceof Publisher) {

            System.out.println((String) newsItem);

        }*/
    }


    private List<Observer> channels = new ArrayList<>();

    public void addNews(Integer newItem) {
        Integer[] i_arr1 = {1, 2, 3, 4, 5};
        Integer[] i_arr2 = {1, 2, 3, 4, 5, 6, 7};
        /*
        for(Observer observ: this.channels) {
            observ.update(this, newItem);
        }*/
        for (Observer observ : this.channels) {
            observ.update(this, i_arr1);
        }
        for (Observer observ : this.channels) {
            observ.update(this, i_arr2);
        }
    }

    public void register(Observer observ)
    {
        channels.add(observ);
    }
}