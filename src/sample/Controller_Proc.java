package sample;

import java.util.concurrent.*;
import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.modbusclient.ModbusClient;

public class Controller_Proc {
    private ModbusClient modbusClient;
    LinkedBlockingQueue<String> ui_cmd_queue = new LinkedBlockingQueue<String>(55);
    private String cmd;
    private Controller controller;
    private ScheduledExecutorService execute;
    ScheduledFuture<?> future_1, future_2, future_3;
    Runnable task = () ->{
        //this.Get_ui_cmd();
        while(true)
        try {
            Thread.sleep( this.controller.getId_timeout_val() );
            System.out.println(this.controller.getId_timeout_val());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    };
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

    public Controller_Proc(Controller controller) {
        this.controller = controller;
        execute = Executors.newScheduledThreadPool(2);
//        future_1 = execute.scheduleAtFixedRate(task_1, 0, 500, TimeUnit.MILLISECONDS);
//        future_2 = execute.scheduleAtFixedRate(task_2, 0, 500, TimeUnit.MILLISECONDS);
//        future_3 = execute.scheduleAtFixedRate(task_3, 0, 500, TimeUnit.MILLISECONDS);
        //task.run();
        Thread thread = new Thread(task);
        thread.start();
        int qqq = 5;
        qqq++;
    }

    public Controller_Proc(LinkedBlockingQueue<String> ui_cmd_queue) {
        this.ui_cmd_queue = ui_cmd_queue;
    }

    /*public void setInterface_cmd_queue(LinkedBlockingQueue<String> ui_cmd_queue) {
        this.ui_cmd_queue = ui_cmd_queue;
    }*/

    private void Get_ui_cmd() {
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

    public void Set_ui_cmd(String cmd) {
        System.out.println("qwertyuiop");
        Boolean[] bbb = new Boolean[6];
        bbb[0] = future_1.isCancelled();
        bbb[1] = future_1.isDone();
        bbb[2] = future_1.cancel(true);
        bbb[3] = future_1.isCancelled();
        bbb[4] = future_1.isDone();
        bbb[5] = future_1.cancel(true);
        System.out.println("qwertyuiop");
/*        try {
        int qwerty = 0;
        qwerty = 55;
            this.ui_cmd_queue.put(cmd);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
    }

    private void Select_ui_func(String cmd) {
        this.cmd = cmd;
        switch (cmd) {
            case "Modbus_connect": Modbus_Client_Connect();
                break;
            case "Read_Input_regs": break;
            case "Write_Holding_regs": break;
            default: break;
        }
    }

    private void Modbus_Client_Connect() {
        //modbusClient.Connect(id_mb_IP_text.getText(),controller.get_Id_mb_port_text_int());
    }
}
