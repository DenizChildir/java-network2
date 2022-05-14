/*
Comp 429 Project 1
Group 3
Spring 2021
Deniz Childir
Brandon Dahl
*/

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

// Server thread that is assigned one socket to listen for client data
// Will be closed when we are done using the socket
public class ServerThread extends Thread {

    private final String myIpAddress;
    private final int myPort;
    private final Socket serverSocket;

    public ServerThread(String ip, int p, Socket s)
    {
        myIpAddress = ip;
        myPort = p;
        serverSocket = s;
    }

    public void run()
    {
        try
        {
            while (true)
            {
                try
                {
                    // Wait here for new data from client
                    InputStream input = serverSocket.getInputStream();
                    InputStreamReader read = new InputStreamReader(input);
                    BufferedReader reader = new BufferedReader(read);

                    String peerIpAddress = reader.readLine();
                    int peerPort = Integer.parseInt(reader.readLine());
                    String clientType = reader.readLine();

                    if (clientType.equals("connect"))
                    {
                        // Add entry to hashtable
                        Socket clientSocket = new Socket(peerIpAddress, peerPort);
                        Chat.table.put(Chat.index++, new PeerInfo(peerIpAddress, peerPort, clientSocket, serverSocket));

                        // Send confirmation to peer that connection is successful
                        new ClientThread(myIpAddress, myPort, clientSocket, "confirm").start();
                        System.out.println("\nSuccessful connection to " + peerIpAddress + " on port " + peerPort + "\n");
                    }
                    else if (clientType.equals("confirm"))
                    {
                        // Update hashtable with serverSocket
                        for (PeerInfo peer : Chat.table.values())
                        {
                            if (peer.ipAddress.equals(peerIpAddress) && peer.port == peerPort)
                            {
                                peer.serverSocket = serverSocket;
                            }
                        }
                        System.out.println("\nSuccessful connection to " + peerIpAddress + " on port " + peerPort + "\n");
                    }
                    else if (clientType.equals("send"))
                    {
                        // Output client message to console
                        String message = reader.readLine();
                        System.out.println("\nMessage received from " + peerIpAddress);
                        System.out.println("Sender's Port: " + peerPort);
                        System.out.println("Message: " + message + "\n");
                    }
                    else if (clientType.equals("terminate"))
                    {
                        // Close server and client sockets
                        for (PeerInfo peer : Chat.table.values())
                        {
                            try
                            {
                                peer.clientSocket.close();
                                peer.serverSocket.close();
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        }

                        // Remove entry from hashtable
                        Chat.table.entrySet().removeIf(entry ->
                                (entry.getValue().ipAddress.equals(peerIpAddress) && entry.getValue().port == peerPort));

                        System.out.println("\nConnection terminated to " + peerIpAddress + " on port " + peerPort + "\n");
                    }
                    else
                    {
                        System.out.println("\nUnknown type received from " + peerIpAddress + " with port " + peerPort);
                        System.out.println("type = " + clientType + "\n");
                    }
                }
                // Socket/NumberFormat Exception is only raised when socket is closed and we intend to exit the server thread
                catch (SocketException | NumberFormatException e)
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
}
