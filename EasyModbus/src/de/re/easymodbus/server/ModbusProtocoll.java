/*
 * 
 * Creative Commons license: Attribution-NoDerivatives 4.0 International (CC BY-ND 4.0)
 * You are free to:
 *
 *Share — copy and redistribute the material in any medium or format
 *for any purpose, even commercially.
 *The licensor cannot revoke these freedoms as long as you follow the license terms.
 *Under the following terms:
 *
 *Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 *NoDerivatives — If you remix, transform, or build upon the material, you may not distribute the modified material.
 *No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */
package de.re.easymodbus.server;

public class ModbusProtocoll 
{
    public java.util.Calendar timeStamp;
    public boolean request;
    public boolean response;
    public int transactionIdentifier;
    public int protocolIdentifier;
    public int length;
    public byte unitIdentifier;
    public byte functionCode;
    public int startingAdress;
    public int quantity;
    public short byteCount;
    public byte exceptionCode;
    public byte errorCode;
    public short[] receiveCoilValues;
    public int[] receiveRegisterValues;
    public int[] sendRegisterValues;
    public boolean[] sendCoilValues;
}

