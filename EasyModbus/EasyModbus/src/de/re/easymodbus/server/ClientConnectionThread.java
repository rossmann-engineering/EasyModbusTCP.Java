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
            System.out.println("Connected");
            try
            {
                while (socket.isConnected() & !socket.isClosed() & easyModbusTCPServer.getServerRunning())
		{
                    socket.setSoTimeout(10000);
                    java.io.InputStream inputStream;                   
                    inputStream = socket.getInputStream();
                    inputStream.read(inBuffer);
                    (new ProcessReceivedDataThread(inBuffer, easyModbusTCPServer, socket)).start();
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
