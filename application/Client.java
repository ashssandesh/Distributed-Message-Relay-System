package src.application;
import src.utils.NetworkOperations;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client {
    private NetworkOperations networkOperations;
    private Socket clientSocket;

    public Client(NetworkOperations networkOperations) {
        this.networkOperations = networkOperations;
    }

    public void startClientCommunication(String relayAddress, int relayPort) {
        try {
            clientSocket = networkOperations.connect(relayAddress, relayPort);
            System.out.println("Connected to relay server.");

            // Receive acknowledgement from relay server
            Object ackObject = networkOperations.receiveObject(clientSocket);

            if (ackObject instanceof DataObject) {
                DataObject ackData = (DataObject) ackObject;
                System.out.println("Received acknowledgement: " + ackData.getData());
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void takeInput(String placeholder) throws ClassNotFoundException  {
        try {
            // take the input from the user
            BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
            String message;
            while (true) {
                if(placeholder.equals("close")) {
                    System.out.print("Closing the application. Please type Close: ");
                } else {
                    System.out.print("Enter your " + placeholder + " : ");
                }
                message = consoleInput.readLine();
                String response = null;
                String responseType = null;
                // Send input to relay server
                networkOperations.sendObject(new DataObject(message, placeholder), clientSocket);

                // Receive acknowledgement from relay server
                Object responseObject = networkOperations.receiveObject(clientSocket);
                if (responseObject instanceof DataObject) {
                    DataObject responseData = (DataObject) responseObject;
                    response = responseData.getData();
                    responseType = responseData.getType();
                    if (response.equals("Valid Username.") | 
                        response.equals("Authenticated.") | 
                        response.equals("Receiver Found.") |
                        responseType.equals("search result") |
                        response.equals("Closed the Receiver")) {
                        // Input processing successful, move to the next iteration
                        //break;
                        
                        if (responseType.equals("search result") ) {
                            System.out.println("Longest Common Substring: "+ response);
                        }
                        if(response.equals("Closed the Receiver")) {
                            System.out.println("Closing Client");
                            networkOperations.close(clientSocket);
                            break;
                        } else {
                            break;
                        }
                    } else {
                        System.out.println("Input processing failed. Please try again.");
                    }
                    System.out.println("Received acknowledgement: " + responseData.getData());

                }
            }
        } 
        catch (IOException e) {
            System.out.println("Data Input Failed - Sender. "+ e.getMessage());
        }
    }
}