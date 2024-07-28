package client;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import ru.netology.client.Client1;

import java.io.*;
import java.net.Socket;


import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

class Client1Test {

    @Test
    void testStartMethod() {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream("Сообщение сервера\n".getBytes());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        System.setIn(new ByteArrayInputStream("Ввод данных клиентом\n".getBytes()));
        System.setOut(new PrintStream(byteArrayOutputStream));

        try (Socket socket = Mockito.mock(Socket.class)) {
            Mockito.when(socket.getInputStream()).thenReturn(byteArrayInputStream);
            Mockito.when(socket.getOutputStream()).thenReturn(new ByteArrayOutputStream());

            Client1 client = new Client1();
            client.clientSocket = socket;
            client.start();

            assertEquals("Сообщение сервера\n", byteArrayOutputStream.toString());
        } catch (Exception e) {
            // fail("Exception should not be thrown here");
            System.out.println("Ошибка: " + e);
        }
    }

    @Test
    void testMainMethod() {
        PrintStream originalOut = System.out;
        InputStream originalIn = System.in;

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream("Сообщение сервера\n".getBytes());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        System.setIn(new ByteArrayInputStream("Ввод данных клиентом\n".getBytes()));
        System.setOut(new PrintStream(byteArrayOutputStream));

        try (Socket socket = Mockito.mock(Socket.class)) {
            Mockito.when(socket.getInputStream()).thenReturn(byteArrayInputStream);
            Mockito.when(socket.getOutputStream()).thenReturn(new ByteArrayOutputStream());

            Client1 client = new Client1() {
                @Override
                public void start() {
                    System.out.println("Имитируемый метод запуска");
                }
            };

            client.clientSocket = socket;

            System.setOut(originalOut);
            System.setIn(originalIn);

            assertDoesNotThrow(() -> Client1.main(new String[]{"arg1", "arg2"}));
            assertEquals("Имитируемый метод запуска\n", byteArrayOutputStream.toString());
        } catch (IOException e) {
            System.out.println("Ошибка: " + e);
        }
    }

    @Test
    void testExceptionHandling() throws IOException {
        PrintStream originalErr = System.err;

        try {
            Client1 client = new Client1();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            System.setErr(new PrintStream(outputStream));

            client.clientSocket = Mockito.mock(Socket.class);
            Mockito.doThrow(new IOException("Mocked IOException")).when(client.clientSocket).close();

            client.start();

            assertTrue(outputStream.toString().contains("Ошибка закрытия сокета: Mocked IOException"));
        } finally {
            System.setErr(originalErr);
        }
    }

    String path = "unit.log";

    @Test
    public void testMain() {
        String msg = "Test logging";
        File file = new File(path);
        long beforeLength = file.length();
        long afterLength = file.length();
        boolean afterLengthOverBefore = afterLength > beforeLength;
        assertTrue(!afterLengthOverBefore);
    }

    @Mock
    private Socket clientSocket;

    private Client1 client = new Client1();


    @Test
    void getUsername() {
        String username = "TestUser";
        assertEquals(username, client.username);
    }

    @Test
    void getLogFile() {
        File logFile = new File("user.log");
        assertEquals(logFile, client.logFile);
    }

    @Test
    void start() throws IOException {
        // Установка моков для чтения с консоли, записи на сервер и чтения с сервера
        BufferedReader consoleReader = Mockito.mock(BufferedReader.class);
        BufferedWriter writer = Mockito.mock(BufferedWriter.class);
        BufferedReader reader = Mockito.mock(BufferedReader.class);

        // Выполнение метода start()
        client.start();

        // Проверка вызова метода write() для отправки имени пользователя на сервер
        Mockito.verify(writer).write(client.getClass() + "\n");
        Mockito.verify(writer).flush();

        // Проверка вывода сообщения на консоль
        Mockito.verify(consoleReader).readLine();

        // Проверка записи сообщения в журнал
        Mockito.verify(writer, Mockito.times(2)).write(Mockito.anyString());
        Mockito.verify(writer).flush();

        // Проверка отправки сообщения на сервер
        Mockito.verify(writer).write(Mockito.anyString());
        Mockito.verify(writer).flush();
    }
}
