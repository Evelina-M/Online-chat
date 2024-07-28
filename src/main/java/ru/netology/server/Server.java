package ru.netology.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static final String DEFAULT_SETTINGS_FILE = "settings.txt";
    private static final int DEFAULT_PORT = 1234;
    private static final String EXIT_COMMAND = "/exit";

    public ServerSocket serverSocket;
    private ExecutorService executorService;
    private List<ClientHandler> clients;
    private File logFile;

    public Server() {
        try {
            // Считываем порт из файла настроек
            int port = DEFAULT_PORT;
            try (BufferedReader reader = new BufferedReader(new FileReader(DEFAULT_SETTINGS_FILE))) {
                port = Integer.parseInt(reader.readLine());
            } catch (IOException e) {
                System.err.println("Ошибка чтения файла настроек: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.err.println("Некорректный формат номера порта в файле настроек: " + e.getMessage());
            }

            // Создаем серверный сокет
            serverSocket = new ServerSocket(port);

            // Создаем список подключенных клиентов
            clients = new ArrayList<>();

            // Создаем пул потоков для обработки клиентов
            executorService = Executors.newCachedThreadPool();

            // Создаем файл журнала
            logFile = new File("Server.log");
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
        } catch (IOException e) {
            System.err.println("Ошибка создания сервера: " + e.getMessage());
        }
    }

    public void start() {
        System.out.println("Запуск сервера...");

        // Бесконечно ждем подключения новых клиентов
        while (true) {
            try {
                // Ожидаем подключения клиента
                Socket clientSocket = serverSocket.accept();

                // Создаем обработчик клиента для нового подключения
                ClientHandler clientHandler = new ClientHandler(clientSocket);

                // Добавляем обработчик клиента в список
                synchronized (clients) {
                    clients.add(clientHandler);
                }

                // Запускаем поток обработчика клиента в пуле потоков
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

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {

                // Считываем имя пользователя с клиента
                username = reader.readLine();

                // Отправляем приветственное сообщение клиенту
                writer.write("Добро пожаловать в чат, " + username + "!\n");
                writer.flush();

                // Читаем сообщения от клиента и рассылаем их всем клиентам
                while (true) {
                    String message = reader.readLine();
                    if (message == null) {
                        break;
                    }

                    // Записываем сообщение в журнал
                    try (BufferedWriter logWriter = new BufferedWriter(new FileWriter(logFile, true))) {
                        logWriter.write(String.format("[%s] %s: %s\n", new Date(), username, message));
                    } catch (IOException e) {
                        System.err.println("Ошибка записи сообщения в журнал: " + e.getMessage());
                    }

                    // Проверяем, не является ли сообщение командой выхода
                    if (message.equals(EXIT_COMMAND)) {
                        break;
                    }

                    // Рассылаем сообщение всем клиентам
                    broadcastMessage(String.format(message));
                }
            } catch (IOException e) {
                System.err.println("Ошибка обработки клиента: " + e.getMessage());
            } finally {
                // Удаляем обработчик клиента из списка
                clients.remove(this);

                // Рассылаем сообщение об отключении пользователя всем клиентам
                broadcastMessage("*** " + username + " покинул чат ***");

                // Закрываем сокет клиента
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Ошибка закрытия сокета клиента: " + e.getMessage());
                }
            }
        }

        private void broadcastMessage(String message) {
            // Синхронизируем список клиентов для обеспечения безопасного доступа в многопоточной среде
            synchronized (clients) {
                // Рассылаем сообщение всем подключенным клиентам
                for (ClientHandler client : clients) {
                    try {
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.clientSocket.getOutputStream()));
                        writer.write(message + "\n");
                        writer.flush();
                    } catch (IOException e) {
                        System.err.println("Ошибка отправки сообщения клиенту: " + e.getMessage());
                    }
                }
            }
        }

    }
}
