package src.application;
import src.utils.NetworkOperations;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

public class Receiver {
    private NetworkOperations networkOperations;
    private ServerSocket serverSocket = null;
    private Socket relaySocket = null;

    public Receiver(NetworkOperations networkOperations) {
        this.networkOperations = networkOperations;
        this.serverSocket = null;
        this.relaySocket = null;
    }

    public void startReceiverCommunication(int receiverPort) {

        try {
            serverSocket = new ServerSocket(receiverPort);
            System.out.println("Receiver server started. Waiting for relay...");

            relaySocket = serverSocket.accept();
            System.out.println("Relay connected: " + relaySocket);

            networkOperations.sendObject(new DataObject("Connection established with the receiver server.", "Connection ACK"), relaySocket);

        } catch (IOException e) {
            e.printStackTrace();
        } 
    }

    public void receiveData() {
        try {
            while (true) {
                // Receive data from client
                Object dataObject = networkOperations.receiveObject(relaySocket);
                if (dataObject instanceof DataObject) {
                    DataObject data = (DataObject) dataObject;
                    String message = data.getData();
                    String type = data.getType();
                    System.out.println(message + type);
                    if (("close".equalsIgnoreCase(type)) & ("close".equalsIgnoreCase(message))){
                        System.out.println("Received Close Message");
                        networkOperations.sendObject(new DataObject("Closed the Receiver", "receiver"), relaySocket);
                        networkOperations.close(relaySocket);
                        break;
                    }

                    System.out.println("Received data from relay: " + message);
                    patternMatching(message);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void patternMatching(String data) {
        try {
            System.out.println("Run Pattern Matching Algorithm");
            networkOperations.sendObject(new DataObject(searchSubString(data), "search result"), relaySocket);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public String searchSubString(String str) {
        String searchResult = "";
        int n = str.length();

        int[][] dp = new int[n + 1][n + 1];
        int maxLength = 0;
        int endIndex = 0;

        for (int i = 1; i <= n; i++) {
            for (int j = i + 1; j <= n; j++) {
                if (str.charAt(j - 1) == str.charAt(i - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                    if (dp[i][j] > maxLength) {
                        maxLength = dp[i][j];
                        endIndex = j - 1;
                    }
                } else {
                    dp[i][j] = 0;
                }
            }
        }

        if (maxLength == 0) {
            searchResult =  "";  // No common substring found
        }

        searchResult = str.substring(endIndex - maxLength + 1, endIndex + 1);
        String[] words = searchResult.split(" ");
        
        // Create a HashSet to store unique words
        HashSet<String> uniqueWords = new HashSet<>();
        
        // Iterate over the words and add them to the HashSet
        for (String word : words) {
            uniqueWords.add(word);
        }
        // Concatenate the unique words into a single string
        StringBuilder result = new StringBuilder();
        for (String word : uniqueWords) {
            result.append(word).append(" ");
        }

        return result.toString();
    }
}
