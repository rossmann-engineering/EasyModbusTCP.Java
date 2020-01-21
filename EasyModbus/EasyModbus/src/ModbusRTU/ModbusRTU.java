/*
 * Creative Commons license: Attribution-NonCommercial-NoDerivatives 4.0 International (CC BY-NC-ND 4.0)
 *You are free to:
 *
 *Share - copy and redistribute the material in any medium or format
 *The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 *Under the following terms:
 *
 *Attribution - You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 *NonCommercial - You may not use the material for commercial purposes.
 *NoDerivatives - If you remix, transform, or build upon the material, you may not distribute the modified material.
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
