package src.utils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class NetworkOperations {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    public Socket connect(String address, int port) throws IOException {
        socket = new Socket(address, port);
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream(), true);
        return socket;
    }

    public void sendObject(Object object, Socket socket) throws IOException {
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectOutputStream.writeObject(object);
        objectOutputStream.flush();
    }


    public Object receiveObject(Socket socket) throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
        return objectInputStream.readObject();
    }

    public void close(Socket closingSocket) throws IOException {
        if (input != null)
            input.close();
        if (output != null)
            output.close();
        if (closingSocket != null)
            closingSocket.close();
    }
}
