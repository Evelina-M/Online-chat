package qw;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

public class Client1 {
    private BufferedWriter out;
    private BufferedReader in;

    private Socket clientSocket;
    private BufferedReader user;
    private String userNick;


    public Client1() throws IOException {
        String url = "settings.txt";  // Запуск клиента через файл
        File settings = new File(url);
        Scanner scanner = new Scanner(settings);
        String hostSettings = scanner.nextLine();
        String[] hostSocket = hostSettings.split(":");
        final String HOST = hostSocket[0];
        final int PORT = Integer.parseInt(hostSocket[0]);
        try {
            clientSocket = new Socket(HOST, PORT);            // Подключения к серверу
        } catch (IOException e) {
            System.out.println("Ошибка сокет не создан!");
        }

        try {
            user = new BufferedReader(new InputStreamReader(System.in));                    // Ник клиентов
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));   // Переменая принимает сообщения
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));  // Переменая отправляет сообщения
            userNick = this.nickUser();
            new ReadChat().start();
            new WriteChat().start();
        } catch (IOException e) {
            endSocket();
        }
    }

    public static void main(String[] args) throws IOException {
        new Client1();

    }

    public String nickUser() {                      // Сохроняет и отправляет клиента на сервер
        System.out.println("Ведите вааш ник !");
        try {
            userNick = user.readLine();
            out.write("Добро пожаловать " + userNick + " ! " + "\n");
            out.flush();
        } catch (IOException ignored) {

        }
        return userNick;
    }

    private void endSocket() {                  // Отключает клента
        try {
            if (!clientSocket.isClosed()) {
                clientSocket.close();
                in.close();
                out.close();
            }
        } catch (IOException ignored) {
        }
        clientSocket.isClosed();
    }

    public class ReadChat extends Thread {  // Клиент принимает сообщения
        @Override
        public void run() {
            String msgRead;
            try {
                while (true) {
                    msgRead = in.readLine();
                    if (msgRead.equals("/exit")) {
                        endSocket();
                        break;
                    }
                    logServer(msgRead);
                    System.out.println(msgRead);
                    System.out.print(userNick + "->");
                }
            } catch (IOException e) {
                endSocket();
            }
        }
    }

    public class WriteChat extends Thread {  // Клиент отправляет сообщения
        @Override
        public void run() {
            while (true) {
                String msgWrite;
                try {
                    msgWrite = user.readLine();
                    if (msgWrite.equals("/exit")) {
                        out.write("/exit" + "\n");
                        endSocket();
                        break;
                    } else {
                        out.write(new SimpleDateFormat("dd.MM.yyyy HH.mm.ss ").format(Calendar.getInstance().getTime())
                                + userNick + " : " + msgWrite + "\n");
                    }
                    out.flush();
                } catch (Exception e) {
                    endSocket();
                }

            }
        }
    }

    public static void logServer(String log) throws IOException {
        FileWriter logs = new FileWriter("Client1.log", true);
        logs.append(new SimpleDateFormat("dd.MM.yyyy HH.mm.ss ").format(Calendar.getInstance().getTime()))
                .append(" ")
                .append(log)
                .append("\n")
                .flush();
    }
}
