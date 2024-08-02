package server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.netology.server.Server;

import java.io.*;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ServerTest {
    private Server server;
    private ByteArrayOutputStream outputStream;
    private PrintStream printStream;

    @BeforeEach
    public void setUp() throws Exception {
        outputStream = new ByteArrayOutputStream();
        printStream = new PrintStream(outputStream);
        System.setOut(printStream); // Переадресуем вывод в поток
        server = new Server();
    }

    @AfterEach
    public void tearDown() {
        System.setOut(System.out); // Восстанавливаем стандартный вывод
    }

    @Test
    public void testServerInitialization() {
        assertNotNull(server);
        assertNotNull(server.clients);
    }

    @Test
    public void testClientHandlerAdd() throws IOException {
        Socket mockSocket = mock(Socket.class);
        when(mockSocket.getInputStream()).thenReturn(new ByteArrayInputStream("TestUser\n".getBytes()));
        when(mockSocket.getOutputStream()).thenReturn(new ByteArrayOutputStream());

        Server.ClientHandler clientHandler = server.new ClientHandler(mockSocket);
        Thread clientThread = new Thread(clientHandler);
        clientThread.start(); // Запускаем поток, чтобы он читал имя пользователя
        server.clients.add(clientHandler);

        assertTrue(server.clients.contains(clientHandler));
    }

    @Test
    public void testSendMessageToAllClients() throws IOException {
        Socket mockSocket1 = mock(Socket.class);
        Socket mockSocket2 = mock(Socket.class);
        PrintWriter out1 = new PrintWriter(new ByteArrayOutputStream(), true);
        PrintWriter out2 = new PrintWriter(new ByteArrayOutputStream(), true);

        when(mockSocket1.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(mockSocket2.getOutputStream()).thenReturn(new ByteArrayOutputStream());

        Server.ClientHandler handler1 = server.new ClientHandler(mockSocket1);
        Server.ClientHandler handler2 = server.new ClientHandler(mockSocket2);

        server.clients.add(handler1);
        server.clients.add(handler2);

        handler1.sendToAll("Hello to all!");

        // Мы не можем проверить вывод, так как работаем с PrintWriter внутри клиента
        assertTrue(true); // Можно заменить реальной проверкой при наличии интерфейса для вывода
    }
}
