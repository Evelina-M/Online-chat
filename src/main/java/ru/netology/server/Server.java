package ru.netology.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final String DEFAULT_SETTINGS_FILE = "settings.txt";
    private static final int DEFAULT_PORT = 1234;
    private static final String EXIT_COMMAND = "/exit";

    protected ServerSocket serverSocket;
    private ExecutorService executorService;
    public Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();
    private File logFile;

    public Server() {
        setupServer();
    }

    private void setupServer() {
        try {
            // Считываем порт из файла настроек
            int port = DEFAULT_PORT;
            try (BufferedReader reader = new BufferedReader(new FileReader(DEFAULT_SETTINGS_FILE))) {
                port = Integer.parseInt(reader.readLine());
            } catch (IOException | NumberFormatException e) {
                System.err.println("Ошибка чтения файла настроек: " + e.getMessage());
            }

            serverSocket = new ServerSocket(port);
            executorService = Executors.newCachedThreadPool();
            logFile = new File("File.log");
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
        } catch (IOException e) {
            System.err.println("Ошибка создания сервера: " + e.getMessage());
        }
    }

    public void start() {
        System.out.println("Запуск сервера...");

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                executorService.submit(clientHandler);
            } catch (IOException e) {
                System.err.println("Ошибка при подключении клиента: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }

    public class ClientHandler implements Runnable {
        private Socket clientSocket;
        private String username;
        private PrintWriter out;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
            try {
                // Инициализируем выводной поток только тут, от входящего сокета
                this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (IOException e) {
                System.err.println("Ошибка инициализации потока вывода: " + e.getMessage());
            }
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                username = reader.readLine(); // Чтение имени пользователя

                sendToAll(username + " присоединился к чату");

                String message;
                while ((message = reader.readLine()) != null) {
                    if (message.equalsIgnoreCase(EXIT_COMMAND)) {
                        break;
                    }
                    logMessage(username, message);
                    sendToAll(username + ": " + message);
                }
            } catch (IOException e) {
                System.err.println("Ошибка в обработчике клиента: " + e.getMessage());
            } finally {

                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Ошибка закрытия сокета: " + e.getMessage());
                }
                clients.remove(this);
                sendToAll(username + " вышел из чата");
            }
        }

        public void sendToAll(String message) {
            for (ClientHandler client : clients) {
                if (client != this) {
                    client.out.println(message);
                }
            }
            logMessage("Server", message); // Логи от сервера
        }

        private void logMessage(String sender, String message) {
            try (BufferedWriter logWriter = new BufferedWriter(new FileWriter(logFile, true))) {
                logWriter.write(String.format("%s [%s]: %s%n", sender, new Date(), message));
            } catch (IOException e) {
                System.err.println("Ошибка записи в журнал: " + e.getMessage());
            }
        }
    }
}

