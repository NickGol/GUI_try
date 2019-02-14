package sample;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.modbusclient.ModbusClient;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;

public class Controller {
    private ModbusClient modbusClient;
    ScheduledExecutorService execute;
    Runnable task;

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
    void initialize() {
        //but1_id.setOnAction();
        boolean success = false;
        modbusClient = new ModbusClient(id_IP.getText(),Integer.parseInt(id_Port.getText()));
        System.out.println(modbusClient.Available(500));
        System.out.println("1234567890");
        Thread t = Thread.currentThread(); // получаем главный поток
        System.out.println(t.getName()); // main
        label1_id.setText(t.getName());
        execute = Executors.newScheduledThreadPool(1);
        task = () ->{
            //label1_id.setText(String.valueOf(System.nanoTime()));
            System.out.println(System.nanoTime());
        };
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
        execute.scheduleAtFixedRate(task,0, 100, TimeUnit.MILLISECONDS);
    }
}