/*
 * Creative Commons license: Attribution-NonCommercial-NoDerivatives 4.0 International (CC BY-NC-ND 4.0)
 *You are free to:
 *
 *Share ó copy and redistribute the material in any medium or format
 *The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 *Under the following terms:
 *
 *Attribution ó You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 *NonCommercial ó You may not use the material for commercial purposes.
 *NoDerivatives ó If you remix, transform, or build upon the material, you may not distribute the modified material.
 */
package de.re.easymodbus.modbusclient;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.io.*;
import java.util.*;
import java.io.InputStream;
import java.io.OutputStream;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;


     /**
     * @author Stefan Ro√ümann
     */
public class ModbusClient 
{
	public enum RegisterOrder { LowHigh, HighLow };
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
	private int connectTimeout = 500;
	private InputStream inStream;
	private DataOutputStream outStream;
    public byte[] receiveData;
    public byte[] sendData;  
	private List<ReceiveDataChangedListener> receiveDataChangedListener = new ArrayList<ReceiveDataChangedListener>();
	private List<SendDataChangedListener> sendDataChangedListener = new ArrayList<SendDataChangedListener>();
	private SerialPort serialPort;
	
	public ModbusClient(String ipAddress, int port)
	{
		System.out.println("EasyModbus Client Library");
		System.out.println("Copyright (c) Stefan Rossmann Engineering Solutions");
		System.out.println("www.rossmann-engineering.de");
		System.out.println("");
		System.out.println("Creative commons license");
		System.out.println("Attribution-NonCommercial-NoDerivatives 4.0 International (CC BY-NC-ND 4.0)");
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
	}
	
