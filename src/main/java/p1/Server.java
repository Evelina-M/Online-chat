package p1;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

public class Server {

    private static final String SETTINGS_FILE = "settings.txt";
    private static final int DEFAULT_PORT = 1234;
    private static final String EXIT_COMMAND = "/exit";

    private ServerSocket serverSocket;
    private List<ClientHandler> clients;
    private File logFile;

    public Server() {
        try {
            // Считываем порт из файла настроек
            int port = DEFAULT_PORT;
            try (BufferedReader reader = new BufferedReader(new FileReader(SETTINGS_FILE))) {
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

        while (true) {
            try {
                // Ожидаем подключения клиента
                Socket clientSocket = serverSocket.accept();

                // Создаем обработчик клиента для нового подключения
                ClientHandler clientHandler = new ClientHandler(clientSocket);

                // Добавляем обработчик клиента в список
//****                clients.add(clientHandler);

                // Запускаем поток обработчика клиента
//****                new Thread(clientHandler).start();
            } catch (IOException e) {
                System.err.println("Ошибка при подключении клиента: " + e.getMessage());
            }
        }
    }

    public void broadcastMessage(String message) {
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

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start();

//        logServer("Запуск сервера");
//        System.out.println("Запуск сервера");
//        String url = "settings.txt";    // Запуск сервера через файл
//        File settings = new File(url);
//        Scanner scanner1 = new Scanner(settings);
//        String hostSettings = scanner1.nextLine();
//        String[] hostSocket = hostSettings.split(":");
//        final int PORT = Integer.parseInt(hostSocket[0]);
//
//        try (ServerSocket server = new ServerSocket(PORT)) {
//            while (true) {
//                Socket socket = server.accept();
//                try {
//                    //.add(new qw.Server(socket));
//                    logServer("Подключился клиент : " + socket);
//                    System.out.println("Подключился клиент : " + socket);
//                } catch (IOException e) {
//                    socket.close();
//                }
//            }
//        }
    }

    private class ClientHandler implements Runnable {

        private Socket clientSocket;
        private String username;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;

        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {

                // Получаем имя пользователя
                username = reader.readLine();

                // Отправляем приветственное сообщение
                writer.write("Добро пожаловать в чат, " + username + "!\n");
                writer.flush();

                broadcastMessage("*** " + username + " присоединился к чату ***");

                // Бесконечно читаем сообщения от клиента
                while (true) {
                    String message = reader.readLine();
                    if (message == null) {
                        break;
                    }
//                    // Создаем файл журнала
//                    logFile = new File("server.log");
//                    if (!logFile.exists()) {
//                        logFile.createNewFile();
//                    }
//
//                    try (BufferedWriter logWriter = new BufferedWriter(new FileWriter(logFile, true))) {
//                        logWriter.write(String.format("[%s] %s: %s\n", new Date(), username, message));
//                    } catch (IOException e) {
//                        System.err.println("Ошибка записи сообщения в журнал: " + e.getMessage());
//                    }
                    if (message.equals(EXIT_COMMAND)) {
                        break;
                    }
                    // Рассылаем сообщение всем клиентам
                    broadcastMessage(String.format("[%s] %s: %s", new Date(), username, message));
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

    }

    public static void logServer(String log) throws IOException {
        FileWriter logs = new FileWriter("Server.log", true);
        logs.append(new SimpleDateFormat("dd.MM.yyyy HH.mm.ss ").format(Calendar.getInstance().getTime()))
                .append(" ")
                .append(log)
                .append("\n")
                .flush();
    }
}