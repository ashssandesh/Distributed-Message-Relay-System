package src.application;
import src.utils.NetworkOperations;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RelayServer {
    private NetworkOperations networkOperations;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private Socket relaySocket;
    private String loggedInUser;
    private Map<String, String> userPasswords = new HashMap<>(); 
    private Map<String, List<String>> receiverIPMapping = new HashMap<>(); 
    
    public RelayServer(NetworkOperations networkOperations) {
        this.networkOperations = networkOperations;
        this.loggedInUser = null;
        this.serverSocket = null;
        this.clientSocket = null;
        this.relaySocket = null;
    }

    public void startRelayCommunication(int port) {

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Relay server started. Waiting for clients...");

            clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket);

            // Send acknowledgement to client
            networkOperations.sendObject(new DataObject("Connection established with the relay server.", "Connection ACK"), clientSocket);

            System.out.println("Send");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startRelayReceiverCommunication(String receiverAddress, int receiverPort) {
        try {
            relaySocket = networkOperations.connect(receiverAddress, receiverPort);
            System.out.println("Connected to receiver server.");

            // Receive acknowledgement from relay server
            Object ackObject = networkOperations.receiveObject(relaySocket);

            if (ackObject instanceof DataObject) {
                DataObject ackData = (DataObject) ackObject;
                System.out.println("Received acknowledgement: " + ackData.getData());
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } 
    }

    public void receiveData() {
        try {
            while (true) {
                // Receive data from client
                Object dataObject = networkOperations.receiveObject(clientSocket);
                if (dataObject instanceof DataObject) {
                    DataObject data = (DataObject) dataObject;
                    String message = data.getData();
                    String type = data.getType();

                    System.out.println("Received data from client: " + message);
                    
                    if(!type.equalsIgnoreCase("close")) {
                        processData(message, type);
                    } 

                    if (type.equalsIgnoreCase("actualData") | (type.equalsIgnoreCase("close") & message.equalsIgnoreCase("Close"))) {
                        
                        //Send data to receiver server
                        networkOperations.sendObject(new DataObject(message, type), relaySocket);

                        //Receive acknowledgement from receiver server
                        Object ackObject = networkOperations.receiveObject(relaySocket);
                        if (ackObject instanceof DataObject) {
                            DataObject ackData = (DataObject) ackObject;
                            System.out.println("Received acknowledgement from receiver server: " + ackData.getData());

                            // Send acknowledgement back to client
                            networkOperations.sendObject(new DataObject(ackData.getData(), ackData.getType()), clientSocket);
                        }
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Communication Terminated...Distributed Network closed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processData(String data, String type) {
        if (type.equalsIgnoreCase("username")) {
            readUsersFile();
            verifyUser(data);
        } else if (type.equalsIgnoreCase("password")) {
            validateUser(data);
        } else if (type.equalsIgnoreCase("receiverServer")) {
            readReceiverFile();
            verifyReceiver(data);
        }
    }

    public void readUsersFile() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("../src/static/userList.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(" ");
                if (columns.length == 3) {
                    userPasswords.put(columns[0], columns[1]);
                } else {
                    System.out.println("Invaild line: " + line);
                }
            }
        } catch (IOException e) {
            System.out.print("Relay File Reader: " + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch(IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    public void verifyUser(String userName) {
        try {
            if(userPasswords.containsKey(userName)) {
                System.out.println("Valid");
                this.loggedInUser = userName;
                networkOperations.sendObject(new DataObject("Valid Username.", "Username"), clientSocket);
            } else {
                networkOperations.sendObject(new DataObject("InValid Username.", "Username"), clientSocket);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void validateUser(String data) {
        try {
            if(data.equalsIgnoreCase(userPasswords.get(this.loggedInUser))){
                System.out.println("Authenticated");
                networkOperations.sendObject(new DataObject("Authenticated.", "Password"), clientSocket);
            } else {
                System.out.println("Not Authenticated");
                networkOperations.sendObject(new DataObject("Not Authenticated.", "Password"), clientSocket);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }   
    }

    public void readReceiverFile() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("../src/static/receiverList.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(" ");
                if (columns.length == 3) {
                    List<String> addressAndPort = new ArrayList<>();
                    addressAndPort.add(columns[1]);
                    addressAndPort.add(columns[2]);
                    receiverIPMapping.put(columns[0], addressAndPort);
                } else {
                    System.out.println("Invaild line: " + line);
                }
            }
        } catch (IOException e) {
            System.out.print("Relay File Reader: " + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch(IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    public void verifyReceiver(String address) {
        List<String> addressAndPort = receiverIPMapping.get(address);
        if(receiverIPMapping.containsKey(address)){
            try {
                networkOperations.sendObject(new DataObject("Receiver Found.", "receiver"), clientSocket);
                startRelayReceiverCommunication(addressAndPort.get(0), Integer.parseInt(addressAndPort.get(1)));
            } catch(IOException e) {
                e.printStackTrace();
            }
            
        } else {
            try {
                networkOperations.sendObject(new DataObject("Reciever Not found!" + address + " , act:" + addressAndPort.get(0), "Password"), clientSocket);
            } catch(IOException e) {
                e.printStackTrace();
            }
            
        }
    }
}
