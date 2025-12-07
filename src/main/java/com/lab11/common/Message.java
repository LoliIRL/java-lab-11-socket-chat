package com.lab11.common;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message implements Serializable {
    private MessageType type;
    private String sender;
    private String recipient;
    private String text;
    private LocalDateTime timestamp;

    public Message(MessageType type, String sender, String recipient, String text) {
        this.type = type;
        this.sender = sender;
        this.recipient = recipient;
        this.text = text;
        this.timestamp = LocalDateTime.now();
    }

    // Геттеры
    public MessageType getType() { return type; }
    public String getSender() { return sender; }
    public String getRecipient() { return recipient; }
    public String getText() { return text; }
    public LocalDateTime getTimestamp() { return timestamp; }

    // Форматированное отображение
    @Override
    public String toString() {
        String time = timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        if (type == MessageType.BROADCAST) {
            return String.format("[%s] [%s -> ALL]: %s", time, sender, text);
        } else if (type == MessageType.PRIVATE) {
            return String.format("[%s] [%s -> %s]: %s", time, sender, recipient, text);
        } else {
            return String.format("[%s] [System]: %s", time, text);
        }
    }

    // Для логирования
    public String toLogString() {
        return String.format("Type: %s, From: %s, To: %s, Text: %s",
                type, sender, recipient != null ? recipient : "ALL", text);
    }
}