        /**
        * Connects to ModbusServer
        * @throws UnknownHostException
        * @throws IOException
        */        
	public void Connect() throws UnknownHostException, IOException
	{
		if (!udpFlag)
		{
			tcpClientSocket.setSoTimeout(connectTimeout);
			tcpClientSocket = new Socket(ipAddress, port);
			outStream = new DataOutputStream(tcpClientSocket.getOutputStream());
			inStream = tcpClientSocket.getInputStream();
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
		tcpClientSocket.setSoTimeout(connectTimeout);
		tcpClientSocket = new Socket(ipAddress, port);
		outStream = new DataOutputStream(tcpClientSocket.getOutputStream());
		inStream = tcpClientSocket.getInputStream();
	}
        
        /**
        * Connects to ModbusServer with serial connection
        * @param comPort  used Com-Port
        * @throws UnknownHostException
        * @throws IOException
        */   
        OutputStream out;
        InputStream in;
        CommPortIdentifier portIdentifier;
	public void Connect(String comPort) throws Exception
	{
            portIdentifier = CommPortIdentifier.getPortIdentifier( comPort );
                if( portIdentifier.isCurrentlyOwned() ) {
        System.out.println( "Error: Port is currently in use" );
        } else {
      int timeout = 2000;
      
      CommPort commPort = portIdentifier.open( this.getClass().getName(), timeout );
 
      if( commPort instanceof SerialPort ) {
        serialPort = ( SerialPort )commPort;
        serialPort.setSerialPortParams( 9600,
                                        SerialPort.DATABITS_8,
                                        SerialPort.STOPBITS_1,
                                        SerialPort.PARITY_EVEN );
 
        in = serialPort.getInputStream();
        out = serialPort.getOutputStream();
        
        serialflag = true; 
      } else {
        System.out.println( "Error: Only serial ports are handled by this example." );
      }
	}  
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
        * Convert two 16 Bit Registers to 32 Bit long value
        * @param        registers   16 Bit Registers
        * @return       32 bit value
        */
    public static int ConvertRegistersToDouble(int[] registers) throws IllegalArgumentException
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
    public static int ConvertRegistersToDouble(int[] registers, RegisterOrder registerOrder) throws IllegalArgumentException
    {
        int[] swappedRegisters = { registers[0], registers[1] };
        if (registerOrder == RegisterOrder.HighLow)
            swappedRegisters = new int[] { registers[1], registers[0] };
        return ConvertRegistersToDouble(swappedRegisters);
    }
    
        /**
        * Convert 32 Bit real Value to two 16 Bit Value to send as Modbus Registers
        * @param        floatValue      real to be converted
        * @return       16 Bit Register values
        */
    public static int[] ConvertFloatToTwoRegisters(float floatValue)
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
    public static int[] ConvertFloatToTwoRegisters(float floatValue, RegisterOrder registerOrder)
    {
        int[] registerValues = ConvertFloatToTwoRegisters(floatValue);
        int[] returnValue = registerValues;
        if (registerOrder == RegisterOrder.HighLow)
            returnValue = new int[] { registerValues[1], registerValues[0] };
        return returnValue;
    }
    
        /**
        * Convert 32 Bit Value to two 16 Bit Value to send as Modbus Registers
        * @param        doubleValue      Value to be converted
        * @return       16 Bit Register values
        */
    public static int[] ConvertDoubleToTwoRegisters(int doubleValue)
    {
        byte[] doubleBytes = toByteArrayDouble(doubleValue);
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
        * @param        doubleValue      Value to be converted
        * @param        registerOrder    High Register first or low Register first
        * @return       16 Bit Register values
        */
    public static int[] ConvertDoubleToTwoRegisters(int doubleValue, RegisterOrder registerOrder)
    {
        int[] registerValues = ConvertFloatToTwoRegisters(doubleValue);
        int[] returnValue = registerValues;
        if (registerOrder == RegisterOrder.HighLow)
            returnValue = new int[] { registerValues[1], registerValues[0] };
        return returnValue;
    }

    
        public static byte[] calculateCRC(byte[] data, int numberOfBytes, int startByte)
        { 
           byte[] auchCRCHi = {
            (byte)0x00, (byte)0xC1, (byte)0x81, (byte)0x40, (byte)0x01, (byte)0xC0, (byte)0x80, (byte)0x41, (byte)0x01, (byte)0xC0, (byte)0x80, (byte)0x41, (byte)0x00, (byte)0xC1, (byte)0x81,
            (byte)0x40, (byte)0x01, (byte)0xC0, (byte)0x80, (byte)0x41, (byte)0x00, (byte)0xC1, (byte)0x81, (byte)0x40, (byte)0x00, (byte)0xC1, (byte)0x81, (byte)0x40, (byte)0x01, (byte)0xC0,
            (byte)0x80, (byte)0x41, (byte)0x01, (byte)0xC0, (byte)0x80, (byte)0x41, (byte)0x00, (byte)0xC1, (byte)0x81, (byte)0x40, (byte)0x00, (byte)0xC1, (byte)0x81, (byte)0x40, (byte)0x01,
            (byte)0xC0, (byte)0x80, (byte)0x41, (byte)0x00, (byte)0xC1, (byte)0x81, (byte)0x40, (byte)0x01, (byte)0xC0, (byte)0x80, (byte)0x41, (byte)0x01, (byte)0xC0, (byte)0x80, (byte)0x41,
            (byte)0x00, (byte)0xC1, (byte)0x81, (byte)0x40, (byte)0x01, (byte)0xC0, (byte)0x80, (byte)0x41, (byte)0x00, (byte)0xC1, (byte)0x81, (byte)0x40, (byte)0x00, (byte)0xC1, (byte)0x81,
            (byte)0x40, (byte)0x01, (byte)0xC0, (byte)0x80, (byte)0x41, (byte)0x00, (byte)0xC1, (byte)0x81, (byte)0x40, (byte)0x01, (byte)0xC0, (byte)0x80, (byte)0x41, (byte)0x01, (byte)0xC0,
            (byte)0x80, (byte)0x41, (byte)0x00, (byte)0xC1, (byte)0x81, (byte)0x40, (byte)0x00, (byte)0xC1, (byte)0x81, (byte)0x40, (byte)0x01, (byte)0xC0, (byte)0x80, (byte)0x41, (byte)0x01,
            (byte)0xC0, (byte)0x80, (byte)0x41, (byte)0x00, (byte)0xC1, (byte)0x81, (byte)0x40, (byte)0x01, (byte)0xC0, (byte)0x80, (byte)0x41, (byte)0x00, (byte)0xC1, (byte)0x81, (byte)0x40,
            (byte)0x00, (byte)0xC1, (byte)0x81, (byte)0x40, (byte)0x01, (byte)0xC0, (byte)0x80, (byte)0x41, (byte)0x01, (byte)0xC0, (byte)0x80, (byte)0x41, (byte)0x00, (byte)0xC1, (byte)0x81,
            (byte)0x40, (byte)0x00, (byte)0xC1, (byte)0x81, (byte)0x40, (byte)0x01, (byte)0xC0, (byte)0x80, (byte)0x41, (byte)0x00, (byte)0xC1, (byte)0x81, (byte)0x40, (byte)0x01, (byte)0xC0,
            (byte)0x80, (byte)0x41, (byte)0x01, (byte)0xC0, (byte)0x80, (byte)0x41, (byte)0x00, (byte)0xC1, (byte)0x81, (byte)0x40, (byte)0x00, (byte)0xC1, (byte)0x81, (byte)0x40, (byte)0x01,
            (byte)0xC0, (byte)0x80, (byte)0x41, (byte)0x01, (byte)0xC0, (byte)0x80, (byte)0x41, (byte)0x00, (byte)0xC1, (byte)0x81, (byte)0x40, (byte)0x01, (byte)0xC0, (byte)0x80, (byte)0x41,
            (byte)0x00, (byte)0xC1, (byte)0x81, (byte)0x40, (byte)0x00, (byte)0xC1, (byte)0x81, (byte)0x40, (byte)0x01, (byte)0xC0, (byte)0x80, (byte)0x41, (byte)0x00, (byte)0xC1, (byte)0x81,
            (byte)0x40, (byte)0x01, (byte)0xC0, (byte)0x80, (byte)0x41, (byte)0x01, (byte)0xC0, (byte)0x80, (byte)0x41, (byte)0x00, (byte)0xC1, (byte)0x81, (byte)0x40, (byte)0x01, (byte)0xC0,
            (byte)0x80, (byte)0x41, (byte)0x00, (byte)0xC1, (byte)0x81, (byte)0x40, (byte)0x00, (byte)0xC1, (byte)0x81, (byte)0x40, (byte)0x01, (byte)0xC0, (byte)0x80, (byte)0x41, (byte)0x01,
            (byte)0xC0, (byte)0x80, (byte)0x41, (byte)0x00, (byte)0xC1, (byte)0x81, (byte)0x40, (byte)0x00, (byte)0xC1, (byte)0x81, (byte)0x40, (byte)0x01, (byte)0xC0, (byte)0x80, (byte)0x41,
            (byte)0x00, (byte)0xC1, (byte)0x81, (byte)0x40, (byte)0x01, (byte)0xC0, (byte)0x80, (byte)0x41, (byte)0x01, (byte)0xC0, (byte)0x80, (byte)0x41, (byte)0x00, (byte)0xC1, (byte)0x81,
            (byte)0x40
            };
		
            byte[] auchCRCLo = {
            (byte)0x00, (byte)0xC0, (byte)0xC1, (byte)0x01, (byte)0xC3, (byte)0x03, (byte)0x02, (byte)0xC2, (byte)0xC6, (byte)0x06, (byte)0x07, (byte)0xC7, (byte)0x05, (byte)0xC5, (byte)0xC4,
            (byte)0x04, (byte)0xCC, (byte)0x0C, (byte)0x0D, (byte)0xCD, (byte)0x0F, (byte)0xCF, (byte)0xCE, (byte)0x0E, (byte)0x0A, (byte)0xCA, (byte)0xCB, (byte)0x0B, (byte)0xC9, (byte)0x09,
            (byte)0x08, (byte)0xC8, (byte)0xD8, (byte)0x18, (byte)0x19, (byte)0xD9, (byte)0x1B, (byte)0xDB, (byte)0xDA, (byte)0x1A, (byte)0x1E, (byte)0xDE, (byte)0xDF, (byte)0x1F, (byte)0xDD,
            (byte)0x1D, (byte)0x1C, (byte)0xDC, (byte)0x14, (byte)0xD4, (byte)0xD5, (byte)0x15, (byte)0xD7, (byte)0x17, (byte)0x16, (byte)0xD6, (byte)0xD2, (byte)0x12, (byte)0x13, (byte)0xD3,
            (byte)0x11, (byte)0xD1, (byte)0xD0, (byte)0x10, (byte)0xF0, (byte)0x30, (byte)0x31, (byte)0xF1, (byte)0x33, (byte)0xF3, (byte)0xF2, (byte)0x32, (byte)0x36, (byte)0xF6, (byte)0xF7,
            (byte)0x37, (byte)0xF5, (byte)0x35, (byte)0x34, (byte)0xF4, (byte)0x3C, (byte)0xFC, (byte)0xFD, (byte)0x3D, (byte)0xFF, (byte)0x3F, (byte)0x3E, (byte)0xFE, (byte)0xFA, (byte)0x3A,
            (byte)0x3B, (byte)0xFB, (byte)0x39, (byte)0xF9, (byte)0xF8, (byte)0x38, (byte)0x28, (byte)0xE8, (byte)0xE9, (byte)0x29, (byte)0xEB, (byte)0x2B, (byte)0x2A, (byte)0xEA, (byte)0xEE,
            (byte)0x2E, (byte)0x2F, (byte)0xEF, (byte)0x2D, (byte)0xED, (byte)0xEC, (byte)0x2C, (byte)0xE4, (byte)0x24, (byte)0x25, (byte)0xE5, (byte)0x27, (byte)0xE7, (byte)0xE6, (byte)0x26,
            (byte)0x22, (byte)0xE2, (byte)0xE3, (byte)0x23, (byte)0xE1, (byte)0x21, (byte)0x20, (byte)0xE0, (byte)0xA0, (byte)0x60, (byte)0x61, (byte)0xA1, (byte)0x63, (byte)0xA3, (byte)0xA2,
            (byte)0x62, (byte)0x66, (byte)0xA6, (byte)0xA7, (byte)0x67, (byte)0xA5, (byte)0x65, (byte)0x64, (byte)0xA4, (byte)0x6C, (byte)0xAC, (byte)0xAD, (byte)0x6D, (byte)0xAF, (byte)0x6F,
            (byte)0x6E, (byte)0xAE, (byte)0xAA, (byte)0x6A, (byte)0x6B, (byte)0xAB, (byte)0x69, (byte)0xA9, (byte)0xA8, (byte)0x68, (byte)0x78, (byte)0xB8, (byte)0xB9, (byte)0x79, (byte)0xBB,
            (byte)0x7B, (byte)0x7A, (byte)0xBA, (byte)0xBE, (byte)0x7E, (byte)0x7F, (byte)0xBF, (byte)0x7D, (byte)0xBD, (byte)0xBC, (byte)0x7C, (byte)0xB4, (byte)0x74, (byte)0x75, (byte)0xB5,
            (byte)0x77, (byte)0xB7, (byte)0xB6, (byte)0x76, (byte)0x72, (byte)0xB2, (byte)0xB3, (byte)0x73, (byte)0xB1, (byte)0x71, (byte)0x70, (byte)0xB0, (byte)0x50, (byte)0x90, (byte)0x91,
            (byte)0x51, (byte)0x93, (byte)0x53, (byte)0x52, (byte)0x92, (byte)0x96, (byte)0x56, (byte)0x57, (byte)0x97, (byte)0x55, (byte)0x95, (byte)0x94, (byte)0x54, (byte)0x9C, (byte)0x5C,
            (byte)0x5D, (byte)0x9D, (byte)0x5F, (byte)0x9F, (byte)0x9E, (byte)0x5E, (byte)0x5A, (byte)0x9A, (byte)0x9B, (byte)0x5B, (byte)0x99, (byte)0x59, (byte)0x58, (byte)0x98, (byte)0x88,
            (byte)0x48, (byte)0x49, (byte)0x89, (byte)0x4B, (byte)0x8B, (byte)0x8A, (byte)0x4A, (byte)0x4E, (byte)0x8E, (byte)0x8F, (byte)0x4F, (byte)0x8D, (byte)0x4D, (byte)0x4C, (byte)0x8C,
            (byte)0x44, (byte)0x84, (byte)0x85, (byte)0x45, (byte)0x87, (byte)0x47, (byte)0x46, (byte)0x86, (byte)0x82, (byte)0x42, (byte)0x43, (byte)0x83, (byte)0x41, (byte)0x81, (byte)0x80,
            (byte)0x40
            };
            short usDataLen = (short)numberOfBytes;
            byte  uchCRCHi = (byte)0xFF ; 
            byte uchCRCLo = (byte)0xFF ; 
            int i = 0;
            int uIndex ;
            while (usDataLen>0) 
            {
                usDataLen--;
                uIndex = (int)(uchCRCLo ^ (int)data[i+startByte]); 
                if (uIndex<0)
                    uIndex = 256+uIndex;
                uchCRCLo = (byte) (uchCRCHi ^ auchCRCHi[uIndex]) ; 
                uchCRCHi = (byte) auchCRCLo[uIndex] ;
                i++;
            }
            byte[] returnValue = {uchCRCLo, uchCRCHi};
            return returnValue ;
        }
    
    
        /**
        * Read Discrete Inputs from Server
        * @param        startingAddress      Fist Address to read; Shifted by -1	
        * @param        quantity            Number of Inputs to read
        * @return       Discrete Inputs from Server
        * @throws de.re.easymodbus.exceptions.ModbusException
        * @throws UnknownHostException
        * @throws SocketException
        */    
	public boolean[] ReadDiscreteInputs(int startingAddress, int quantity) throws de.re.easymodbus.exceptions.ModbusException,
                UnknownHostException, SocketException, IOException
	{
		if (tcpClientSocket == null)
			throw new de.re.easymodbus.exceptions.ConnectionException("connection Error");
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
        if (this.serialflag)
        {
            crc = calculateCRC(data, 6, 6);
            data[data.length -2] = crc[0];
            data[data.length -1] = crc[1];
        }
        byte[] serialdata =null;
        if (serialflag)
        {
           
            out.write(data,6,8);
            byte receivedUnitIdentifier = (byte)0xFF;
            int len = -1;
            byte[] serialBuffer = new byte[256];
            serialdata = new byte[256];
            int expectedlength = 5+quantity/8+1;
            if (quantity % 8 == 0)
                expectedlength = 5+quantity/8;
            int currentLength = 0;
                while (currentLength < expectedlength)
                 {
                     len = -1;
                   
                       while (( len = this.in.read(serialBuffer)) <=0);
                   
                       for (int i = 0; i < len; i++)
                   {
                       serialdata[currentLength] = serialBuffer[i];
                       currentLength++;
                   }                       
               }              
           receivedUnitIdentifier = serialdata[0];
           if (receivedUnitIdentifier != this.unitIdentifier)
           {
                serialdata = new byte[256];     
           }
        }
        if (serialdata != null)
        {
            data = new byte[262]; 
            System.arraycopy(serialdata, 0, data, 6, serialdata.length);
        }
		
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
				}
			}
			}
			if (((int) (data[7] & 0xff)) == 0x82 & ((int) data[8]) == 0x01)
				throw new de.re.easymodbus.exceptions.FunctionCodeNotSupportedException("Function code not supported by master");
			if (((int) (data[7] & 0xff)) == 0x82 & ((int) data[8]) == 0x02)
				throw new de.re.easymodbus.exceptions.StartingAddressInvalidException("Starting adress invalid or starting adress + quantity invalid");
			if (((int) (data[7] & 0xff)) == 0x82 & ((int) data[8]) == 0x03)
				throw new de.re.easymodbus.exceptions.QuantityInvalidException("Quantity invalid");
			if (((int) (data[7] & 0xff)) == 0x82 & ((int) data[8]) == 0x04)
				throw new de.re.easymodbus.exceptions.ModbusException("Error reading");
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
	
        /**
        * Read Coils from Server
        * @param        startingAddress      Fist Address to read; Shifted by -1	
        * @param        quantity            Number of Inputs to read
        * @return       coils from Server
        * @throws de.re.easymodbus.exceptions.ModbusException
        * @throws UnknownHostException
        * @throws SocketException
        */
	public boolean[] ReadCoils(int startingAddress, int quantity) throws de.re.easymodbus.exceptions.ModbusException,
                UnknownHostException, SocketException, IOException
	{
		if (tcpClientSocket == null)
			throw new de.re.easymodbus.exceptions.ConnectionException("connection Error");
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
            if (this.serialflag)
            {
                crc = calculateCRC(data, 6, 6);
                data[data.length -2] = crc[0];
                data[data.length -1] = crc[1];
            }
            byte[] serialdata =null;
            if (serialflag)
            {
               
                out.write(data,6,8);
                byte receivedUnitIdentifier = (byte)0xFF;
                int len = -1;
                byte[] serialBuffer = new byte[256];
                serialdata = new byte[256];
                int expectedlength = 5+quantity/8+1;
                if (quantity % 8 == 0)
                    expectedlength = 5+quantity/8;
                int currentLength = 0;
           	
                    while ((currentLength < expectedlength))
                     {
                         len = -1;
                       
                           while (( len = this.in.read(serialBuffer)) <=0);
                       
                           for (int i = 0; i < len; i++)
                       {
                           serialdata[currentLength] = serialBuffer[i];
                           currentLength++;
                       }                       
                   }              

               receivedUnitIdentifier = serialdata[0];
               if (receivedUnitIdentifier != this.unitIdentifier)
               {
                    serialdata = new byte[256];     
               }
            }
            if (serialdata != null)
            {
                data = new byte[262]; 
                System.arraycopy(serialdata, 0, data, 6, serialdata.length);
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
				}
			}
                }
			if (((int) (data[7] & 0xff)) == 0x81 & ((int) data[8]) == 0x01)
				throw new de.re.easymodbus.exceptions.FunctionCodeNotSupportedException("Function code not supported by master");
			if (((int) (data[7] & 0xff)) == 0x81 & ((int) data[8]) == 0x02)
				throw new de.re.easymodbus.exceptions.StartingAddressInvalidException("Starting adress invalid or starting adress + quantity invalid");
			if (((int) (data[7] & 0xff)) == 0x81 & ((int) data[8]) == 0x03)
				throw new de.re.easymodbus.exceptions.QuantityInvalidException("Quantity invalid");
			if (((int) (data[7] & 0xff)) == 0x81 & ((int) data[8]) == 0x04)
				throw new de.re.easymodbus.exceptions.ModbusException("Error reading");
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
        
