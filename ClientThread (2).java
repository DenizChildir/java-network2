/*
Comp 429 Project 1
Group 3
Spring 2021
Deniz Childir
Brandon Dahl
*/

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

// Client Thread is used to send data to another peer
// Each data transfer will create a new thread
public class ClientThread extends Thread
{

    private final String myIpAddress;
    private final int myPort;
    private final Socket clientSocket;
    private final String clientType;
    private String message;

    public ClientThread(String myIp, int myP, Socket clientS, String type)
    {
        myIpAddress = myIp;
        myPort = myP;
        clientSocket = clientS;
        clientType = type;
    }

    // Overloaded method to handle messages
    public ClientThread(String myIp, int myP, Socket clientS, String type, String msg)
    {
        myIpAddress = myIp;
        myPort = myP;
        clientSocket = clientS;
        clientType = type;
        message = msg;
    }

    public void run()
    {
        try {
            OutputStream output = clientSocket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            writer.println(myIpAddress);
            writer.println(myPort);
            writer.println(clientType);
            if (message != null) {
                writer.println(message);
            }
        }
        catch (UnknownHostException ex)
        {
            System.out.println("\nServer not found: " + ex.getMessage() + "\n");
        }
        // IOException will occur when socket is closed
        catch (IOException ignored)
        {

        }
    }

}
