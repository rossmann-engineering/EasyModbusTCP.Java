/*
Copyright (c) 2018-2020 Rossmann-Engineering
Permission is hereby granted, free of charge,
to any person obtaining a copy of this software
and associated documentation files (the "Software"),
to deal in the Software without restriction,
including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit
persons to whom the Software is furnished to do so,
subject to the following conditions:
The above copyright notice and this permission
notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package de.re.easymodbus.server;

import java.util.Calendar;

class ProcessReceivedDataThread extends Thread 
{
	short[] inBuffer;
	ModbusServer easyModbusTCPServer;
	java.net.Socket socket;
	
	public ProcessReceivedDataThread(byte[] inBuffer, ModbusServer easyModbusTCPServer, java.net.Socket socket)
	{
		this.socket = socket;
		this.inBuffer = new short[inBuffer.length];
		for (int i = 0; i < inBuffer.length ; i++)
		{

				this.inBuffer[i] = (short) ((short)inBuffer[i] & 0xff);
		}
		this.easyModbusTCPServer = easyModbusTCPServer;
	}
	
	public void run()
	{
		synchronized (easyModbusTCPServer)
		{
		       short[] wordData = new short[1];
               short[] byteData = new short[2];
               easyModbusTCPServer.receiveData = new ModbusProtocoll();
               easyModbusTCPServer.receiveData.timeStamp = Calendar.getInstance();
               easyModbusTCPServer.receiveData.request = true;

               //Lese Transaction identifier
               byteData[1] = inBuffer[0];
               byteData[0] = inBuffer[1];
               wordData[0] = (short) byteArrayToInt(byteData);
               easyModbusTCPServer.receiveData.transactionIdentifier = wordData[0];

               //Lese Protocol identifier
               byteData[1] = inBuffer[2];
               byteData[0] = inBuffer[3];
               wordData[0] = (short) byteArrayToInt(byteData);
               easyModbusTCPServer.receiveData.protocolIdentifier = wordData[0];

               //Lese length
               byteData[1] = inBuffer[4];
               byteData[0] = inBuffer[5];
               wordData[0] = (short) byteArrayToInt(byteData);
               easyModbusTCPServer.receiveData.length = wordData[0];

               //Lese unit identifier
               easyModbusTCPServer.receiveData.unitIdentifier = (byte) inBuffer[6];

               // Lese function code
               easyModbusTCPServer.receiveData.functionCode = (byte) inBuffer[7];

               // Lese starting address 
               byteData[1] = inBuffer[8];
               byteData[0] = inBuffer[9];
               wordData[0] = (short) byteArrayToInt(byteData);
               easyModbusTCPServer.receiveData.startingAdress = wordData[0];
               
               if (easyModbusTCPServer.receiveData.functionCode <= 4)
               {
                   // Lese quantity
                   byteData[1] = inBuffer[10];
                   byteData[0] = inBuffer[11];
                   wordData[0] = (short) byteArrayToInt(byteData);
                   easyModbusTCPServer.receiveData.quantity = wordData[0];
               }
               if (easyModbusTCPServer.receiveData.functionCode == 5)
               {
            	   easyModbusTCPServer.receiveData.receiveCoilValues = new short[1];
                   // Lese Value
                   byteData[1] = inBuffer[10];
                   byteData[0] = inBuffer[11];
                   easyModbusTCPServer.receiveData.receiveCoilValues[0] = (short) byteArrayToInt(byteData);
               }
               if (easyModbusTCPServer.receiveData.functionCode == 6)
               {
            	   easyModbusTCPServer.receiveData.receiveRegisterValues = new int[1];
                   // Lese Value
                   byteData[1] = inBuffer[10];
                   byteData[0] = inBuffer[11];
                   easyModbusTCPServer.receiveData.receiveRegisterValues[0] = byteArrayToInt(byteData);
               }
               if (easyModbusTCPServer.receiveData.functionCode == 15)
               {
                   // Lese quantity
                   byteData[1] = inBuffer[10];
                   byteData[0] = inBuffer[11];
                   wordData[0] = (short) byteArrayToInt(byteData);
                   easyModbusTCPServer.receiveData.quantity = wordData[0];

                   easyModbusTCPServer.receiveData.byteCount = (byte) inBuffer[12];

                   if ((easyModbusTCPServer.receiveData.byteCount % 2) != 0)
                	   easyModbusTCPServer.receiveData.receiveCoilValues = new short[easyModbusTCPServer.receiveData.byteCount / 2 + 1];
                   else
                	   easyModbusTCPServer.receiveData.receiveCoilValues = new short[easyModbusTCPServer.receiveData.byteCount / 2];
                   // Lese Value
                   for (int i = 0; i < easyModbusTCPServer.receiveData.byteCount; i++)
                   {
                	   if ((i%2) == 1)
                		   easyModbusTCPServer.receiveData.receiveCoilValues[i/2] =  (short) (easyModbusTCPServer.receiveData.receiveCoilValues[i/2] + 256*inBuffer[13+i]);
                	   else
                		   easyModbusTCPServer.receiveData.receiveCoilValues[i/2] = inBuffer[13+i];
                   }
               }
               if (easyModbusTCPServer.receiveData.functionCode == 16)
               {
                   // Lese quantity
                   byteData[1] = inBuffer[10];
                   byteData[0] = inBuffer[11];
                   wordData[0] = (short) byteArrayToInt(byteData);
                   easyModbusTCPServer.receiveData.quantity = wordData[0];

                   easyModbusTCPServer.receiveData.byteCount = (byte) inBuffer[12];
                   easyModbusTCPServer.receiveData.receiveRegisterValues = new int[easyModbusTCPServer.receiveData.quantity];
                   for (int i = 0; i < easyModbusTCPServer.receiveData.quantity; i++)
                   {
                       // Lese Value
                       byteData[1] = inBuffer[13+i*2];
                       byteData[0] = inBuffer[14+i*2];                       		
                       easyModbusTCPServer.receiveData.receiveRegisterValues[i] = byteData[0];                  	   
                       easyModbusTCPServer.receiveData.receiveRegisterValues[i] = (int) (easyModbusTCPServer.receiveData.receiveRegisterValues[i] + (byteData[1] <<8));                   	  
                   }                
               }
               easyModbusTCPServer.CreateAnswer(socket);
               easyModbusTCPServer.CreateLogData();
            }
	}
	
	public int byteArrayToInt(short[] byteArray)
	{
		int returnValue;
		returnValue = byteArray[0];
		returnValue = (int) (returnValue + 256*byteArray[1]);
		return returnValue;
	}
	
}
