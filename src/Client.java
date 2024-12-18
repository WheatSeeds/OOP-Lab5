import java.io.*;
import java.net.*;

public class Client {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8030;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Подключено к серверу " + SERVER_HOST + ":" + SERVER_PORT);

            // Поток для чтения сообщений от сервера
            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    System.out.println("Соединение с сервером потеряно.");
                }
            }).start();

            // Отправка сообщений на сервер
            String userInput;
            while ((userInput = consoleInput.readLine()) != null) {
                out.println(userInput);
            }
        } catch (IOException e) {
            System.out.println("Ошибка клиента: " + e.getMessage());
        }
    }
}
