package src.application;
import src.utils.NetworkOperations;

public class AkulaP1Receiver {
    public static void main(String[] args) {
        NetworkOperations networkOperations = new NetworkOperations();
        Receiver receiver = new Receiver(networkOperations);

        int relayPort = Integer.parseInt(args[0]); 

        Thread receiverServerThread = new Thread(
            () -> {
                receiver.startReceiverCommunication(relayPort);
                receiver.receiveData();
            }
        );

        receiverServerThread.start();
    }
}
