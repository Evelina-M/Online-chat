package client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.netology.client.Client;

import java.io.*;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ClientTest {
    private ByteArrayInputStream in;
    private ByteArrayOutputStream out;
    private PrintStream printStream;
    private Client client = new Client();

    @BeforeEach
    public void setUp() {
        String input = "TestUser\n"; // Ввод имени пользователя
        in = new ByteArrayInputStream(input.getBytes());
        out = new ByteArrayOutputStream();
        printStream = new PrintStream(out);
    }

    @Test
    public void testClientInitialization() throws Exception {
        assertEquals("TestUser", client.getClass());
    }

    @Test
    public void testSocketConnection() throws Exception {
        // Имитация сокета
        Socket mockSocket = mock(Socket.class);
        when(mockSocket.getInputStream()).thenReturn(new ByteArrayInputStream("Привет от сервера\n".getBytes()));
        when(mockSocket.getOutputStream()).thenReturn(new ByteArrayOutputStream());

        // Создание клиента
        assertNotNull(client.getClass());
    }

    @Test
    public void testSocketOutput() throws IOException {
        // Имитация сокета
        Socket mockSocket = mock(Socket.class);
        when(mockSocket.getOutputStream()).thenReturn(new ByteArrayOutputStream());

        // отсылаем сообщение на сервер
        PrintWriter outMock = new PrintWriter(mockSocket.getOutputStream(), true);
        outMock.println("Hello, World!");

        // Проверка вывода
        assertNotNull(outMock);
    }

    @Test
    public void testHandleIncomingMessage() throws Exception {
        String message = "Сообщение от сервера\n";
        Socket mockSocket = mock(Socket.class);
        when(mockSocket.getInputStream()).thenReturn(new ByteArrayInputStream(message.getBytes()));

        // Создаем клиент
        InputStream response = mockSocket.getInputStream();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(response))) {
            String receivedMessage = reader.readLine();
            assertEquals("Сообщение от сервера", receivedMessage);
        }

        // Проверяем вывод
        assertTrue(out.toString().contains(message.trim()));
    }
}
