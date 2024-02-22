package src.application;
import src.utils.NetworkOperations;

public class AkulaP1RelayServer {
    public static void main(String[] args) {
        NetworkOperations networkOperations = new NetworkOperations();
        RelayServer relayServer = new RelayServer(networkOperations);

        int relayPort = Integer.parseInt(args[0]); 

        Thread relayServerThread = new Thread(
            () -> {
                relayServer.startRelayCommunication(relayPort);
                relayServer.receiveData();
            }
        );

        relayServerThread.start();
    }
}
