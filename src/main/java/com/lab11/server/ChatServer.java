package com.lab11.server;

import com.lab11.common.Message;
import com.lab11.common.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    private static final Logger logger = LoggerFactory.getLogger(ChatServer.class);
    private static final Logger errorLogger = LoggerFactory.getLogger("ERROR_LOGGER");

    private final int port;
    private final Map<String, ClientHandler> clients;
    private ServerSocket serverSocket;
    private boolean running;

    public ChatServer(int port) {
        this.port = port;
        this.clients = new ConcurrentHashMap<>();
        this.running = false;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            logger.info("Сервер запущен на порту {}", port);
            logger.info("Ожидание подключений...");

            while (running) {
                Socket clientSocket = serverSocket.accept();
                logger.info("Новое подключение: {}", clientSocket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                new Thread(clientHandler).start();
            }

        } catch (IOException e) {
            if (running) {
                errorLogger.error("Ошибка сервера: {}", e.getMessage(), e);
            }
        } finally {
            stop();
        }
    }

    public synchronized boolean registerClient(String nickname, ClientHandler handler) {
        if (clients.containsKey(nickname)) {
            return false;
        }
        clients.put(nickname, handler);
        return true;
    }

    public synchronized void removeClient(String nickname) {
        clients.remove(nickname);
    }

    public synchronized Set<String> getConnectedUsers() {
        return new HashSet<>(clients.keySet());
    }

    public synchronized boolean sendPrivateMessage(Message message) {
        ClientHandler recipient = clients.get(message.getRecipient());
        if (recipient != null) {
            recipient.sendMessage(message);
            return true;
        }
        return false;
    }

    public synchronized void broadcastMessage(Message message) {
        for (ClientHandler client : clients.values()) {
            if (!client.getNickname().equals(message.getSender())) {
                client.sendMessage(message);
            }
        }
        logger.info("Широковещательное сообщение: {}", message.toLogString());
    }

    public synchronized void broadcastSystemMessage(String text) {
        Message message = new Message(MessageType.BROADCAST, "System", null, text);
        for (ClientHandler client : clients.values()) {
            client.sendMessage(message);
        }
        logger.info("Системное сообщение: {}", text);
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            errorLogger.warn("Ошибка при закрытии сервера: {}", e.getMessage());
        }

        // Отключаем всех клиентов
        for (ClientHandler client : clients.values()) {
            client.disconnect();
        }
        clients.clear();

        logger.info("Сервер остановлен");
    }

    public static void main(String[] args) {
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                logger.error("Неверный порт: {}", args[0]);
                return;
            }
        }

        ChatServer server = new ChatServer(port);

        // Обработка завершения работы
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Завершение работы сервера...");
            server.stop();
        }));

        server.start();
    }
}