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


class ClientConnectionThread extends Thread 
{
	private java.net.Socket socket;
	private byte[] inBuffer = new byte[1024];
	ModbusServer easyModbusTCPServer;
	
	public ClientConnectionThread(java.net.Socket socket, ModbusServer easyModbusTCPServer)
	{
		this.easyModbusTCPServer = easyModbusTCPServer;
		this.socket = socket;		
	}
	
	public void run()
	{
            this.easyModbusTCPServer.setNumberOfConnectedClients(this.easyModbusTCPServer.getNumberOfConnectedClients()+1);
            
            try
            {
                socket.setSoTimeout(easyModbusTCPServer.getClientConnectionTimeout());
                java.io.InputStream inputStream;                   
                inputStream = socket.getInputStream();
                while (socket.isConnected() & !socket.isClosed() & easyModbusTCPServer.getServerRunning())
		{
                	
                    int numberOfBytes=(inputStream.read(inBuffer));
                    if (numberOfBytes  > 4)
                    (new ProcessReceivedDataThread(inBuffer, easyModbusTCPServer, socket)).start();
                    Thread.sleep(5);
		}
                this.easyModbusTCPServer.setNumberOfConnectedClients(this.easyModbusTCPServer.getNumberOfConnectedClients()-1);
                socket.close();
            } catch (Exception e) 
            {
                this.easyModbusTCPServer.setNumberOfConnectedClients(this.easyModbusTCPServer.getNumberOfConnectedClients()-1);
                try
                {
                 socket.close();
                }
                catch (Exception exc)
                {}
                e.printStackTrace();
            }
	}
	
}
