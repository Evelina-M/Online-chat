package ru.netology.client;

import java.io.*;
import java.net.*;

public class Client {
    private static final String DEFAULT_SETTINGS_FILE = "settings.txt";

    private static final int DEFAULT_PORT = 1234;
    private static final String EXIT_COMMAND = "/exit";

    public Socket clientSocket;
    public String username;

    public Client() {
        try {
            // Считываем имя пользователя с консоли
            System.out.print("Введите ваше имя: ");
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            username = consoleReader.readLine();

            // Считываем порт из файла настроек
            int port = DEFAULT_PORT;
            try (BufferedReader reader = new BufferedReader(new FileReader(DEFAULT_SETTINGS_FILE))) {
                port = Integer.parseInt(reader.readLine());
            } catch (IOException e) {
                System.err.println("Ошибка чтения файла настроек: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.err.println("Некорректный формат номера порта в файле настроек: " + e.getMessage());
            }

            // Создаем сокет клиента
            clientSocket = new Socket("localhost", port);
            System.out.println("Подключено к серверу на порту " + port);

            // Отправляем имя пользователя на сервер
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            out.println(username);

            // Запускаем потоки
            new Thread(new ru.netology.client.Client.IncomingMessageHandler(clientSocket)).start();
            new Thread(new ru.netology.client.Client.OutgoingMessageHandler(out)).start();

        } catch (IOException e) {
            System.err.println("Ошибка создания клиента: " + e.getMessage());
        }
    }

    public class IncomingMessageHandler implements Runnable {
        private Socket socket;

        public IncomingMessageHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println(message);
                }
            } catch (IOException e) {
                System.err.println("Ошибка при обработке входящих сообщений: " + e.getMessage());
            }
        }
    }

    private class OutgoingMessageHandler implements Runnable {
        private PrintWriter out;

        public OutgoingMessageHandler(PrintWriter out) {
            this.out = out;
        }

        @Override
        public void run() {
            try (BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))) {
                String inputMessage;
                while (true) {
                    inputMessage = consoleReader.readLine();
                    if (inputMessage.equals(EXIT_COMMAND)) {
                        out.println(inputMessage);
                        break;
                    }
                    out.println(inputMessage);
                }
            } catch (IOException e) {
                System.err.println("Ошибка при отправке сообщений: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        new n.C();
    }
}
