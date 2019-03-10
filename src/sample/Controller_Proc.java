package sample;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.*;
import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.modbusclient.ModbusClient;
import javafx.application.Platform;
import javafx.scene.chart.XYChart;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;

public class Controller_Proc implements Observer {
    private ModbusClient modbusClient = new ModbusClient();
    LinkedBlockingQueue<String> ui_cmd_queue = new LinkedBlockingQueue<String>(55);
    private Controller controller;
    XYChart.Series series/* = new XYChart.Series()*/;

    Runnable task = () ->{
        this.Get_ui_cmd();
    };

    public Controller_Proc(Controller controller) {
        this.controller = controller;
        series = new XYChart.Series();
        Thread thread = new Thread(task);
        thread.start();
    }

    private void Get_ui_cmd() { // Create independent Thread
        String str;
        while (true) {
            try {
                str = "12345";
                str = ui_cmd_queue.take();
                Select_ui_func(str);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void Set_ui_cmd(String cmd) { // Work from Controller Thread
        System.out.println(Thread.currentThread().getName() + " set");
       try {
            this.ui_cmd_queue.put(cmd);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void Select_ui_func(String cmd) {
        switch (cmd) {
            case "Modbus_connect": Modbus_Client_Connect();
                break;
            case "Read_Input_regs": Modbus_Read_Input_Regs();
                break;
            case "Write_Holding_regs": Modbus_Write_Holding_Regs();
                break;
            default: break;
        }
    }

    private void Modbus_Client_Connect() /*throws IOException*/ {
        try {
            modbusClient.Connect(controller.get_id_mb_IP_text_str(),controller.get_Id_mb_port_text_int());
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("Failed attempt to connect to Modbus server");
        }
    }

    private  void Modbus_Read_Input_Regs() {
        int[] Input_regs = new int[0];
            try {
                Input_regs = modbusClient.ReadInputRegisters(0,4000);
            } catch (ModbusException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String str = Input_regs[0]+" "+Input_regs[1]+" "+Input_regs[2]+" "+Input_regs[3];
            System.out.println(Thread.currentThread().getName());
            controller.set_id_Input_str(str);
            Send_data_to_gaph(Input_regs);
    }

    private  void Modbus_Write_Holding_Regs() {
        try {
            modbusClient.WriteMultipleRegisters(0,controller.get_id_Holding_str_int16_arr());
        } catch (ModbusException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void Send_data_to_gaph(int[] data_arr){
        series = new XYChart.Series();
        //if(series.getData().size()>0)
          //  series.getData().remove(0, 4000);
        for(Integer i=0; i<data_arr.length; i++)
        {
            series.getData().add(  new XYChart.Data( i.toString(), data_arr[i] )  );
        }
        Platform.runLater(()-> {controller.id_chart.getData().setAll(series);});
    }
    @Override
    public void update(Observable o, Object arg) {
        try {
            ui_cmd_queue.put( (String) arg );
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}







/*        while(true)
        try {
            Thread.sleep( this.controller.getId_timeout_val() );
            System.out.println(this.controller.getId_timeout_val());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

//        execute = Executors.newScheduledThreadPool(2);
//        future_1 = execute.scheduleAtFixedRate(task_1, 0, 500, TimeUnit.MILLISECONDS);
//        future_2 = execute.scheduleAtFixedRate(task_2, 0, 500, TimeUnit.MILLISECONDS);
//        future_3 = execute.scheduleAtFixedRate(task_3, 0, 500, TimeUnit.MILLISECONDS);
//task.run();

/*
    Runnable task_1 = () ->{
        System.out.println("1");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    };
    Runnable task_2 = () ->{
        System.out.println("2");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    };
    Runnable task_3 = () ->{
        System.out.println("3");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    };
*/

        /*System.out.println("qwertyuiop");
        Boolean[] bbb = new Boolean[6];
        bbb[0] = future_1.isCancelled();
        bbb[1] = future_1.isDone();
        bbb[2] = future_1.cancel(true);
        bbb[3] = future_1.isCancelled();
        bbb[4] = future_1.isDone();
        bbb[5] = future_1.cancel(true);
        System.out.println("qwertyuiop");*/

           /*public Controller_Proc(LinkedBlockingQueue<String> ui_cmd_queue) {
        this.ui_cmd_queue = ui_cmd_queue;
    }*/

    /*public void setInterface_cmd_queue(LinkedBlockingQueue<String> ui_cmd_queue) {
        this.ui_cmd_queue = ui_cmd_queue;
    }*/

/*
    private ScheduledExecutorService execute;
    ScheduledFuture<?> future_1, future_2, future_3;*/
