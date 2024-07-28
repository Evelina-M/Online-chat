package p3.Client;

import java.io.*;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;

public class Client {
    public static final String SETTINGS_FILENAME = "settings.txt";
    private int port;
    private String host;
    private final Logger log = Logger.getInstance();
    private final BufferedReader in;
    private final PrintWriter out;
    private String path;
    private Socket socket;
    private final Scanner scanner;

    public Client() {

        Logger log = Logger.getInstance();

        try (FileReader reader = new FileReader(SETTINGS_FILENAME)) {
            Properties properties = new Properties();
            properties.load(reader);
            port = Integer.parseInt(properties.getProperty("SERVER_PORT"));
            host = properties.getProperty("SERVER_HOST");
            path = properties.getProperty("CLIENT_LOG");
        } catch (IOException e) {
            log.log(e.getMessage(), path);
            e.printStackTrace(System.out);
        }

        try {
            this.socket = new Socket(host, port);
        } catch (IOException e) {
            e.printStackTrace(System.out);
            log.log(e.getMessage(), path);
        }

        try {

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            scanner = new Scanner(System.in);

            String msg;
            System.out.print("Введите Ваше имя: ");
            msg = scanner.nextLine();

            out.println("/name " + msg);
            String receivedMsg = "SERVER: " + in.readLine();
            System.out.println(receivedMsg);
            log.log(receivedMsg, path);

            new WriteChat().start();
            new ReadChat().start();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        new Client();
    }

    private class ReadChat extends Thread {

        @Override
        public void run() {
            String str;
            try {
                while (true) {
                    System.out.print("> ");
                    str = in.readLine();
                    if (str.contains("/exit")) {
                        System.out.println("Вы вышли из чата");
                        log.log(str, path);
                        break;
                    }
                    System.out.println(str);
                    log.log(str, path);
                }
            } catch (IOException e) {
                e.printStackTrace();
                log.log(e.getMessage(), path);
            }
        }
    }

    public class WriteChat extends Thread {

        @Override
        public void run() {
            while (true) {
                String userMsg;
                userMsg = scanner.nextLine();
                out.println(userMsg);
                out.flush();
                if (userMsg.equals("/exit")) {
                    break;
                }
            }
        }

    }
}
