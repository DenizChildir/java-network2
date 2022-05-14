/*
Comp 429 Project 1
Group 3
Spring 2021
Deniz Childir
Brandon Dahl
*/

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

// Thread dedicated to listening for new connections to port
// Will be closed when exiting the program
public class ListenThread extends Thread {

    private final String myIpAddress;
    private final int myPort;
    private boolean running = true;
    private ServerSocket serverSocket;

    public ListenThread(String myIp, int myP)
    {
        myIpAddress = myIp;
        myPort = myP;
    }

    public void run()
    {
        try
        {
            serverSocket = new ServerSocket(myPort);
            while (running)
            {
                try
                {
                    Socket socket = serverSocket.accept();
                    new ServerThread(myIpAddress, myPort, socket).start();
                }
                // Socket Exception is only raised when socket is closed and we intend to exit the listen thread
                catch (SocketException e)
                {
                    return;
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    // Called on terminate/exit, will end thread execution
    // Currently still gets stuck in while loop because of wait on socket accept()
    public void exit()
    {
        running = false;
        try
        {
            serverSocket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
