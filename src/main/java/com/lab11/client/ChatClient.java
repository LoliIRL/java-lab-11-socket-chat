package com.lab11.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
    private static final Logger logger = LoggerFactory.getLogger(ChatClient.class);

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Scanner scanner;
    private String nickname;
    private boolean running;

    public ChatClient(String host, int port) {
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            scanner = new Scanner(System.in);
            running = true;

            logger.info("Подключение к серверу {}:{} успешно", host, port);
        } catch (IOException e) {
            logger.error("Ошибка подключения к серверу: {}", e.getMessage());
            System.exit(1);
        }
    }

    public void start() {
        try {
            // Ввод никнейма
            System.out.print("Введите ваш никнейм: ");
            nickname = scanner.nextLine();
            out.println(nickname);

            // Проверка ответа сервера
            String response = in.readLine();
            if (response.startsWith("ERROR:")) {
                System.out.println(response);
                return;
            }

            System.out.println(response);

            // Запускаем поток для чтения сообщений от сервера
            Thread readerThread = new Thread(this::readMessages);
            readerThread.start();

            // Основной цикл отправки сообщений
            while (running) {
                String message = scanner.nextLine();

                if (message.equalsIgnoreCase("/exit")) {
                    out.println("/exit");
                    running = false;
                    break;
                }

                out.println(message);
            }

        } catch (IOException e) {
            logger.error("Ошибка связи с сервером: {}", e.getMessage());
        } finally {
            disconnect();
        }
    }

    private void readMessages() {
        try {
            String message;
            while (running && (message = in.readLine()) != null) {
                System.out.println(message);
            }
        } catch (IOException e) {
            if (running) {
                logger.error("Ошибка чтения сообщений: {}", e.getMessage());
            }
        }
    }

    private void disconnect() {
        running = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
            if (scanner != null) scanner.close();
        } catch (IOException e) {
            logger.warn("Ошибка при отключении: {}", e.getMessage());
        }
        logger.info("Клиент отключен");
    }

    public static void main(String[] args) {
        String host = "localhost";
        int port = 8080;

        if (args.length >= 2) {
            host = args[0];
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                logger.error("Неверный порт: {}", args[1]);
                return;
            }
        }

        System.out.println("=== Консольный чат-клиент ===");
        System.out.println("Подключение к " + host + ":" + port);

        ChatClient client = new ChatClient(host, port);
        client.start();
    }
}