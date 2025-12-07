package com.lab11.server;

import com.lab11.common.Message;
import com.lab11.common.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.Set;

public class ClientHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private static final Logger errorLogger = LoggerFactory.getLogger("ERROR_LOGGER");

    private Socket socket;
    private ChatServer server;
    private PrintWriter out;
    private BufferedReader in;
    private String nickname;
    private boolean running;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
        this.running = true;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Получаем никнейм
            nickname = in.readLine();
            if (nickname == null || nickname.trim().isEmpty()) {
                out.println("ERROR: Nickname cannot be empty");
                return;
            }

            // Регистрируем клиента
            if (!server.registerClient(nickname, this)) {
                out.println("ERROR: Nickname already in use");
                return;
            }

            logger.info("Клиент подключился: {}", nickname);
            server.broadcastSystemMessage(nickname + " присоединился к чату");

            // Отправляем приветствие
            out.println("Добро пожаловать в чат, " + nickname + "!");
            out.println("Команды: /users - список пользователей, /exit - выход, /private - личное сообщение");

            // Основной цикл обработки сообщений
            String input;
            while (running && (input = in.readLine()) != null) {
                processMessage(input);
            }

        } catch (IOException e) {
            errorLogger.warn("Ошибка соединения с клиентом {}: {}", nickname, e.getMessage());
        } finally {
            disconnect();
        }
    }

    private void processMessage(String input) {
        try {
            if (input.equalsIgnoreCase("/exit")) {
                running = false;
                return;
            }

            if (input.equalsIgnoreCase("/users")) {
                sendUserList();
                return;
            }

            if (input.startsWith("/private ")) {
                sendPrivateMessage(input.substring(8));
                return;
            }

            // Обычное широковещательное сообщение
            Message message = new Message(MessageType.BROADCAST, nickname, null, input);
            server.broadcastMessage(message);
            logger.info("Сообщение от {}: {}", nickname, input);

        } catch (Exception e) {
            errorLogger.error("Ошибка обработки сообщения от {}: {}", nickname, e.getMessage(), e);
            sendMessage("Ошибка обработки сообщения: " + e.getMessage());
        }
    }

    private void sendPrivateMessage(String input) {
        try {
            // Формат: /private получатель сообщение
            int firstSpace = input.indexOf(' ');
            if (firstSpace == -1) {
                sendMessage("Формат: /private получатель сообщение");
                return;
            }

            String recipient = input.substring(0, firstSpace);
            String text = input.substring(firstSpace + 1);

            if (text.trim().isEmpty()) {
                sendMessage("Сообщение не может быть пустым");
                return;
            }

            Message message = new Message(MessageType.PRIVATE, nickname, recipient, text);
            if (server.sendPrivateMessage(message)) {
                logger.info("Личное сообщение {} -> {}: {}", nickname, recipient, text);
                sendMessage("[Вы -> " + recipient + "]: " + text);
            } else {
                sendMessage("Пользователь " + recipient + " не найден или отключен");
            }

        } catch (Exception e) {
            errorLogger.error("Ошибка отправки личного сообщения: {}", e.getMessage(), e);
            sendMessage("Ошибка: " + e.getMessage());
        }
    }

    private void sendUserList() {
        Set<String> users = server.getConnectedUsers();
        StringBuilder userList = new StringBuilder("Подключенные пользователи:\n");
        for (String user : users) {
            userList.append("  - ").append(user).append("\n");
        }
        sendMessage(userList.toString());
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public void sendMessage(Message message) {
        sendMessage(message.toString());
    }

    public String getNickname() {
        return nickname;
    }

    public void disconnect() {
        running = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            errorLogger.warn("Ошибка при закрытии соединения: {}", e.getMessage());
        }

        server.removeClient(nickname);
        logger.info("Клиент отключился: {}", nickname);
        server.broadcastSystemMessage(nickname + " покинул чат");
    }
}