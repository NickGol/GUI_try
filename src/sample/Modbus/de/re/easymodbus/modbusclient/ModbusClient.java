/*
 * (c) Stefan Ro?mann
 *	This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package sample.Modbus.de.re.easymodbus.modbusclient;
//package sample;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.io.*;
import java.util.*;

import java.io.InputStream;
import sample.Modbus.de.re.easymodbus.datatypes.RegisterOrder;
import sample.Modbus.de.re.easymodbus.exceptions.*;
import sample.Modbus.de.re.easymodbus.modbusclient.*;

public class ModbusClient 
{
	private Socket tcpClientSocket = new Socket();
	protected String ipAddress = "190.201.100.100";
	protected int port = 502;
	private byte [] transactionIdentifier = new byte[2];
	private byte [] protocolIdentifier = new byte[2];
	private byte [] length = new byte[2];
	private byte[] crc = new byte[2];
	private byte unitIdentifier = 1;
	private byte functionCode;
	private byte [] startingAddress = new byte[2];
	private byte [] quantity = new byte[2];
	private boolean udpFlag = false;
    private boolean serialflag = false;
	private int connectTimeout = 1000;
	private InputStream inStream;
	private DataOutputStream outStream;
    public byte[] receiveData;
    public byte[] sendData;  
	private List<ReceiveDataChangedListener> receiveDataChangedListener = new ArrayList<ReceiveDataChangedListener>();
	private List<SendDataChangedListener> sendDataChangedListener = new ArrayList<SendDataChangedListener>();
    private boolean debug=false;

	public ModbusClient(String ipAddress, int port)
	{
		System.out.println("EasyModbus Client Library");
		System.out.println("Copyright (c) Stefan Rossmann Engineering Solutions");
		System.out.println("www.rossmann-engineering.de");
		System.out.println("");
		System.out.println("Creative commons license");
		System.out.println("Attribution-NonCommercial-NoDerivatives 4.0 International (CC BY-NC-ND 4.0)");
		if (debug) StoreLogData.getInstance().Store("EasyModbus library initialized for Modbus-TCP, IPAddress: " + ipAddress + ", Port: "+port);
		this.ipAddress = ipAddress;
		this.port = port;
	}
	
	public ModbusClient()
	{
		System.out.println("EasyModbus Client Library");
		System.out.println("Copyright (c) Stefan Rossmann Engineering Solutions");
		System.out.println("www.rossmann-engineering.de");
		System.out.println("");
		System.out.println("Creative commons license");
		System.out.println("Attribution-NonCommercial-NoDerivatives 4.0 International (CC BY-NC-ND 4.0)");
		if (debug) StoreLogData.getInstance().Store("EasyModbus library initialized for Modbus-TCP");
	}
	    /**
        * Connects to ModbusServer
        * @throws UnknownHostException
        * @throws IOException
        */        
	public void Connect() throws UnknownHostException, IOException
	{
		if (!udpFlag && !this.serialflag)
		{
			
			tcpClientSocket = new Socket(ipAddress, port);
			tcpClientSocket.setSoTimeout(connectTimeout);
			outStream = new DataOutputStream(tcpClientSocket.getOutputStream());
			inStream = tcpClientSocket.getInputStream();
			if (debug) StoreLogData.getInstance().Store("Open TCP-Socket, IP-Address: " + ipAddress + ", Port: " + port);
		}
	}
	
        /**
        * Connects to ModbusServer
        * @param ipAddress  IP Address of Modbus Server to connect to
        * @param port   Port Modbus Server listenning (standard 502)
        * @throws UnknownHostException
        * @throws IOException
        */   
	public void Connect(String ipAddress, int port) throws UnknownHostException, IOException
	{
		this.ipAddress = ipAddress;
		this.port = port;
		
		tcpClientSocket = new Socket(ipAddress, port);
		tcpClientSocket.setSoTimeout(connectTimeout);
		outStream = new DataOutputStream(tcpClientSocket.getOutputStream());
		inStream = tcpClientSocket.getInputStream();
		if (debug) StoreLogData.getInstance().Store("Open TCP-Socket, IP-Address: " + ipAddress + ", Port: " + port);
	}

        /**
        * Convert two 16 Bit Registers to 32 Bit real value
        * @param        registers   16 Bit Registers
        * @return       32 bit real value
        */
    public static float ConvertRegistersToFloat(int[] registers) throws IllegalArgumentException
    {
        if (registers.length != 2)
            throw new IllegalArgumentException("Input Array length invalid");
        int highRegister = registers[1];
        int lowRegister = registers[0];
        byte[] highRegisterBytes = toByteArray(highRegister);
        byte[] lowRegisterBytes = toByteArray(lowRegister);
        byte[] floatBytes = {
                                highRegisterBytes[1],
                                highRegisterBytes[0],
                                lowRegisterBytes[1],
                                lowRegisterBytes[0]
                            };
        return ByteBuffer.wrap(floatBytes).getFloat();
    }  
    
    /**
    * Convert two 16 Bit Registers to 64 Bit double value  Reg0: Low Word.....Reg3: High Word
    * @param        registers  16 Bit Registers
    * @return       64 bit double value
    */
    public static double ConvertRegistersToDouble(int[] registers) throws IllegalArgumentException
    {
    	if (registers.length != 4)
    		throw new IllegalArgumentException("Input Array length invalid");
    	byte[] highRegisterBytes = toByteArray(registers[3]);
    	byte[] highLowRegisterBytes = toByteArray(registers[2]); 
    	byte[] lowHighRegisterBytes = toByteArray(registers[1]);
    	byte[] lowRegisterBytes = toByteArray(registers[0]);
    	byte[] doubleBytes = {
                            highRegisterBytes[1],
                            highRegisterBytes[0],
                            highLowRegisterBytes[1],
                            highLowRegisterBytes[0],
                            lowHighRegisterBytes[1],
                            lowHighRegisterBytes[0],
                            lowRegisterBytes[1],
                            lowRegisterBytes[0]
                        };
    	return ByteBuffer.wrap(doubleBytes).getDouble();
    }  
    
    /**
    * Convert two 16 Bit Registers to 64 Bit double value  Order "LowHigh": Reg0: Low Word.....Reg3: High Word, "HighLow": Reg0: High Word.....Reg3: Low Word
    * @param        registers   16 Bit Registers
    * @param        registerOrder High Register first or low Register first 
    * @return       64 bit double value
    */
    public static double ConvertRegistersToDouble(int[] registers, RegisterOrder registerOrder) throws IllegalArgumentException
    {
    	if (registers.length != 4)
    		throw new IllegalArgumentException("Input Array length invalid");
    	int[] swappedRegisters = { registers[0], registers[1], registers[2], registers[3] };
    	if (registerOrder == RegisterOrder.HighLow)
    		swappedRegisters = new int[] { registers[3], registers[2], registers[1], registers[0] };
    	return ConvertRegistersToDouble(swappedRegisters);
    }
   
        /**
        * Convert two 16 Bit Registers to 32 Bit real value 
        * @param        registers   16 Bit Registers
        * @param        registerOrder    High Register first or low Register first 
        * @return       32 bit real value
        */
    public static float ConvertRegistersToFloat(int[] registers, RegisterOrder registerOrder) throws IllegalArgumentException
    {
        int [] swappedRegisters = {registers[0],registers[1]};
        if (registerOrder == RegisterOrder.HighLow) 
            swappedRegisters = new int[] {registers[1],registers[0]};
        return ConvertRegistersToFloat(swappedRegisters);
    }
   
    
    /**
    * Convert four 16 Bit Registers to 64 Bit long value Reg0: Low Word.....Reg3: High Word
    * @param        registers   16 Bit Registers
    * @return       64 bit value
    */
    public static long ConvertRegistersToLong(int[] registers) throws IllegalArgumentException
    {
    	if (registers.length != 4)
    		throw new IllegalArgumentException("Input Array length invalid");
    	byte[] highRegisterBytes = toByteArray(registers[3]);
    	byte[] highLowRegisterBytes = toByteArray(registers[2]); 
    	byte[] lowHighRegisterBytes = toByteArray(registers[1]);
    	byte[] lowRegisterBytes = toByteArray(registers[0]);
    	byte[] longBytes = {
                            highRegisterBytes[1],
                            highRegisterBytes[0],
                            highLowRegisterBytes[1],
                            highLowRegisterBytes[0],
                            lowHighRegisterBytes[1],
                            lowHighRegisterBytes[0],
                            lowRegisterBytes[1],
                            lowRegisterBytes[0]
                        };
    	return ByteBuffer.wrap(longBytes).getLong();
}  	

    /**
     * Convert four 16 Bit Registers to 64 Bit long value Register Order "LowHigh": Reg0: Low Word.....Reg3: High Word, "HighLow": Reg0: High Word.....Reg3: Low Word
     * @param        registers   16 Bit Registers
     * @return       64 bit value
     */
    public static long ConvertRegistersToLong(int[] registers, RegisterOrder registerOrder) throws IllegalArgumentException
    {
    	if (registers.length != 4)
    		throw new IllegalArgumentException("Input Array length invalid");
    	int[] swappedRegisters = { registers[0], registers[1], registers[2], registers[3] };
    	if (registerOrder == RegisterOrder.HighLow)
    		swappedRegisters = new int[] { registers[3], registers[2], registers[1], registers[0] };
    	return ConvertRegistersToLong(swappedRegisters);
    }
    
        /**
        * Convert two 16 Bit Registers to 32 Bit long value
        * @param        registers   16 Bit Registers
        * @return       32 bit value
        */
    public static int ConvertRegistersToInt(int[] registers) throws IllegalArgumentException
    {
        if (registers.length != 2)
            throw new IllegalArgumentException("Input Array length invalid");
        int highRegister = registers[1];
        int lowRegister = registers[0];
        byte[] highRegisterBytes = toByteArray(highRegister);
        byte[] lowRegisterBytes = toByteArray(lowRegister);
        byte[] doubleBytes = {
                                highRegisterBytes[1],
                                highRegisterBytes[0],
                                lowRegisterBytes[1],
                                lowRegisterBytes[0]
                            };
        return ByteBuffer.wrap(doubleBytes).getInt();
    }
    
        /**
        * Convert two 16 Bit Registers to 32 Bit long value
        * @param        registers   16 Bit Registers
        * @param        registerOrder    High Register first or low Register first
        * @return       32 bit value
        */
    public static int ConvertRegistersToInt(int[] registers, RegisterOrder registerOrder) throws IllegalArgumentException
    {
        int[] swappedRegisters = { registers[0], registers[1] };
        if (registerOrder == RegisterOrder.HighLow)
            swappedRegisters = new int[] { registers[1], registers[0] };
        return ConvertRegistersToInt(swappedRegisters);
    }
    
        /**
        * Convert 32 Bit real Value to two 16 Bit Value to send as Modbus Registers
        * @param        floatValue      real to be converted
        * @return       16 Bit Register values
        */
    public static int[] ConvertFloatToRegisters(float floatValue)
    {
        byte[] floatBytes = toByteArray(floatValue);
        byte[] highRegisterBytes = 
        {
        		0,0,
            floatBytes[0],
            floatBytes[1],

        };
        byte[] lowRegisterBytes = 
        {
            0,0,
            floatBytes[2],
            floatBytes[3],

        };
        int[] returnValue =
        {
        		ByteBuffer.wrap(lowRegisterBytes).getInt(),
        		ByteBuffer.wrap(highRegisterBytes).getInt()
        };
        return returnValue;
    }
    
        /**
        * Convert 32 Bit real Value to two 16 Bit Value to send as Modbus Registers
        * @param        floatValue      real to be converted
        * @param        registerOrder    High Register first or low Register first
        * @return       16 Bit Register values
        */
    public static int[] ConvertFloatToRegisters(float floatValue, RegisterOrder registerOrder)
    {
        int[] registerValues = ConvertFloatToRegisters(floatValue);
        int[] returnValue = registerValues;
        if (registerOrder == RegisterOrder.HighLow)
            returnValue = new int[] { registerValues[1], registerValues[0] };
        return returnValue;
    }
    
        /**
        * Convert 32 Bit Value to two 16 Bit Value to send as Modbus Registers
        * @param        intValue      Value to be converted
        * @return       16 Bit Register values
        */
    public static int[] ConvertIntToRegisters(int intValue)
    {
        byte[] doubleBytes = toByteArrayInt(intValue);
        byte[] highRegisterBytes = 
        {
        		0,0,
            doubleBytes[0],
            doubleBytes[1],

        };
        byte[] lowRegisterBytes = 
        {
            0,0,
            doubleBytes[2],
            doubleBytes[3],

        };
        int[] returnValue =
        {
        		ByteBuffer.wrap(lowRegisterBytes).getInt(),
        		ByteBuffer.wrap(highRegisterBytes).getInt()
        };
        return returnValue;
    }
    
       	/**
        * Convert 32 Bit Value to two 16 Bit Value to send as Modbus Registers
        * @param        intValue      Value to be converted
        * @param        registerOrder    High Register first or low Register first
        * @return       16 Bit Register values
        */
    public static int[] ConvertIntToRegisters(int intValue, RegisterOrder registerOrder)
    {
        int[] registerValues = ConvertIntToRegisters(intValue);
        int[] returnValue = registerValues;
        if (registerOrder == RegisterOrder.HighLow)
            returnValue = new int[] { registerValues[1], registerValues[0] };
        return returnValue;
    }
 
    /**
     * Convert 64 Bit Value to four 16 Bit Value to send as Modbus Registers
     * @param        longValue      Value to be converted
     * @return       16 Bit Register values
     */
	 public static int[] ConvertLongToRegisters(long longValue)
	 {
	     byte[] doubleBytes = toByteArrayLong(longValue);
	     byte[] highhighRegisterBytes = 
	     {
	     		0,0,
	         doubleBytes[0],
	         doubleBytes[1],
	
	     };
	     byte[] highlowRegisterBytes = 
	     {
	         0,0,
	         doubleBytes[2],
	         doubleBytes[3],
	
	     };
	     byte[] lowHighRegisterBytes = 
	     {
	         0,0,
	         doubleBytes[4],
	         doubleBytes[5],
	     };    
	     byte[] lowlowRegisterBytes = 
	     {
	         0,0,
	         doubleBytes[6],
	         doubleBytes[7],
	
	     };
	     int[] returnValue =
	     {
	     		ByteBuffer.wrap(lowlowRegisterBytes).getInt(),
	     		ByteBuffer.wrap(lowHighRegisterBytes).getInt(),
	     		ByteBuffer.wrap(highlowRegisterBytes).getInt(),
	     		ByteBuffer.wrap(highhighRegisterBytes).getInt(),
	     };
	     return returnValue;
	 }

    	/**
     * Convert 64 Bit Value to two 16 Bit Value to send as Modbus Registers
     * @param        longValue      Value to be converted
     * @param        registerOrder    High Register first or low Register first
     * @return       16 Bit Register values
     */
	 public static int[] ConvertLongToRegisters(int longValue, RegisterOrder registerOrder)
	 {
	     int[] registerValues = ConvertLongToRegisters(longValue);
	     int[] returnValue = registerValues;
	     if (registerOrder == RegisterOrder.HighLow)
	         returnValue = new int[] { registerValues[3], registerValues[2], registerValues[1], registerValues[0]};
	     return returnValue;
	 }
	 
	    /**
	     * Convert 64 Bit Value to four 16 Bit Value to send as Modbus Registers
	     * @param        doubleValue      Value to be converted
	     * @return       16 Bit Register values
	     */
		 public static int[] ConvertDoubleToRegisters(double doubleValue)
		 {
		     byte[] doubleBytes = toByteArrayDouble(doubleValue);
		     byte[] highhighRegisterBytes = 
		     {
		     		0,0,
		         doubleBytes[0],
		         doubleBytes[1],
		
		     };
		     byte[] highlowRegisterBytes = 
		     {
		         0,0,
		         doubleBytes[2],
		         doubleBytes[3],
		
		     };
		     byte[] lowHighRegisterBytes = 
		     {
		         0,0,
		         doubleBytes[4],
		         doubleBytes[5],
		     };    
		     byte[] lowlowRegisterBytes = 
		     {
		         0,0,
		         doubleBytes[6],
		         doubleBytes[7],
		
		     };
		     int[] returnValue =
		     {
		     		ByteBuffer.wrap(lowlowRegisterBytes).getInt(),
		     		ByteBuffer.wrap(lowHighRegisterBytes).getInt(),
		     		ByteBuffer.wrap(highlowRegisterBytes).getInt(),
		     		ByteBuffer.wrap(highhighRegisterBytes).getInt(),
		     };
		     return returnValue;
		 }
		 
		
    	/**
	     * Convert 64 Bit Value to two 16 Bit Value to send as Modbus Registers
	     * @param        doubleValue      Value to be converted
	     * @param        registerOrder    High Register first or low Register first
	     * @return       16 Bit Register values
	     */
		 public static int[] ConvertDoubleToRegisters(double doubleValue, RegisterOrder registerOrder)
		 {
		     int[] registerValues = ConvertDoubleToRegisters(doubleValue);
		     int[] returnValue = registerValues;
		     if (registerOrder == RegisterOrder.HighLow)
		         returnValue = new int[] { registerValues[3], registerValues[2], registerValues[1], registerValues[0]};
		     return returnValue;
		 }
  
   	/**
    * Converts 16 - Bit Register values to String
    * @param registers Register array received via Modbus
    * @param offset First Register containing the String to convert
    * @param stringLength number of characters in String (must be even)
    * @return Converted String
    */
    public static String ConvertRegistersToString(int[] registers, int offset, int stringLength)
    { 
    byte[] result = new byte[stringLength];
    byte[] registerResult = new byte[2];
    
        for (int i = 0; i < stringLength/2; i++)
        {
            registerResult = toByteArray(registers[offset + i]);
            result[i * 2] = registerResult[0];
            result[i * 2 + 1] = registerResult[1];
        }
        return new String(result);
    }  

   	/**
    * Converts a String to 16 - Bit Registers
    * @param stringToConvert String to Convert<
    * @return Converted String
    */
    public static int[] ConvertStringToRegisters(String stringToConvert)
    {
        byte[] array = stringToConvert.getBytes();
        int[] returnarray = new int[stringToConvert.length() / 2 + stringToConvert.length() % 2];
        for (int i = 0; i < returnarray.length; i++)
        {
            returnarray[i] = array[i * 2];
            if (i*2 +1< array.length)
            {
                returnarray[i] = returnarray[i] | ((int)array[i * 2 + 1] << 8);
            }
        }
        return returnarray;
    }

        /*
        * Read Discrete Inputs from Server
        * @param        startingAddress      Fist Address to read; Shifted by -1	
        * @param        quantity            Number of Inputs to read
        * @return       Discrete Inputs from Server
        * @throws sample.Modbus.de.re.easymodbus.exceptions.ModbusException
        * @throws UnknownHostException
        * @throws SocketException
        * @throws SerialPortTimeoutException 
        * @throws SerialPortException 
        */    
	public boolean[] ReadDiscreteInputs(int startingAddress, int quantity) throws ModbusException,
                UnknownHostException, SocketException, IOException
	{
		if (tcpClientSocket == null)
			throw new ConnectionException("connection Error");
		if (startingAddress > 65535 | quantity > 2000)
			throw new IllegalArgumentException("Starting adress must be 0 - 65535; quantity must be 0 - 2000");
		boolean[] response = null;
		this.transactionIdentifier = toByteArray(0x0001);
		this.protocolIdentifier = toByteArray(0x0000);
		this.length = toByteArray(0x0006);
		this.functionCode = 0x02;
		this.startingAddress = toByteArray(startingAddress);
		this.quantity = toByteArray(quantity);
		byte[] data = new byte[]
				{
					this.transactionIdentifier[1],
					this.transactionIdentifier[0],
					this.protocolIdentifier[1],
					this.protocolIdentifier[0],
					this.length[1],
					this.length[0],
					this.unitIdentifier,
					this.functionCode,
					this.startingAddress[1],
					this.startingAddress[0],
					this.quantity[1],
					this.quantity[0],
                    this.crc[0],
                    this.crc[1]					
				};
		
		if (tcpClientSocket.isConnected() | udpFlag)
		{
			if (udpFlag)
			{
				InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
				DatagramPacket sendPacket = new DatagramPacket(data, data.length-2, ipAddress, this.port);
				DatagramSocket clientSocket = new DatagramSocket();
				clientSocket.setSoTimeout(500);
			    clientSocket.send(sendPacket);
			    data = new byte[2100];
			    DatagramPacket receivePacket = new DatagramPacket(data, data.length);
			    clientSocket.receive(receivePacket);
			    clientSocket.close();
			    data = receivePacket.getData();
			}
			else
			{
				outStream.write(data, 0, data.length-2);
        		if (debug) StoreLogData.getInstance().Store("Send ModbusTCP-Data: "+Arrays.toString(data));          		
				if (sendDataChangedListener.size() > 0)
				{
					sendData = new byte[data.length-2];
					System.arraycopy(data, 0, sendData, 0, data.length-2);
					for (SendDataChangedListener hl : sendDataChangedListener)
						hl.SendDataChanged();
				}
				data = new byte[2100];
				int numberOfBytes = inStream.read(data, 0, data.length);
				if (receiveDataChangedListener.size() > 0)
				{
					receiveData = new byte[numberOfBytes];
					System.arraycopy(data, 0, receiveData, 0, numberOfBytes);
					for (ReceiveDataChangedListener hl : receiveDataChangedListener)
						hl.ReceiveDataChanged();
                    if (debug) StoreLogData.getInstance().Store("Receive ModbusTCP-Data: " + Arrays.toString(data));

				}
			}
			}
			if (((int) (data[7] & 0xff)) == 0x82 & ((int) data[8]) == 0x01)
			{
				if (debug) StoreLogData.getInstance().Store("FunctionCodeNotSupportedException Throwed");
				throw new FunctionCodeNotSupportedException("Function code not supported by master");
			}
			if (((int) (data[7] & 0xff)) == 0x82 & ((int) data[8]) == 0x02)
			{
				if (debug) StoreLogData.getInstance().Store("Starting adress invalid or starting adress + quantity invalid");
				throw new StartingAddressInvalidException("Starting adress invalid or starting adress + quantity invalid");
			}
			if (((int) (data[7] & 0xff)) == 0x82 & ((int) data[8]) == 0x03)
			{
				if (debug) StoreLogData.getInstance().Store("Quantity invalid");
				throw new QuantityInvalidException("Quantity invalid");
			}
			if (((int) (data[7] & 0xff)) == 0x82 & ((int) data[8]) == 0x04)
			{
				if (debug) StoreLogData.getInstance().Store("Error reading");
				throw new ModbusException("Error reading");
			}
			response = new boolean [quantity];
			for (int i = 0; i < quantity; i++)
			{
				int intData = data[9 + i/8];
				int mask = (int)Math.pow(2, (i%8));
				intData = ((intData & mask)/mask);
				if (intData >0)
					response[i] = true;
				else
					response[i] = false;
			}
			
		
		return (response);
	}

        /*
        * Read Coils from Server
        * @param        startingAddress      Fist Address to read; Shifted by -1	
        * @param        quantity            Number of Inputs to read
        * @return       coils from Server
        * @throws de.re.easymodbus.exceptions.ModbusException
        * @throws UnknownHostException
        * @throws SocketException
        * @throws SerialPortTimeoutException 
        * @throws SerialPortException 
        */
	public boolean[] ReadCoils(int startingAddress, int quantity) throws ModbusException,
                UnknownHostException, SocketException, IOException
	{
		if (tcpClientSocket == null)
			throw new ConnectionException("connection Error");
		if (startingAddress > 65535 | quantity > 2000)
			throw new IllegalArgumentException("Starting adress must be 0 - 65535; quantity must be 0 - 2000");
		boolean[] response = new boolean[quantity];
		this.transactionIdentifier = toByteArray(0x0001);
		this.protocolIdentifier = toByteArray(0x0000);
		this.length = toByteArray(0x0006);
		//this.unitIdentifier = 0x00;
		this.functionCode = 0x01;
		this.startingAddress = toByteArray(startingAddress);
		this.quantity = toByteArray(quantity);
		byte[] data = new byte[]
				{
					this.transactionIdentifier[1],
					this.transactionIdentifier[0],
					this.protocolIdentifier[1],
					this.protocolIdentifier[0],
					this.length[1],
					this.length[0],
					this.unitIdentifier,
					this.functionCode,
					this.startingAddress[1],
					this.startingAddress[0],
					this.quantity[1],
					this.quantity[0],
                    this.crc[0],
                    this.crc[1]		
				};

		if (tcpClientSocket.isConnected() | udpFlag)
		{
			if (udpFlag)
			{
                            InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
                            DatagramPacket sendPacket = new DatagramPacket(data, data.length, ipAddress, this.port);
                            DatagramSocket clientSocket = new DatagramSocket();
                            clientSocket.setSoTimeout(500);
			    clientSocket.send(sendPacket);
			    data = new byte[2100];
			    DatagramPacket receivePacket = new DatagramPacket(data, data.length);
			    clientSocket.receive(receivePacket);
			    clientSocket.close();
			    data = receivePacket.getData();
			}
			else
			{
				outStream.write(data, 0, data.length-2);
        		if (debug) StoreLogData.getInstance().Store("Send ModbusTCP-Data: "+Arrays.toString(data));   
				if (sendDataChangedListener.size() > 0)
				{
					sendData = new byte[data.length-2];
					System.arraycopy(data, 0, sendData, 0, data.length-2);
					for (SendDataChangedListener hl : sendDataChangedListener)
						hl.SendDataChanged();
				}
				data = new byte[2100];
				int numberOfBytes = inStream.read(data, 0, data.length);
				if (receiveDataChangedListener.size() > 0)
				{
					receiveData = new byte[numberOfBytes];
					System.arraycopy(data, 0, receiveData, 0, numberOfBytes);
					for (ReceiveDataChangedListener hl : receiveDataChangedListener)
						hl.ReceiveDataChanged();
					if (debug) StoreLogData.getInstance().Store("Receive ModbusTCP-Data: " + Arrays.toString(data));
				}
			}
                }
			if (((int) (data[7] & 0xff)) == 0x81 & ((int) data[8]) == 0x01)
			{
				if (debug) StoreLogData.getInstance().Store("FunctionCodeNotSupportedException Throwed");
				throw new FunctionCodeNotSupportedException("Function code not supported by master");
			}
			if (((int) (data[7] & 0xff)) == 0x81 & ((int) data[8]) == 0x02)
			{
				if (debug) StoreLogData.getInstance().Store("Starting adress invalid or starting adress + quantity invalid");
				throw new StartingAddressInvalidException("Starting adress invalid or starting adress + quantity invalid");
			}			
			if (((int) (data[7] & 0xff)) == 0x81 & ((int) data[8]) == 0x03)
			{
				if (debug) StoreLogData.getInstance().Store("Quantity invalid");
				throw new QuantityInvalidException("Quantity invalid");
			}
			if (((int) (data[7] & 0xff)) == 0x81 & ((int) data[8]) == 0x04)
			{
				if (debug) StoreLogData.getInstance().Store("Error reading");
				throw new ModbusException("Error reading");
			}
			for (int i = 0; i < quantity; i++)
			{
				int intData = (int) data[9 + i/8];
				int mask = (int)Math.pow(2, (i%8));
				intData = ((intData & mask)/mask);
				if (intData >0)
					response[i] = true;
				else
					response[i] = false;
			}
			
		
		return (response);
	}

        /*
        * Read Holding Registers from Server
        * @param        startingAddress      Fist Address to read; Shifted by -1	
        * @param        quantity            Number of Inputs to read
        * @return       Holding Registers from Server
        * @throws de.re.easymodbus.exceptions.ModbusException
        * @throws UnknownHostException
        * @throws SocketException
        * @throws SerialPortTimeoutException 
        * @throws SerialPortException 
        */
	public int[] ReadHoldingRegisters(int startingAddress, int quantity) throws ModbusException,
                UnknownHostException, SocketException, IOException
	{
		if (tcpClientSocket == null)
			throw new ConnectionException("connection Error");
		if (startingAddress > 65535 | quantity > 125)
			throw new IllegalArgumentException("Starting adress must be 0 - 65535; quantity must be 0 - 125");
		int[] response = new int[quantity];
		this.transactionIdentifier = toByteArray(0x0001);
		this.protocolIdentifier = toByteArray(0x0000);
		this.length = toByteArray(0x0006);
		//serialdata = this.unitIdentifier;
		this.functionCode = 0x03;
		this.startingAddress = toByteArray(startingAddress);
		this.quantity = toByteArray(quantity);

		byte[] data = new byte[]
				{
					this.transactionIdentifier[1],
					this.transactionIdentifier[0],
					this.protocolIdentifier[1],
					this.protocolIdentifier[0],
					this.length[1],
					this.length[0],
					this.unitIdentifier,
					this.functionCode,
					this.startingAddress[1],
					this.startingAddress[0],
					this.quantity[1],
					this.quantity[0],
                    this.crc[0],
                    this.crc[1]		
				};

		if (tcpClientSocket.isConnected() | udpFlag)
		{
			if (udpFlag)
			{
				InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
				DatagramPacket sendPacket = new DatagramPacket(data, data.length, ipAddress, this.port);
				DatagramSocket clientSocket = new DatagramSocket();
				clientSocket.setSoTimeout(500);
			    clientSocket.send(sendPacket);
			    data = new byte[2100];
			    DatagramPacket receivePacket = new DatagramPacket(data, data.length);
			    clientSocket.receive(receivePacket);
			    clientSocket.close();
			    data = receivePacket.getData();
			}
			else
			{
				outStream.write(data, 0, data.length-2);
        		if (debug) StoreLogData.getInstance().Store("Send ModbusTCP-Data: "+Arrays.toString(data));   
				if (sendDataChangedListener.size() > 0)
				{
					sendData = new byte[data.length-2];
					System.arraycopy(data, 0, sendData, 0, data.length-2);
					for (SendDataChangedListener hl : sendDataChangedListener)
						hl.SendDataChanged();
				}
				data = new byte[2100];
				int numberOfBytes = inStream.read(data, 0, data.length);
				if (receiveDataChangedListener.size() > 0)
				{
					receiveData = new byte[numberOfBytes];
					System.arraycopy(data, 0, receiveData, 0, numberOfBytes);
					for (ReceiveDataChangedListener hl : receiveDataChangedListener)
						hl.ReceiveDataChanged();
					if (debug) StoreLogData.getInstance().Store("Receive ModbusTCP-Data: " + Arrays.toString(data));
				}
                        }
			}
			if (((int) data[7]) == 0x83 & ((int) data[8]) == 0x01)
			{
				if (debug) StoreLogData.getInstance().Store("FunctionCodeNotSupportedException Throwed");
				throw new FunctionCodeNotSupportedException("Function code not supported by master");
			}
			if (((int) data[7]) == 0x83 & ((int) data[8]) == 0x02)
			{
				if (debug) StoreLogData.getInstance().Store("Starting adress invalid or starting adress + quantity invalid");
				throw new StartingAddressInvalidException("Starting adress invalid or starting adress + quantity invalid");
			}			
			if (((int) data[7]) == 0x83 & ((int) data[8]) == 0x03)
			{
				if (debug) StoreLogData.getInstance().Store("Quantity invalid");
				throw new QuantityInvalidException("Quantity invalid");
			}
			if (((int) data[7]) == 0x83 & ((int) data[8]) == 0x04)
			{
				if (debug) StoreLogData.getInstance().Store("Error reading");
				throw new ModbusException("Error reading");
			}
			for (int i = 0; i < quantity; i++)
			{
				byte[] bytes = new byte[2];
				bytes[0] = data[9+i*2];
				bytes[1] = data[9+i*2+1];
				ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
						
				response[i] = byteBuffer.getShort();
			}

		return (response);
	}

	/*
        * Read Input Registers from Server
        * @param        startingAddress      Fist Address to read; Shifted by -1	
        * @param        quantity            Number of Inputs to read
        * @return       Input Registers from Server
        * @throws de.re.easymodbus.exceptions.ModbusException
        * @throws UnknownHostException
        * @throws SocketException
	 * @throws SerialPortTimeoutException 
	 * @throws SerialPortException 
        */
	public int[] ReadInputRegisters(int startingAddress, int quantity) throws ModbusException,
                UnknownHostException, SocketException, IOException
	{
		if (tcpClientSocket == null)
			throw new ConnectionException("connection Error");
		if (startingAddress > 65535 | quantity > 5125)
			throw new IllegalArgumentException("Starting adress must be 0 - 65535; quantity must be 0 - 125");
		int[] response = new int[quantity];
		this.transactionIdentifier = toByteArray(0x0001);
		this.protocolIdentifier = toByteArray(0x0000);
		this.length = toByteArray(0x0006);
		//this.unitIdentifier = 0x00;
		this.functionCode = 0x04;
		this.startingAddress = toByteArray(startingAddress);
		this.quantity = toByteArray(quantity);
        //this.quantity[0] = 15;
        //this.quantity[1] = 15;
		byte[] data = new byte[]
				{
					this.transactionIdentifier[1],
					this.transactionIdentifier[0],
					this.protocolIdentifier[1],
					this.protocolIdentifier[0],
					this.length[1],
					this.length[0],
					this.unitIdentifier,
					this.functionCode,
					this.startingAddress[1],
					this.startingAddress[0],
					this.quantity[1],
					this.quantity[0],
                    this.crc[0],
                    this.crc[1]		
				};

		if (tcpClientSocket.isConnected() | udpFlag)
		{
			if (udpFlag)
			{
				InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
				DatagramPacket sendPacket = new DatagramPacket(data, data.length, ipAddress, this.port);
				DatagramSocket clientSocket = new DatagramSocket();
				clientSocket.setSoTimeout(500);
			    clientSocket.send(sendPacket);
			    data = new byte[2100];
			    DatagramPacket receivePacket = new DatagramPacket(data, data.length);
			    clientSocket.receive(receivePacket);
			    clientSocket.close();
			    data = receivePacket.getData();
			}
			else
			{
				outStream.write(data, 0, data.length-2);
        		if (debug) StoreLogData.getInstance().Store("Send ModbusTCP-Data: "+Arrays.toString(data));   
				if (sendDataChangedListener.size() > 0)
				{
					sendData = new byte[data.length-2];
					System.arraycopy(data, 0, sendData, 0, data.length-2);
					for (SendDataChangedListener hl : sendDataChangedListener)
						hl.SendDataChanged();
				}
				data = new byte[8009];
				DataInputStream in = new DataInputStream(inStream);
				in.readFully(data, 0, 9);
				int numberOfBytes = data.length;
				//int numberOfBytes = inStream.read(data, 0, data.length);
				if (receiveDataChangedListener.size() > 0)
				{
					receiveData = new byte[numberOfBytes];
					System.arraycopy(data, 0, receiveData, 0, numberOfBytes);
					for (ReceiveDataChangedListener hl : receiveDataChangedListener)
						hl.ReceiveDataChanged();
					if (debug) StoreLogData.getInstance().Store("Receive ModbusTCP-Data: " + Arrays.toString(data));
				}
			}
			if (((int) (data[7] & 0xff)) == 0x84 & ((int) data[8]) == 0x01)
			{
				if (debug) StoreLogData.getInstance().Store("FunctionCodeNotSupportedException Throwed");
				throw new FunctionCodeNotSupportedException("Function code not supported by master");
			}			
			if (((int) (data[7] & 0xff)) == 0x84 & ((int) data[8]) == 0x02)
			{
				if (debug) StoreLogData.getInstance().Store("Starting adress invalid or starting adress + quantity invalid");
				throw new StartingAddressInvalidException("Starting adress invalid or starting adress + quantity invalid");
			}			
			if (((int) (data[7] & 0xff)) == 0x84 & ((int) data[8]) == 0x03)
			{
				if (debug) StoreLogData.getInstance().Store("Quantity invalid");
				throw new QuantityInvalidException("Quantity invalid");
			}
			if (((int) (data[7] & 0xff)) == 0x84 & ((int) data[8]) == 0x04)
			{
				if (debug) StoreLogData.getInstance().Store("Error reading");
				throw new ModbusException("Error reading");
			}
			}
			for (int i = 0; i < quantity; i++)
			{
				byte[] bytes = new byte[2];
				bytes[0] = (byte) data[9+i*2];
				bytes[1] = (byte) data[9+i*2+1];
				ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
				response[i] = byteBuffer.getShort();
			}

		return (response);
	}
	
        /*
        * Write Single Coil to Server
        * @param        startingAddress      Address to write; Shifted by -1	
        * @param        value            Value to write to Server
        * @throws de.re.easymodbus.exceptions.ModbusException
        * @throws UnknownHostException
        * @throws SocketException
        * @throws SerialPortTimeoutException 
        * @throws SerialPortException 
        */
    public void WriteSingleCoil(int startingAddress, boolean value) throws ModbusException,
                UnknownHostException, SocketException, IOException
    {
        if (tcpClientSocket == null & !udpFlag)
            throw new ConnectionException("connection error");
        byte[] coilValue = new byte[2];
		this.transactionIdentifier = toByteArray(0x0001);
		this.protocolIdentifier = toByteArray(0x0000);
		this.length = toByteArray(0x0006);
		//this.unitIdentifier = 0;
		this.functionCode = 0x05;
		this.startingAddress = toByteArray(startingAddress);
        if (value == true)
        {
            coilValue = toByteArray((int)0xFF00);
        }
        else
        {
            coilValue = toByteArray((int)0x0000);
        }
        byte[] data = new byte[]{	this.transactionIdentifier[1],
						this.transactionIdentifier[0],
						this.protocolIdentifier[1],
						this.protocolIdentifier[0],
						this.length[1],
						this.length[0],
						this.unitIdentifier,
						this.functionCode,
						this.startingAddress[1],
						this.startingAddress[0],
						coilValue[1],
						coilValue[0],
	                    this.crc[0],
	                    this.crc[1]		
                        };

        if (tcpClientSocket.isConnected() | udpFlag)
        {
			if (udpFlag)
			{
				InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
				DatagramPacket sendPacket = new DatagramPacket(data, data.length, ipAddress, this.port);
				DatagramSocket clientSocket = new DatagramSocket();
				clientSocket.setSoTimeout(500);
			    clientSocket.send(sendPacket);
			    data = new byte[2100];
			    DatagramPacket receivePacket = new DatagramPacket(data, data.length);
			    clientSocket.receive(receivePacket);
			    clientSocket.close();
			    data = receivePacket.getData();
			}
			else
			{
				outStream.write(data, 0, data.length-2);
        		if (debug) StoreLogData.getInstance().Store("Send ModbusTCP-Data: "+Arrays.toString(data));   
				if (sendDataChangedListener.size() > 0)
				{
					sendData = new byte[data.length-2];
					System.arraycopy(data, 0, sendData, 0, data.length-2);
					for (SendDataChangedListener hl : sendDataChangedListener)
						hl.SendDataChanged();
				}
				data = new byte[2100];
				int numberOfBytes = inStream.read(data, 0, data.length);
				if (receiveDataChangedListener.size() > 0)
				{
					receiveData = new byte[numberOfBytes];
					System.arraycopy(data, 0, receiveData, 0, numberOfBytes);
					for (ReceiveDataChangedListener hl : receiveDataChangedListener)
						hl.ReceiveDataChanged();
					if (debug) StoreLogData.getInstance().Store("Receive ModbusTCP-Data: " + Arrays.toString(data));
				}
			}
        }
        if (((int)(data[7] & 0xff)) == 0x85 & data[8] == 0x01)
		{
			if (debug) StoreLogData.getInstance().Store("FunctionCodeNotSupportedException Throwed");
			throw new FunctionCodeNotSupportedException("Function code not supported by master");
		}        
        if (((int)(data[7] & 0xff)) == 0x85 & data[8] == 0x02)
		{
			if (debug) StoreLogData.getInstance().Store("Starting adress invalid or starting adress + quantity invalid");
			throw new StartingAddressInvalidException("Starting adress invalid or starting adress + quantity invalid");
		}        
        if (((int)(data[7] & 0xff)) == 0x85 & data[8] == 0x03)
		{
			if (debug) StoreLogData.getInstance().Store("Quantity invalid");
			throw new QuantityInvalidException("Quantity invalid");
		}        
        if (((int)(data[7] & 0xff)) == 0x85 & data[8] == 0x04)
		{
			if (debug) StoreLogData.getInstance().Store("Error reading");
			throw new ModbusException("Error reading");
		}
        }
    
        /*
        * Write Single Register to Server
        * @param        startingAddress      Address to write; Shifted by -1	
        * @param        value            Value to write to Server
        * @throws de.re.easymodbus.exceptions.ModbusException
        * @throws UnknownHostException
        * @throws SocketException
        * @throws SerialPortTimeoutException 
        * @throws SerialPortException 
        */
    public void WriteSingleRegister(int startingAddress, int value) throws ModbusException,
                UnknownHostException, SocketException, IOException
    {
        if (tcpClientSocket == null & !udpFlag)
            throw new ConnectionException("connection error");
        byte[] registerValue = new byte[2];
        this.transactionIdentifier = toByteArray((int)0x0001);
        this.protocolIdentifier = toByteArray((int)0x0000);
        this.length = toByteArray((int)0x0006);
        this.functionCode = 0x06;
        this.startingAddress = toByteArray(startingAddress);
            registerValue = toByteArray((short)value);

        byte[] data = new byte[]{	this.transactionIdentifier[1],
						this.transactionIdentifier[0],
						this.protocolIdentifier[1],
						this.protocolIdentifier[0],
						this.length[1],
						this.length[0],
						this.unitIdentifier,
						this.functionCode,
						this.startingAddress[1],
						this.startingAddress[0],
						registerValue[1],
						registerValue[0],
	                    this.crc[0],
	                    this.crc[1]		
                        };

        if (tcpClientSocket.isConnected() | udpFlag)
        {
		if (udpFlag)
		{
			InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
			DatagramPacket sendPacket = new DatagramPacket(data, data.length, ipAddress, this.port);
			DatagramSocket clientSocket = new DatagramSocket();
			clientSocket.setSoTimeout(500);
		    clientSocket.send(sendPacket);
		    data = new byte[2100];
		    DatagramPacket receivePacket = new DatagramPacket(data, data.length);
		    clientSocket.receive(receivePacket);
		    clientSocket.close();
		    data = receivePacket.getData();
		}
		else
		{
			outStream.write(data, 0, data.length-2);
    		if (debug) StoreLogData.getInstance().Store("Send ModbusTCP-Data: "+Arrays.toString(data));   
			if (sendDataChangedListener.size() > 0)
			{
				sendData = new byte[data.length-2];
				System.arraycopy(data, 0, sendData, 0, data.length-2);
				for (SendDataChangedListener hl : sendDataChangedListener)
					hl.SendDataChanged();
			}
			data = new byte[2100];
			int numberOfBytes = inStream.read(data, 0, data.length);
			if (receiveDataChangedListener.size() > 0)
			{
				receiveData = new byte[numberOfBytes];
				System.arraycopy(data, 0, receiveData, 0, numberOfBytes);
				for (ReceiveDataChangedListener hl : receiveDataChangedListener)
					hl.ReceiveDataChanged();
				if (debug) StoreLogData.getInstance().Store("Receive ModbusTCP-Data: " + Arrays.toString(data));
			}
		}
        }
        if (((int)(data[7] & 0xff)) == 0x86 & data[8] == 0x01)
		{
			if (debug) StoreLogData.getInstance().Store("FunctionCodeNotSupportedException Throwed");
			throw new FunctionCodeNotSupportedException("Function code not supported by master");
		}        
        if (((int)(data[7] & 0xff)) == 0x86 & data[8] == 0x02)
		{
			if (debug) StoreLogData.getInstance().Store("Starting adress invalid or starting adress + quantity invalid");
			throw new StartingAddressInvalidException("Starting adress invalid or starting adress + quantity invalid");
		}        
        if (((int)(data[7] & 0xff)) == 0x86 & data[8] == 0x03)
		{
			if (debug) StoreLogData.getInstance().Store("Quantity invalid");
			throw new QuantityInvalidException("Quantity invalid");
		}
        if (((int)(data[7] & 0xff)) == 0x86 & data[8] == 0x04)
		{
			if (debug) StoreLogData.getInstance().Store("Error reading");
			throw new ModbusException("Error reading");
		}
       }
    
       /*
        * Write Multiple Coils to Server
        * @param        startingAddress      Firts Address to write; Shifted by -1	
        * @param        values           Values to write to Server
        * @throws de.re.easymodbus.exceptions.ModbusException
        * @throws UnknownHostException
        * @throws SocketException
     * @throws SerialPortTimeoutException 
     * @throws SerialPortException 
        */
    public void WriteMultipleCoils(int startingAddress, boolean[] values) throws ModbusException,
                UnknownHostException, SocketException, IOException
    {
        byte byteCount = (byte)(values.length/8+1);
        if (values.length % 8 == 0)
        	byteCount=(byte)(byteCount-1);
        byte[] quantityOfOutputs = toByteArray((int)values.length);
        byte singleCoilValue = 0;
        if (tcpClientSocket == null & !udpFlag)
            throw new ConnectionException("connection error");
        this.transactionIdentifier = toByteArray((int)0x0001);
        this.protocolIdentifier = toByteArray((int)0x0000);
        this.length = toByteArray((int)(7+(values.length/8+1)));
        this.functionCode = 0x0F;
        this.startingAddress = toByteArray(startingAddress);

        byte[] data = new byte[16 + byteCount-1];
        data[0] = this.transactionIdentifier[1];
        data[1] = this.transactionIdentifier[0];
        data[2] = this.protocolIdentifier[1];
        data[3] = this.protocolIdentifier[0];
		data[4] = this.length[1];
		data[5] = this.length[0];
		data[6] = this.unitIdentifier;
		data[7] = this.functionCode;
		data[8] = this.startingAddress[1];
		data[9] = this.startingAddress[0];
        data[10] = quantityOfOutputs[1];
        data[11] = quantityOfOutputs[0];
        data[12] = byteCount;
        for (int i = 0; i < values.length; i++)
        {
            if ((i % 8) == 0)
                singleCoilValue = 0;
            byte CoilValue;
            if (values[i] == true)
                CoilValue = 1;
            else
                CoilValue = 0;


            singleCoilValue = (byte)((int)CoilValue<<(i%8) | (int)singleCoilValue);

            data[13 + (i / 8)] = singleCoilValue;            
        }

        if (tcpClientSocket.isConnected() | udpFlag)
        {
		if (udpFlag)
		{
			InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
			DatagramPacket sendPacket = new DatagramPacket(data, data.length, ipAddress, this.port);
			DatagramSocket clientSocket = new DatagramSocket();
			clientSocket.setSoTimeout(500);
		    clientSocket.send(sendPacket);
		    data = new byte[2100];
		    DatagramPacket receivePacket = new DatagramPacket(data, data.length);
		    clientSocket.receive(receivePacket);
		    clientSocket.close();
		    data = receivePacket.getData();
		}
		else
		{
			outStream.write(data, 0, data.length-2);
    		if (debug) StoreLogData.getInstance().Store("Send ModbusTCP-Data: "+Arrays.toString(data));   
			if (sendDataChangedListener.size() > 0)
			{
				sendData = new byte[data.length-2];
				System.arraycopy(data, 0, sendData, 0, data.length-2);
				for (SendDataChangedListener hl : sendDataChangedListener)
					hl.SendDataChanged();
			}
			data = new byte[2100];
			int numberOfBytes = inStream.read(data, 0, data.length);
			if (receiveDataChangedListener.size() > 0)
			{
				receiveData = new byte[numberOfBytes];
				System.arraycopy(data, 0, receiveData, 0, numberOfBytes);
				for (ReceiveDataChangedListener hl : receiveDataChangedListener)
					hl.ReceiveDataChanged();
				if (debug) StoreLogData.getInstance().Store("Receive ModbusTCP-Data: " + Arrays.toString(data));
			}
		}
        }
        if (((int)(data[7] & 0xff)) == 0x8F & data[8] == 0x01)
		{
			if (debug) StoreLogData.getInstance().Store("FunctionCodeNotSupportedException Throwed");
			throw new FunctionCodeNotSupportedException("Function code not supported by master");
		}       
        if (((int)(data[7] & 0xff)) == 0x8F & data[8] == 0x02)
		{
			if (debug) StoreLogData.getInstance().Store("Starting adress invalid or starting adress + quantity invalid");
			throw new StartingAddressInvalidException("Starting adress invalid or starting adress + quantity invalid");
		}
        if (((int)(data[7] & 0xff)) == 0x8F & data[8] == 0x03)
		{
			if (debug) StoreLogData.getInstance().Store("Quantity invalid");
			throw new QuantityInvalidException("Quantity invalid");
		}
        if (((int)(data[7] & 0xff)) == 0x8F & data[8] == 0x04)
		{
			if (debug) StoreLogData.getInstance().Store("Error reading");
			throw new ModbusException("Error reading");
		}
        }
    
        /*
        * Write Multiple Registers to Server
        * @param        startingAddress      Firts Address to write; Shifted by -1	
        * @param        values           Values to write to Server
        * @throws de.re.easymodbus.exceptions.ModbusException
        * @throws UnknownHostException
        * @throws SocketException
        * @throws SerialPortTimeoutException 
        * @throws SerialPortException 
        */    public void WriteMultipleRegisters(int startingAddress, int[] values) throws ModbusException,
                UnknownHostException, SocketException, IOException/*/////,SerialPortException, SerialPortTimeoutException*/

    {
        byte byteCount = (byte)(values.length * 2);
        byte[] quantityOfOutputs = toByteArray((int)values.length);
        if (tcpClientSocket == null & !udpFlag)
            throw new ConnectionException("connection error");
        this.transactionIdentifier = toByteArray((int)0x0001);
        this.protocolIdentifier = toByteArray((int)0x0000);
        this.length = toByteArray((int)(7+values.length*2));
        this.functionCode = 0x10;
        this.startingAddress = toByteArray(startingAddress);

        byte[] data = new byte[15 + values.length*2];
        data[0] = this.transactionIdentifier[1];
        data[1] = this.transactionIdentifier[0];
        data[2] = this.protocolIdentifier[1];
        data[3] = this.protocolIdentifier[0];
        data[4] = this.length[1];
        data[5] = this.length[0];
        data[6] = this.unitIdentifier;
        data[7] = this.functionCode;
        data[8] = this.startingAddress[1];
        data[9] = this.startingAddress[0];
        data[10] = quantityOfOutputs[1];
        data[11] = quantityOfOutputs[0];
        data[12] = byteCount;
        for (int i = 0; i < values.length; i++)
        {
            byte[] singleRegisterValue = toByteArray((int)values[i]);
            data[13 + i*2] = singleRegisterValue[1];
            data[14 + i*2] = singleRegisterValue[0];
        }

        if (tcpClientSocket.isConnected() | udpFlag)
        {
		if (udpFlag)
		{
			InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
			DatagramPacket sendPacket = new DatagramPacket(data, data.length, ipAddress, this.port);
			DatagramSocket clientSocket = new DatagramSocket();
			clientSocket.setSoTimeout(500);
		    clientSocket.send(sendPacket);
		    data = new byte[2100];
		    DatagramPacket receivePacket = new DatagramPacket(data, data.length);
		    clientSocket.receive(receivePacket);
		    clientSocket.close();
		    data = receivePacket.getData();
		}
		else
		{
			outStream.write(data, 0, data.length-2);
    		if (debug) StoreLogData.getInstance().Store("Send ModbusTCP-Data: "+Arrays.toString(data));   
			if (sendDataChangedListener.size() > 0)
			{
				sendData = new byte[data.length-2];
				System.arraycopy(data, 0, sendData, 0, data.length-2);
				for (SendDataChangedListener hl : sendDataChangedListener)
					hl.SendDataChanged();
			}
			data = new byte[2100];
			int numberOfBytes = inStream.read(data, 0, data.length);
			if (receiveDataChangedListener.size() > 0)
			{
				receiveData = new byte[numberOfBytes];
				System.arraycopy(data, 0, receiveData, 0, numberOfBytes);
				for (ReceiveDataChangedListener hl : receiveDataChangedListener)
					hl.ReceiveDataChanged();
				if (debug) StoreLogData.getInstance().Store("Receive ModbusTCP-Data: " + Arrays.toString(data));
			}
		}
        }
        if (((int)(data[7] & 0xff)) == 0x90 & data[8] == 0x01)
		{
			if (debug) StoreLogData.getInstance().Store("FunctionCodeNotSupportedException Throwed");
			throw new FunctionCodeNotSupportedException("Function code not supported by master");
		}        
        if (((int)(data[7] & 0xff)) == 0x90 & data[8] == 0x02)
		{
			if (debug) StoreLogData.getInstance().Store("Starting adress invalid or starting adress + quantity invalid");
			throw new StartingAddressInvalidException("Starting adress invalid or starting adress + quantity invalid");
		}
        if (((int)(data[7] & 0xff)) == 0x90 & data[8] == 0x03)
		{
			if (debug) StoreLogData.getInstance().Store("Quantity invalid");
			throw new QuantityInvalidException("Quantity invalid");
		}
        if (((int)(data[7] & 0xff)) == 0x90 & data[8] == 0x04)
		{
			if (debug) StoreLogData.getInstance().Store("Error reading");
			throw new ModbusException("Error reading");
		}
        }
	
        /*
        * Read and Write Multiple Registers to Server
        * @param        startingAddressRead      Firts Address to Read; Shifted by -1	
        * @param        quantityRead            Number of Values to Read
        * @param        startingAddressWrite      Firts Address to write; Shifted by -1	
        * @param        values                  Values to write to Server
        * @return       Register Values from Server
        * @throws de.re.easymodbus.exceptions.ModbusException
        * @throws UnknownHostException
        * @throws SocketException
        * @throws SerialPortTimeoutException 
        * @throws SerialPortException 
        */
    public int[] ReadWriteMultipleRegisters(int startingAddressRead, int quantityRead, int startingAddressWrite, int[] values) throws ModbusException,
                UnknownHostException, SocketException, IOException
    {
        byte [] startingAddressReadLocal = new byte[2];
	    byte [] quantityReadLocal = new byte[2];
        byte[] startingAddressWriteLocal = new byte[2];
        byte[] quantityWriteLocal = new byte[2];
        byte writeByteCountLocal = 0;
        if (tcpClientSocket == null & !udpFlag)
            throw new ConnectionException("connection error");
        if (startingAddressRead > 65535 | quantityRead > 125 | startingAddressWrite > 65535 | values.length > 121)
            throw new IllegalArgumentException("Starting address must be 0 - 65535; quantity must be 0 - 125");
        int[] response;
        this.transactionIdentifier = toByteArray((int)0x0001);
        this.protocolIdentifier = toByteArray((int)0x0000);
        this.length = toByteArray((int)0x0006);
        this.functionCode = 0x17;
        startingAddressReadLocal = toByteArray(startingAddressRead);
        quantityReadLocal = toByteArray(quantityRead);
        startingAddressWriteLocal = toByteArray(startingAddressWrite);
        quantityWriteLocal = toByteArray(values.length);
        writeByteCountLocal = (byte)(values.length * 2);
        byte[] data = new byte[19+ values.length*2];
        data[0] =               this.transactionIdentifier[1];
        data[1] =   		    this.transactionIdentifier[0];
		data[2] =   			this.protocolIdentifier[1];
		data[3] =   			this.protocolIdentifier[0];
		data[4] =   			this.length[1];
		data[5] =   			this.length[0];
		data[6] =   			this.unitIdentifier;
		data[7] =   		    this.functionCode;
		data[8] =   			startingAddressReadLocal[1];
		data[9] =   			startingAddressReadLocal[0];
		data[10] =   			quantityReadLocal[1];
		data[11] =   			quantityReadLocal[0];
        data[12] =               startingAddressWriteLocal[1];
		data[13] =   			startingAddressWriteLocal[0];
		data[14] =   			quantityWriteLocal[1];
		data[15] =   			quantityWriteLocal[0];
        data[16] =              writeByteCountLocal;

        for (int i = 0; i < values.length; i++)
        {
            byte[] singleRegisterValue = toByteArray((int)values[i]);
            data[17 + i*2] = singleRegisterValue[1];
            data[18 + i*2] = singleRegisterValue[0];
        }

        if (tcpClientSocket.isConnected() | udpFlag)
        {
		if (udpFlag)
		{
			InetAddress ipAddress = InetAddress.getByName(this.ipAddress);
			DatagramPacket sendPacket = new DatagramPacket(data, data.length, ipAddress, this.port);
			DatagramSocket clientSocket = new DatagramSocket();
			clientSocket.setSoTimeout(500);
		    clientSocket.send(sendPacket);
		    data = new byte[2100];
		    DatagramPacket receivePacket = new DatagramPacket(data, data.length);
		    clientSocket.receive(receivePacket);
		    clientSocket.close();
		    data = receivePacket.getData();
		}
		else
		{
			outStream.write(data, 0, data.length-2);
    		if (debug) StoreLogData.getInstance().Store("Send ModbusTCP-Data: "+Arrays.toString(data));   
			if (sendDataChangedListener.size() > 0)
			{
				sendData = new byte[data.length-2];
				System.arraycopy(data, 0, sendData, 0, data.length-2);
				for (SendDataChangedListener hl : sendDataChangedListener)
					hl.SendDataChanged();
			}
			data = new byte[2100];
			int numberOfBytes = inStream.read(data, 0, data.length);
			if (receiveDataChangedListener.size() > 0)
			{
				receiveData = new byte[numberOfBytes];
				System.arraycopy(data, 0, receiveData, 0, numberOfBytes);
				for (ReceiveDataChangedListener hl : receiveDataChangedListener)
					hl.ReceiveDataChanged();
				if (debug) StoreLogData.getInstance().Store("Receive ModbusTCP-Data: " + Arrays.toString(data));
			}
		}
        }
        if (((int)(data[7] & 0xff)) == 0x97 & data[8] == 0x01)
		{
			if (debug) StoreLogData.getInstance().Store("FunctionCodeNotSupportedException Throwed");
			throw new FunctionCodeNotSupportedException("Function code not supported by master");
		}        
        if (((int)(data[7] & 0xff)) == 0x97 & data[8] == 0x02)
		{
			if (debug) StoreLogData.getInstance().Store("Starting adress invalid or starting adress + quantity invalid");
			throw new StartingAddressInvalidException("Starting adress invalid or starting adress + quantity invalid");
		}
        if (((int)(data[7] & 0xff)) == 0x97 & data[8] == 0x03)
		{
			if (debug) StoreLogData.getInstance().Store("Quantity invalid");
			throw new QuantityInvalidException("Quantity invalid");
		}
        if (((int)(data[7] & 0xff)) == 0x97 & data[8] == 0x04)
		{
			if (debug) StoreLogData.getInstance().Store("Error reading");
			throw new ModbusException("Error reading");
		}
        response = new int[quantityRead];
        for (int i = 0; i < quantityRead; i++)
        {
            byte lowByte;
            byte highByte;
            highByte = data[9 + i * 2];
            lowByte = data[9 + i * 2 + 1];
            
            byte[] bytes = new byte[] {highByte, lowByte};
            
            
			ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
			response[i] = byteBuffer.getShort();
        }
        return (response);
    }
    
        /*
        * Close connection to Server
        * @throws IOException
         * @throws SerialPortException 
        */
	public void Disconnect() throws IOException
	{
			if (inStream!=null)
				inStream.close();
			if (outStream!=null)
				outStream.close();
			if (tcpClientSocket != null)
				tcpClientSocket.close();
			tcpClientSocket = null;
	}
	
	
	public static byte[] toByteArray(int value)
    {
		byte[] result = new byte[2];
	    result[1] = (byte) (value >> 8);
		result[0] = (byte) (value);
	    return result;
	}

	public static byte[] toByteArrayInt(int value)
    {
		return ByteBuffer.allocate(4).putInt(value).array();
	}
	
	public static byte[] toByteArrayLong(long value)
    {
		return ByteBuffer.allocate(8).putLong(value).array();
	}
	
	public static byte[] toByteArrayDouble(double value)
    {
		return ByteBuffer.allocate(8).putDouble(value).array();
	}
	
	public static byte[] toByteArray(float value)
    {
		 return ByteBuffer.allocate(4).putFloat(value).array();
	}
	
        /**
        * client connected to Server
        * @return  if Client is connected to Server
        */
	public boolean isConnected()
	{
		boolean returnValue = false;
		if (tcpClientSocket == null)
			returnValue = false;
		else
		{
			if (tcpClientSocket.isConnected())
				returnValue = true;
			else
				returnValue = false;
		}
		return returnValue;
	}
	
	public boolean Available(int timeout)
	{
        InetAddress address;
		try {
			address = InetAddress.getByName(this.ipAddress);
			boolean reachable = address.isReachable(timeout);
			return reachable;
		} catch (IOException e) 
		{
			e.printStackTrace();
			return false;			
		}
        
	}
	
	
	
        /**
        * Returns ip Address of Server
        * @return ip address of server
        */
	public String getipAddress()
	{
		return ipAddress;
	}
        
         /**
        * sets IP-Address of server
        * @param        ipAddress                  ipAddress of Server
        */
	public void setipAddress(String ipAddress)
	{
		this.ipAddress = ipAddress;
	}
	
        /**
        * Returns port of Server listening
        * @return port of Server listening
        */
	public int getPort()
	{
		return port;
	}
        
        /**
        * sets Portof server
        * @param        port                  Port of Server
        */
	public void setPort(int port)
	{
		this.port = port;
	}	
	
        /**
        * Returns UDP-Flag which enables Modbus UDP and disabled Modbus TCP
        * @return UDP Flag
        */
	public boolean getUDPFlag()
	{
		return udpFlag;
	}
        
        /**
        * sets UDP-Flag which enables Modbus UDP and disables Mopdbus TCP
        * @param        udpFlag      UDP Flag
        */
	public void setUDPFlag(boolean udpFlag)
	{
		this.udpFlag = udpFlag;
	}
	
	public int getConnectionTimeout()
	{
		return connectTimeout;
	}
	public void setConnectionTimeout(int connectionTimeout)
	{
		this.connectTimeout = connectionTimeout;
	}
        
    public void setSerialFlag(boolean serialflag)
    {
        this.serialflag = serialflag;
    }
    
    public boolean getSerialFlag()
    {
        return this.serialflag;
    }
    
    public void setUnitIdentifier(byte unitIdentifier)
    {
        this.unitIdentifier = unitIdentifier;
    }
    
    public byte getUnitIdentifier()
    {
        return this.unitIdentifier;
    }

    /**
     * Sets and enables the Logfilename which writes information about received and send messages to File
     * @param logFileName	File name to log files
     */
    public void setLogFileName(String logFileName)
    {
    	StoreLogData.getInstance().setFilename(logFileName);
    	debug = true;
    }
      
    public void addReveiveDataChangedListener(ReceiveDataChangedListener toAdd) 
    {
        receiveDataChangedListener.add(toAdd);
    }
    public void addSendDataChangedListener(SendDataChangedListener toAdd) 
    {
        sendDataChangedListener.add(toAdd);
    }	
	
}                                                                                                