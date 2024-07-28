package p3.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;


public class Server {

    public static ArrayList<Socket> clients = new ArrayList<>();

    public static void main(String[] args) {
        ServerWork.ConnectWithServer.Connect();
    }

}

class ServerWork implements Runnable {
    public static final String SETTINGS_FILENAME = "settings.txt";
    private static int port;
    private static String host;
    private static String path;
    private static final Logger log = Logger.getInstance();
    Socket client;

    public ServerWork(Socket socket) {
        this.client = socket;
    }

    @Override
    public void run() {

        String username = "";

        log.log("Сервер запущен. Port: " + port + " Host: " + host, path);
        System.out.println("Сервер запущен. Port: " + port + " Host: " + host);

        while (true) {
            try {
                PrintWriter out = new PrintWriter(this.client.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(this.client.getInputStream()));

                String line;
                while ((line = in.readLine()) != null) {
                    if (line.lastIndexOf("/name") == 0) {
                        username = line;
                        username = username.replace("/name", "").trim();
                        log.log(username + " зашёл(ла) в чат", path);
                        send("Привет, " + username + "!");
                        continue;
                    }

                    if (line.equals("/exit")) {
                        log.log(username + " покинул(а) чат", path);
                        send("/exit");
                        Server.clients.remove(client);
                        return;
                    }

                    send('[' + username + "] " + line);
                }

            } catch (IOException e) {
                log.log(e.getMessage(), path);
                e.printStackTrace(System.out);
            }
        }

    }

    private void send(String message) throws IOException {
        for (Socket client : Server.clients) {
            if (client.isClosed()) continue;
            PrintWriter sender = new PrintWriter(client.getOutputStream());
            sender.println(message);
            sender.flush();
        }
        log.log(message, path);
    }

    class ConnectWithServer {
        public static void Connect() {
            try (FileReader reader = new FileReader(SETTINGS_FILENAME)) {
                Properties properties = new Properties();
                properties.load(reader);
                port = Integer.parseInt(properties.getProperty("SERVER_PORT"));
                host = properties.getProperty("SERVER_HOST");
                path = properties.getProperty("SERVER_LOG");
            } catch (IOException e) {
                log.log(e.getMessage(), path);
                System.out.println(e.getMessage());
            }

            try (ServerSocket server = new ServerSocket(port)) {
                while (true) {
                    Socket client = server.accept();
                    Server.clients.add(client);
                    new Thread(new ServerWork(client)).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
                log.log(e.getMessage(), path);
            }
        }
    }
}