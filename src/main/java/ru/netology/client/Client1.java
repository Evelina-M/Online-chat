package ru.netology.client;

import java.io.*;
import java.net.*;
import java.util.Date;

public class Client1 {

    private static final String DEFAULT_SETTINGS_FILE = "settings.txt";
    private static final int DEFAULT_PORT = 1234;
    private static final String EXIT_COMMAND = "/exit";

    public Socket clientSocket;
    public String username;
    public File logFile;

    public Client1() {
        try {
            // Считываем имя пользователя с консоли
            System.out.print("Введите ваше имя: ");
            username = new BufferedReader(new InputStreamReader(System.in)).readLine();

            // Считываем порт из файла настроек
            int port = DEFAULT_PORT;
            try (BufferedReader reader = new BufferedReader(new FileReader(DEFAULT_SETTINGS_FILE))) {
                port = Integer.parseInt(reader.readLine());
            } catch (IOException e) {
                System.err.println("Ошибка чтения файла настроек: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.err.println("Некорректный формат номера порта в файле настроек: " + e.getMessage());
            }

            // Создаем имя файла журнала
            String logFileName = "user.log";

            // Создаем сокет клиента
            clientSocket = new Socket("localhost", port);

            // Создаем файл журнала
            logFile = new File(logFileName);
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
        } catch (IOException e) {
            System.err.println("Ошибка создания клиента: " + e.getMessage());
        }
    }

    public void start() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
             BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))) {

            // Отправляем имя пользователя на сервер
            writer.write(username + "\n");
            writer.flush();

            // Бесконечно читаем сообщения с сервера
            while (true) {
                String message = reader.readLine();
                if (message == null) {
                    break;
                }

                // Выводим сообщение на консоль
                System.out.println(message);

                // Записываем сообщение в журнал
                try (BufferedWriter logWriter = new BufferedWriter(new FileWriter(logFile, true))) {
                    logWriter.write(String.format("[%s] %s\n", new Date(), message));
                } catch (IOException e) {
                    System.err.println("Ошибка записи сообщения в журнал: " + e.getMessage());
                }

                // Читаем сообщение с консоли
                String inputMessage = consoleReader.readLine();
                if (inputMessage == null || inputMessage.equals(EXIT_COMMAND)) {
                    break;
                }

                // Отправляем сообщение на сервер
                writer.write(String.format("[%s] %s: %s\n", new Date(), username, inputMessage));
                writer.flush();
            }
        } catch (IOException e) {
            System.err.println("Ошибка обработки клиента: " + e.getMessage());
        } finally {
            // Закрываем сокет
            try {
                clientSocket.close();
            } catch (IOException e) {

                System.err.println("Ошибка закрытия сокета: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        Client1 client = new Client1();
        client.start();
    }

}

