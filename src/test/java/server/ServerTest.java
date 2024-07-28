package server;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.netology.server.Server;

import java.io.*;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class ServerTest {

    @Test
    void shouldCreateServerSuccessfully() {
        Server server = new Server();

        // Проверяем, что объект сервера создан
        assertNotNull(server);

        // Проверяем, что серверный сокет открыт
        assertTrue(server.serverSocket.isBound());

        // Проверяем, что файл журнала создан
        File logFile = new File("Server.log");
        assertTrue(logFile.exists());
    }

    @TempDir
    File tempDir;

    @Test
    void shouldHandleSettingsFileReadError() throws IOException {
        // Создаем временный файл и записываем в него некорректные данные
        File settingsFile = new File(tempDir, "settings.txt");
        Files.writeString(settingsFile.toPath(), "abc");

        Server server = new Server();

        // Проверяем, что серверный сокет открыт на порту по умолчанию
        assertEquals(1234, server.serverSocket.getLocalPort());
    }

    @Test
    void shouldHandleLogFileCreationError() throws IOException {
        // Делаем временный каталог недоступным для записи
        tempDir.setWritable(false);

        Server server = new Server();

        // Проверяем, что создание файла журнала приводит к ошибке
        assertThrows(IOException.class, () -> server.start());
    }


}




