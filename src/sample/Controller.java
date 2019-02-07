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
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.modbusclient.ModbusClient;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;

public class Controller {
    private ModbusClient modbusClient;

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
    void initialize() {
        //but1_id.setOnAction();
        boolean success = false;
        modbusClient = new ModbusClient(id_IP.getText(),Integer.parseInt(id_Port.getText()));
        System.out.println(modbusClient.Available(500));
        System.out.println("1234567890");

    }

    @FXML
    void asd_func(MouseEvent event) throws IOException {
        System.out.println("12345");
        /*System.out.println(event.getEventType());
        label1_id.setText(new Date().toString());*/
        modbusClient.Connect(id_IP.getText(),Integer.parseInt(id_Port.getText()));
    }
}