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
package ModbusRTU;
import java.io.IOException;

import de.re.easymodbus.modbusclient.ModbusClient;

/**
 *
 * @author SR555
 */
public class ModbusRTU 
{
     public static void main(String args[]) throws IOException 
     {
        //System.loadLibrary("RXTXcomm.jar");
         ModbusClient modbusClient = new ModbusClient();
         modbusClient.setSerialFlag(true);
         try
         {
         modbusClient.Connect("COM3");
         modbusClient.setUnitIdentifier((byte)1);
         boolean[] response = modbusClient.ReadCoils(2, 20);
         int[] responseint = modbusClient.ReadHoldingRegisters(0, 20);
         modbusClient.WriteSingleCoil(0, true);
         modbusClient.WriteSingleRegister(200, 456);
         modbusClient.WriteMultipleCoils(200, new boolean[]{true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true});
         modbusClient.WriteMultipleRegisters(300, new int[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15});
         modbusClient.ReadWriteMultipleRegisters(0, 10, 200, new int[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15});
         for (int i = 0; i < response.length; i++)
         {
        	 System.out.println(response[i]);
          	System.out.println(responseint[i]); 
         }
             
         }
         catch (Exception e){
         e.printStackTrace();
         }
         finally
         {
        	 modbusClient.Disconnect();
         }
         
     }
}
