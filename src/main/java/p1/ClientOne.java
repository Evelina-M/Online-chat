package p1;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class ClientOne {
    private static final String SETTINGS_FILE = "settings.txt";
    private static final String DEFAULT_PORT = "1234";
    private static final String EXIT_COMMAND = "/exit";

    private String username;
    private Socket clientSocket;
    private File logFile;

    public ClientOne() {
        try {
            // Считываем имя пользователя с консоли
            System.out.print("Введите ваше имя: ");
            username = new BufferedReader(new InputStreamReader(System.in)).readLine();

            // Считываем номер порта из файла настроек
            int port = Integer.parseInt(DEFAULT_PORT);
            try (BufferedReader reader = new BufferedReader(new FileReader(SETTINGS_FILE))) {
                port = Integer.parseInt(reader.readLine());
            } catch (IOException e) {
                System.err.println("Ошибка чтения файла настроек: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.err.println("Некорректный формат номера порта в файле настроек: " + e.getMessage());
            }

            // Создаем сокет клиента
            clientSocket = new Socket("localhost", port);

            // Создаем файл журнала
            logFile = new File(username + ".log");
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
        } catch (IOException e) {
            System.err.println("Ошибка создания клиента: " + e.getMessage());
        }
    }

    public void start() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {

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
                // Проверяем, не является ли сообщение командой выхода
                if (message.equals(EXIT_COMMAND)) {
                    break;
                }
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
        ClientOne client = new ClientOne();
        client.start();
    }
}

