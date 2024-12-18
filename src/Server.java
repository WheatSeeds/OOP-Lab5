import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 8030;
    private static Map<String, ClientHandler> clients = new HashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Сервер запущен на порту " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Новое подключение: " + clientSocket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.out.println("Ошибка сервера: " + e.getMessage());
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private String clientName;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("Введите ваше имя:");
                clientName = in.readLine();
                synchronized (clients) {
                    clients.put(clientName, this);
                }
                System.out.println(clientName + " подключился");

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equals("/list")) {
                        sendClientList();
                    } else if (message.startsWith("@")) {
                        String[] parts = message.split(" ", 2);
                        if (parts.length == 2) {
                            String recipientName = parts[0].substring(1);
                            String msg = parts[1];

                            sendToClient(recipientName, clientName + ": " + msg);
                        } else {
                            out.println("Неверный формат сообщения. Используйте: @имя сообщение");
                        }
                    } else {
                        out.println("Сообщения должны начинаться с @имя или быть командой /list.");
                    }
                }
            } catch (IOException e) {
                System.out.println("Ошибка клиента: " + e.getMessage());
            } finally {
                disconnect();
            }
        }

        private void sendClientList() {
            synchronized (clients) {
                out.println("Клиенты в чате:");
                for (String name : clients.keySet()) {
                    if (!name.equals(clientName)) { // Исключаем отправителя
                        out.println("- " + name);
                    }
                }
            }
        }

        private void sendToClient(String recipientName, String message) {
            ClientHandler recipient;
            synchronized (clients) {
                recipient = clients.get(recipientName);
            }
            if (recipient != null) {
                recipient.out.println(message);
            } else {
                out.println("Клиент " + recipientName + " не найден.");
            }
        }

        private void disconnect() {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Ошибка закрытия сокета: " + e.getMessage());
            }
            synchronized (clients) {
                clients.remove(clientName);
            }
            System.out.println(clientName + " отключился.");
        }
    }
}
