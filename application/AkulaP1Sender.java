package src.application;
import src.utils.NetworkOperations;

public class AkulaP1Sender {
    public static void main(String[] args) {
        NetworkOperations networkOperations = new NetworkOperations();
        Client client = new Client(networkOperations);

        String relayAddress = args[0];
        int relayPort = Integer.parseInt(args[1]); 


        Thread clientThread = new Thread(
            () -> {
                client.startClientCommunication(relayAddress, relayPort);
                try {
                    client.takeInput("username");
                    client.takeInput("password");
                    client.takeInput("receiverServer");
                    client.takeInput("actualData");
                    client.takeInput("close");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        );

        clientThread.start();
    }
}
