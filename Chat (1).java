/*
Comp 429 Project 1
Group 3
Spring 2021
Deniz Childir
Brandon Dahl
*/

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.InputMismatchException;
import java.util.Scanner;


// Chat class is the main class used to handle user commands
public class Chat {

    public static Hashtable<Integer, PeerInfo> table = new Hashtable<Integer, PeerInfo>();
    public static int index = 0;

    public static void main(String[] args)
    {
        // Get port as command line argument
        if (args.length == 0)
        {
            System.out.println("\nError: No arguments passed. Please pass a port number to use. \n");
            return;
        }
        int myPort = Integer.parseInt(args[0]);
        String myIpAddress;
        try
        {
            InetAddress ip;
            ip = InetAddress.getLocalHost();
            myIpAddress = ip.getHostAddress();
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
            return;
        }

        // Listen to own port for peer connections
        ListenThread listen = new ListenThread(myIpAddress, myPort);
        listen.start();

        Scanner scan = new Scanner(System.in);

        // Input loop
        while (true)
        {
            String input = (scan.next());
            if (input.equals("help"))
            {
                System.out.println("\n------------");
                System.out.println("| Commands |");
                System.out.println("------------");
                System.out.println("help\t\t\t\t\tDisplay information about the available user interface options or command manual.");
                System.out.println("myip\t\t\t\t\tDisplay the IP address of this process.");
                System.out.println("myport\t\t\t\t\tDisplay the port on which this process is listening for incoming connections.");
                System.out.println("connect <destination> <port no>\t\tThis command establishes a new TCP connection to the specified <destination> at the specified <port no>.");
                System.out.println("list\t\t\t\t\tDisplay a numbered list of all the connections this process is part of.");
                System.out.println("terminate <connection id>\t\tThis command will terminate the connection listed under the <connection id> number when \"list\" is used to display all connections.");
                System.out.println("send <connection id> <message>\t\tThis will send the message to the host on the connection that is designated by the <connection id> number when command \"list\" is used.");
                System.out.println("exit\t\t\t\t\tClose all connections and terminate this process.\n");
            }
            else if (input.equals("myip"))
            {
                System.out.println("\nIP Address: " + myIpAddress + "\n");
            }
            else if (input.equals("myport"))
            {
                System.out.println("\nPort: " + myPort + "\n");
            }
            else if (input.equals("connect"))
            {
                // Validate input
                try
                {
                    String peerIpAddress = scan.next();
                    int peerPort = scan.nextInt();

                    // Self-connection
                    if (peerIpAddress.equals(myIpAddress) && peerPort == myPort)
                    {
                        System.out.println("\nError: Self-connection is not allowed.\n");
                    }

                    // Duplicate connection
                    else if (DuplicateConnectionCheck(peerIpAddress, peerPort))
                    {
                        System.out.println("\nError: Connection already exists.\n");
                    }
                    else
                    {
                        // Attempt to create socket for client, failure indicates a bad ip address
                        try
                        {
                            Socket clientSocket = new Socket(peerIpAddress, peerPort);
                            new ClientThread(myIpAddress, myPort, clientSocket, "connect").start();

                            // Add entry to hashtable, will update with serverSocket after confirm received
                            table.put(Chat.index++, new PeerInfo(peerIpAddress, peerPort, clientSocket, null));
                        }
                        catch (UnknownHostException ex)
                        {
                            System.out.println("\nInvalid IP address: " + ex.getMessage() + "\n");
                        }
                        catch (IOException ex)
                        {
                            System.out.println("\nI/O error: " + ex.getMessage() + "\n");
                        }
                    }
                }
                // Validate input
                catch (InputMismatchException e)
                {
                    System.out.println("\nError: Invalid input, please ensure the Ip address and port are typed correctly.\n");
                }
            }
            else if (input.equals("list"))
            {
                System.out.println("\nID\tIP Address\tPort");
                table.forEach((key, peer)->
                {
                    System.out.print(key + "\t");
                    System.out.print(peer.ipAddress + "\t");
                    System.out.println(peer.port);
                });
                System.out.print("\n");
            }
            else if (input.equals("terminate"))
            {
                int id = scan.nextInt();

                // Ensure valid id was entered
                if (table.containsKey(id))
                {
                    PeerInfo peer = table.get(id);

                    // Send terminate to peer and close peer sockets
                    try
                    {
                        // Call join on clientThread to ensure it is done before sockets are closed
                        // Otherwise sockets could close before terminate message is sent to peer
                        ClientThread c = new ClientThread(myIpAddress, myPort, peer.clientSocket, "terminate");
                        c.start();
                        c.join();
                        peer.clientSocket.close();
                        peer.serverSocket.close();
                    }
                    catch (InterruptedException | IOException e)
                    {
                        e.printStackTrace();
                    }

                    // Remove entry from hashtable
                    table.entrySet().removeIf(entry ->
                            (entry.getValue().ipAddress.equals(peer.ipAddress) && entry.getValue().port == peer.port));

                    System.out.println("\nConnection terminated to " + peer.ipAddress + " on port " + peer.port + "\n");
                }
                else
                {
                    System.out.println("\nError: Invalid ID entered.\n");
                }
            }
            else if (input.equals("send"))
            {
                int id = scan.nextInt();
                String message = scan.nextLine();
                // Ensure message fits 100 character limit
                if (message.length() > 100)
                {
                    System.out.println("\nError: Message exceeds 100 characters, try a shorter message.\n");
                }
                else
                {
                    // Ensure valid id was entered
                    if (table.containsKey(id))
                    {
                        PeerInfo peer = table.get(id);
                        new ClientThread(myIpAddress, myPort, peer.clientSocket, "send", message).start();
                        System.out.println("\nMessage sent to " + id + "\n");
                    }
                    else
                    {
                        System.out.println("\nError: Invalid ID entered.\n");
                    }
                }
            }
            else if (input.equals("exit"))
            {
                // Send terminate to each peers on list and close peer sockets
                for (PeerInfo peer : table.values())
                {
                    try
                    {
                        // Call join on clientThread to ensure it is done before sockets are closed
                        // Otherwise sockets could close before terminate message is sent to peer
                        ClientThread c = new ClientThread(myIpAddress, myPort, peer.clientSocket, "terminate");
                        c.start();
                        c.join();
                        peer.clientSocket.close();
                        peer.serverSocket.close();
                    }
                    catch (InterruptedException | IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                // Close listenThread
                listen.exit();
                break;
            }
            else
            {
                System.out.println("\nError: Command not found. Type \"help\" to see available commands.\n");
            }
        }
    }

    private static boolean DuplicateConnectionCheck(String peerIpAddress, int peerPort)
    {
        for (PeerInfo peer : table.values())
        {
            if (peer.ipAddress.equals(peerIpAddress) && peer.port == peerPort)
            {
                return true;
            }
        }
        return false;
    }
}
