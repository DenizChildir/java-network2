/*
Comp 429 Project 1
Group 3
Spring 2021
Deniz Childir
Brandon Dahl
*/

import java.net.Socket;

// PeerInfo holds the IP address and port of the peer we are connected to
// Also holds the client and server sockets that are used to communicate to the peer
public class PeerInfo
{
    public String ipAddress;
    public int port;
    public Socket clientSocket;
    public Socket serverSocket;

    public PeerInfo(String ip, int p, Socket clientS, Socket serverS)
    {
        ipAddress = ip;
        port = p;
        clientSocket = clientS;
        serverSocket = serverS;
    }
}