        /**
        * Read Holding Registers from Server
        * @param        startingAddress      Fist Address to read; Shifted by -1	
        * @param        quantity            Number of Inputs to read
        * @return       Holding Registers from Server
        * @throws de.re.easymodbus.exceptions.ModbusException
        * @throws UnknownHostException
        * @throws SocketException
        */
	public int[] ReadHoldingRegisters(int startingAddress, int quantity) throws de.re.easymodbus.exceptions.ModbusException,
                UnknownHostException, SocketException, IOException
	{
		if (tcpClientSocket == null)
			throw new de.re.easymodbus.exceptions.ConnectionException("connection Error");
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
            
            if (this.serialflag)
            {
                crc = calculateCRC(data, 6, 6);
                data[data.length -2] = crc[0];
                data[data.length -1] = crc[1];
            }
            byte[] serialdata =null;   
            if (serialflag)
            {             
               out.write(data,6,8);
               byte receivedUnitIdentifier = (byte)0xFF;
               int len = -1;
               byte[] serialBuffer = new byte[256];
               serialdata = new byte[256];
               int expectedlength = 5+2*quantity;
               int currentLength = 0;
                   while (currentLength < expectedlength)
                   {
                       len = -1;
                       
                       while (( len = this.in.read(serialBuffer)) <=0);
                       
                       for (int i = 0; i < len; i++)
                       {
                           serialdata[currentLength] = serialBuffer[i];
                           currentLength++;
                       }                       
                   }
                   
               receivedUnitIdentifier = serialdata[0];
               if (receivedUnitIdentifier != this.unitIdentifier)
               {
                    data = new byte[256];                       
               }
               if (serialdata != null)
               {
                   data = new byte[262]; 
                   System.arraycopy(serialdata, 0, data, 6, serialdata.length);
               }
                for (int i = 0; i < quantity; i++)
                {
                    byte[] bytes = new byte[2];
                    bytes[0] = data[3+i*2];
                    bytes[1] = data[3+i*2+1];
                    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);						
                    response[i] = byteBuffer.getShort();
		}	
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
				}
                        }
			}
			if (((int) data[7]) == 0x83 & ((int) data[8]) == 0x01)
				throw new de.re.easymodbus.exceptions.FunctionCodeNotSupportedException("Function code not supported by master");
			if (((int) data[7]) == 0x83 & ((int) data[8]) == 0x02)
				throw new de.re.easymodbus.exceptions.StartingAddressInvalidException("Starting adress invalid or starting adress + quantity invalid");
			if (((int) data[7]) == 0x83 & ((int) data[8]) == 0x03)
				throw new de.re.easymodbus.exceptions.QuantityInvalidException("Quantity invalid");
			if (((int) data[7]) == 0x83 & ((int) data[8]) == 0x04)
				throw new de.re.easymodbus.exceptions.ModbusException("Error reading");
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
	
	/**
        * Read Input Registers from Server
        * @param        startingAddress      Fist Address to read; Shifted by -1	
        * @param        quantity            Number of Inputs to read
        * @return       Input Registers from Server
        * @throws de.re.easymodbus.exceptions.ModbusException
        * @throws UnknownHostException
        * @throws SocketException
        */
	public int[] ReadInputRegisters(int startingAddress, int quantity) throws de.re.easymodbus.exceptions.ModbusException,
                UnknownHostException, SocketException, IOException
	{
		if (tcpClientSocket == null)
			throw new de.re.easymodbus.exceptions.ConnectionException("connection Error");
		if (startingAddress > 65535 | quantity > 125)
			throw new IllegalArgumentException("Starting adress must be 0 - 65535; quantity must be 0 - 125");
		int[] response = new int[quantity];
		this.transactionIdentifier = toByteArray(0x0001);
		this.protocolIdentifier = toByteArray(0x0000);
		this.length = toByteArray(0x0006);
		//this.unitIdentifier = 0x00;
		this.functionCode = 0x04;
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
        if (this.serialflag)
        {
            crc = calculateCRC(data, 6, 6);
            data[data.length -2] = crc[0];
            data[data.length -1] = crc[1];
        }
        byte[] serialdata =null;   
        if (serialflag)
        {             
           out.write(data,6,8);
           byte receivedUnitIdentifier = (byte)0xFF;
           int len = -1;
           byte[] serialBuffer = new byte[256];
           serialdata = new byte[256];
           int expectedlength = 5+2*quantity;
           int currentLength = 0;

               while (currentLength < expectedlength)
               {
                   len = -1;
                   
                   while (( len = this.in.read(serialBuffer)) <=0);
                   
                   for (int i = 0; i < len; i++)
                   {
                       serialdata[currentLength] = serialBuffer[i];
                       currentLength++;
                   }                       
               }
               
           
           receivedUnitIdentifier = serialdata[0];
           if (receivedUnitIdentifier != this.unitIdentifier)
           {
                data = new byte[256];                       
           }
           if (serialdata != null)
           {
               data = new byte[262]; 
               System.arraycopy(serialdata, 0, data, 6, serialdata.length);
           }
            for (int i = 0; i < quantity; i++)
            {
                byte[] bytes = new byte[2];
                bytes[0] = data[3+i*2];
                bytes[1] = data[3+i*2+1];
                ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);						
                response[i] = byteBuffer.getShort();
            }	
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
				}
			}
			if (((int) (data[7] & 0xff)) == 0x84 & ((int) data[8]) == 0x01)
				throw new de.re.easymodbus.exceptions.FunctionCodeNotSupportedException("Function code not supported by master");
			if (((int) (data[7] & 0xff)) == 0x84 & ((int) data[8]) == 0x02)
				throw new de.re.easymodbus.exceptions.StartingAddressInvalidException("Starting adress invalid or starting adress + quantity invalid");
			if (((int) (data[7] & 0xff)) == 0x84 & ((int) data[8]) == 0x03)
				throw new de.re.easymodbus.exceptions.QuantityInvalidException("Quantity invalid");
			if (((int) (data[7] & 0xff)) == 0x84 & ((int) data[8]) == 0x04)
				throw new de.re.easymodbus.exceptions.ModbusException("Error reading");
			for (int i = 0; i < quantity; i++)
			{
				byte[] bytes = new byte[2];
				bytes[0] = (byte) data[9+i*2];
				bytes[1] = (byte) data[9+i*2+1];
				ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
				response[i] = byteBuffer.getShort();
			}
			
		}
		return (response);
	}
	
        /**
        * Write Single Coil to Server
        * @param        startingAddress      Address to write; Shifted by -1	
        * @param        value            Value to write to Server
        * @throws de.re.easymodbus.exceptions.ModbusException
        * @throws UnknownHostException
        * @throws SocketException
        */
    public void WriteSingleCoil(int startingAddress, boolean value) throws de.re.easymodbus.exceptions.ModbusException,
                UnknownHostException, SocketException, IOException
    {
        if (tcpClientSocket == null & !udpFlag)
            throw new de.re.easymodbus.exceptions.ConnectionException("connection error");
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
        if (this.serialflag)
        {
            crc = calculateCRC(data, 6, 6);
            data[data.length -2] = crc[0];
            data[data.length -1] = crc[1];
        }
        byte[] serialdata =null;   
        if (serialflag)
        {             
           out.write(data,6,8);
           byte receivedUnitIdentifier = (byte)0xFF;
           int len = -1;
           byte[] serialBuffer = new byte[256];
           serialdata = new byte[256];
           int expectedlength = 8;
           int currentLength = 0;
               while (currentLength < expectedlength)
               {
                   len = -1;
                   
                   while (( len = this.in.read(serialBuffer)) <=0);
                   
                   for (int i = 0; i < len; i++)
                   {
                       serialdata[currentLength] = serialBuffer[i];
                       currentLength++;
                   }                       
               }
               
           receivedUnitIdentifier = serialdata[0];
           if (receivedUnitIdentifier != this.unitIdentifier)
           {
                data = new byte[256];                       
           }
        }
        if (serialdata != null)
        {
            data = new byte[262]; 
            System.arraycopy(serialdata, 0, data, 6, serialdata.length);
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
				}
			}
        }
        if (((int)(data[7] & 0xff)) == 0x85 & data[8] == 0x01)
            throw new de.re.easymodbus.exceptions.FunctionCodeNotSupportedException("Function code not supported by master");
        if (((int)(data[7] & 0xff)) == 0x85 & data[8] == 0x02)
            throw new de.re.easymodbus.exceptions.StartingAddressInvalidException("Starting address invalid or starting address + quantity invalid");
        if (((int)(data[7] & 0xff)) == 0x85 & data[8] == 0x03)
            throw new de.re.easymodbus.exceptions.QuantityInvalidException("quantity invalid");
        if (((int)(data[7] & 0xff)) == 0x85 & data[8] == 0x04)
            throw new de.re.easymodbus.exceptions.ModbusException("error reading");
    }
    
        /**
        * Write Single Register to Server
        * @param        startingAddress      Address to write; Shifted by -1	
        * @param        value            Value to write to Server
        * @throws de.re.easymodbus.exceptions.ModbusException
        * @throws UnknownHostException
        * @throws SocketException
        */
    public void WriteSingleRegister(int startingAddress, int value) throws de.re.easymodbus.exceptions.ModbusException,
                UnknownHostException, SocketException, IOException
    {
        if (tcpClientSocket == null & !udpFlag)
            throw new de.re.easymodbus.exceptions.ConnectionException("connection error");
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
        if (this.serialflag)
        {
            crc = calculateCRC(data, 6, 6);
            data[data.length -2] = crc[0];
            data[data.length -1] = crc[1];
        }
        byte[] serialdata =null;   
        if (serialflag)
        {             
           out.write(data,6,8);
           byte receivedUnitIdentifier = (byte)0xFF;
           int len = -1;
           byte[] serialBuffer = new byte[256];
           serialdata = new byte[256];
           int expectedlength = 8;
           int currentLength = 0;
               while (currentLength < expectedlength)
               {
                   len = -1;
                   
                   while (( len = this.in.read(serialBuffer)) <=0);
                   
                   for (int i = 0; i < len; i++)
                   {
                       serialdata[currentLength] = serialBuffer[i];
                       currentLength++;
                   }                       
               }
               
           receivedUnitIdentifier = serialdata[0];
           if (receivedUnitIdentifier != this.unitIdentifier)
           {
                data = new byte[256];                       
           }
        }
        if (serialdata != null)
        {
            data = new byte[262]; 
            System.arraycopy(serialdata, 0, data, 6, serialdata.length);
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
			}
		}
        }
        if (((int)(data[7] & 0xff)) == 0x86 & data[8] == 0x01)
            throw new de.re.easymodbus.exceptions.FunctionCodeNotSupportedException("Function code not supported by master");
        if (((int)(data[7] & 0xff)) == 0x86 & data[8] == 0x02)
            throw new de.re.easymodbus.exceptions.StartingAddressInvalidException("Starting address invalid or starting address + quantity invalid");
        if (((int)(data[7] & 0xff)) == 0x86 & data[8] == 0x03)
            throw new de.re.easymodbus.exceptions.QuantityInvalidException("quantity invalid");
        if (((int)(data[7] & 0xff)) == 0x86 & data[8] == 0x04)
            throw new de.re.easymodbus.exceptions.ModbusException("error reading");
    }
    
       /**
        * Write Multiple Coils to Server
        * @param        startingAddress      Firts Address to write; Shifted by -1	
        * @param        values           Values to write to Server
        * @throws de.re.easymodbus.exceptions.ModbusException
        * @throws UnknownHostException
        * @throws SocketException
        */
    public void WriteMultipleCoils(int startingAddress, boolean[] values) throws de.re.easymodbus.exceptions.ModbusException,
                UnknownHostException, SocketException, IOException
    {
        byte byteCount = (byte)(values.length/8+1);
        if (values.length % 8 == 0)
        	byteCount=(byte)(byteCount-1);
        byte[] quantityOfOutputs = toByteArray((int)values.length);
        byte singleCoilValue = 0;
        if (tcpClientSocket == null & !udpFlag)
            throw new de.re.easymodbus.exceptions.ConnectionException("connection error");
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
        if (this.serialflag)
        {
            crc = calculateCRC(data, data.length-8,6);
            data[data.length -2] = crc[0];
            data[data.length -1] = crc[1];
        }
        byte[] serialdata =null;   
        if (serialflag)
        {             
           out.write(data,6,9+byteCount);
           byte receivedUnitIdentifier = (byte)0xFF;
           int len = -1;
           byte[] serialBuffer = new byte[256];
           serialdata = new byte[256];
           int expectedlength = 8;
           int currentLength = 0;

               while (currentLength < expectedlength)
               {
                   len = -1;
                   
                   while (( len = this.in.read(serialBuffer)) <=0);
                   
                   for (int i = 0; i < len; i++)
                   {
                       serialdata[currentLength] = serialBuffer[i];
                       currentLength++;
                   }                       
               }
               

           receivedUnitIdentifier = serialdata[0];
           if (receivedUnitIdentifier != this.unitIdentifier)
           {
                data = new byte[256];                       
           }
        }
        if (serialdata != null)
        {
            data = new byte[262]; 
            System.arraycopy(serialdata, 0, data, 6, serialdata.length);
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
			}
		}
        }
        if (((int)(data[7] & 0xff)) == 0x8F & data[8] == 0x01)
            throw new de.re.easymodbus.exceptions.FunctionCodeNotSupportedException("Function code not supported by master");
        if (((int)(data[7] & 0xff)) == 0x8F & data[8] == 0x02)
            throw new de.re.easymodbus.exceptions.StartingAddressInvalidException("Starting address invalid or starting address + quantity invalid");
        if (((int)(data[7] & 0xff)) == 0x8F & data[8] == 0x03)
            throw new de.re.easymodbus.exceptions.QuantityInvalidException("quantity invalid");
        if (((int)(data[7] & 0xff)) == 0x8F & data[8] == 0x04)
            throw new de.re.easymodbus.exceptions.ModbusException("error reading");
    }
    
        /**
        * Write Multiple Registers to Server
        * @param        startingAddress      Firts Address to write; Shifted by -1	
        * @param        values           Values to write to Server
        * @throws de.re.easymodbus.exceptions.ModbusException
        * @throws UnknownHostException
        * @throws SocketException
        */    public void WriteMultipleRegisters(int startingAddress, int[] values) throws de.re.easymodbus.exceptions.ModbusException,
                UnknownHostException, SocketException, IOException

    {
        byte byteCount = (byte)(values.length * 2);
        byte[] quantityOfOutputs = toByteArray((int)values.length);
        if (tcpClientSocket == null & !udpFlag)
            throw new de.re.easymodbus.exceptions.ConnectionException("connection error");
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
        if (this.serialflag)
        {
            crc = calculateCRC(data, data.length-8,6);
            data[data.length -2] = crc[0];
            data[data.length -1] = crc[1];
        }
        byte[] serialdata =null;   
        if (serialflag)
        {             
           out.write(data,6,9+byteCount);
           byte receivedUnitIdentifier = (byte)0xFF;
           int len = -1;
           byte[] serialBuffer = new byte[256];
           serialdata = new byte[256];
           int expectedlength = 8;
           int currentLength = 0;

               while (currentLength < expectedlength)
               {
                   len = -1;
                   
                   while (( len = this.in.read(serialBuffer)) <=0);
                   
                   for (int i = 0; i < len; i++)
                   {
                       serialdata[currentLength] = serialBuffer[i];
                       currentLength++;
                   }                       
               }
               
           receivedUnitIdentifier = serialdata[0];
           if (receivedUnitIdentifier != this.unitIdentifier)
           {
                data = new byte[256];                       
           }
        }
        if (serialdata != null)
        {
            data = new byte[262]; 
            System.arraycopy(serialdata, 0, data, 6, serialdata.length);
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
			}
		}
        }
        if (((int)(data[7] & 0xff)) == 0x90 & data[8] == 0x01)
            throw new de.re.easymodbus.exceptions.FunctionCodeNotSupportedException("Function code not supported by master");
        if (((int)(data[7] & 0xff)) == 0x90 & data[8] == 0x02)
            throw new de.re.easymodbus.exceptions.StartingAddressInvalidException("Starting address invalid or starting address + quantity invalid");
        if (((int)(data[7] & 0xff)) == 0x90 & data[8] == 0x03)
            throw new de.re.easymodbus.exceptions.QuantityInvalidException("quantity invalid");
        if (((int)(data[7] & 0xff)) == 0x90 & data[8] == 0x04)
            throw new de.re.easymodbus.exceptions.ModbusException("error reading");
    }
	
        /**
        * Read and Write Multiple Registers to Server
        * @param        startingAddressRead      Firts Address to Read; Shifted by -1	
        * @param        quantityRead            Number of Values to Read
        * @param        startingAddressWrite      Firts Address to write; Shifted by -1	
        * @param        values                  Values to write to Server
        * @return       Register Values from Server
        * @throws de.re.easymodbus.exceptions.ModbusException
        * @throws UnknownHostException
        * @throws SocketException
        */
    public int[] ReadWriteMultipleRegisters(int startingAddressRead, int quantityRead, int startingAddressWrite, int[] values) throws de.re.easymodbus.exceptions.ModbusException,
                UnknownHostException, SocketException, IOException
    {
        byte [] startingAddressReadLocal = new byte[2];
	    byte [] quantityReadLocal = new byte[2];
        byte[] startingAddressWriteLocal = new byte[2];
        byte[] quantityWriteLocal = new byte[2];
        byte writeByteCountLocal = 0;
        if (tcpClientSocket == null & !udpFlag)
            throw new de.re.easymodbus.exceptions.ConnectionException("connection error");
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
        if (this.serialflag)
        {
            crc = calculateCRC(data, data.length-8,6);
            data[data.length -2] = crc[0];
            data[data.length -1] = crc[1];
        }
        byte[] serialdata =null;   
        if (serialflag)
        {             
           out.write(data,6,13+writeByteCountLocal);
           byte receivedUnitIdentifier = (byte)0xFF;
           int len = -1;
           byte[] serialBuffer = new byte[256];
           serialdata = new byte[256];
           int expectedlength = 5+quantityRead;
           int currentLength = 0;
               while (currentLength < expectedlength)
               {
                   len = -1;
                   
                   while (( len = this.in.read(serialBuffer)) <=0);
                   
                   for (int i = 0; i < len; i++)
                   {
                       serialdata[currentLength] = serialBuffer[i];
                       currentLength++;
                   }                       
               }
               
           receivedUnitIdentifier = serialdata[0];
           if (receivedUnitIdentifier != this.unitIdentifier)
           {
                data = new byte[256];                       
           }
        }
        if (serialdata != null)
        {
            data = new byte[262]; 
            System.arraycopy(serialdata, 0, data, 6, serialdata.length);
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
			}
		}
        }
        if (((int)(data[7] & 0xff)) == 0x97 & data[8] == 0x01)
            throw new de.re.easymodbus.exceptions.FunctionCodeNotSupportedException("Function code not supported by master");
        if (((int)(data[7] & 0xff)) == 0x97 & data[8] == 0x02)
            throw new de.re.easymodbus.exceptions.StartingAddressInvalidException("Starting address invalid or starting address + quantity invalid");
        if (((int)(data[7] & 0xff)) == 0x97 & data[8] == 0x03)
            throw new de.re.easymodbus.exceptions.QuantityInvalidException("quantity invalid");
        if (((int)(data[7] & 0xff)) == 0x97 & data[8] == 0x04)
            throw new de.re.easymodbus.exceptions.ModbusException("error reading");
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
    
        /**
        * Close connection to Server
        * @throws IOException
        */
	public void Disconnect() throws IOException
	{
		if (!serialflag)
		{
			if (inStream!=null)
				inStream.close();
			if (outStream!=null)
				outStream.close();
			if (tcpClientSocket != null)
				tcpClientSocket.close();
			tcpClientSocket = null;
		}
		else
		{
			if (serialPort != null)
				serialPort.close();
		}
	}
	
	
	public static byte[] toByteArray(int value)
    {
		byte[] result = new byte[2];
	    result[1] = (byte) (value >> 8);
		result[0] = (byte) (value);
	    return result;
	}

	public static byte[] toByteArrayDouble(int value)
    {
		return ByteBuffer.allocate(4).putInt(value).array();
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
		if (serialflag)
		{
			if (portIdentifier == null)
				return false;
			if (portIdentifier.isCurrentlyOwned())
				return true;
			else
				return false;
		}
			
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
        
        public void addReveiveDataChangedListener(ReceiveDataChangedListener toAdd) 
        {
            receiveDataChangedListener.add(toAdd);
        }
        public void addSendDataChangedListener(SendDataChangedListener toAdd) 
        {
            sendDataChangedListener.add(toAdd);
        }	
	
}                                                                                                