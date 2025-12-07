package com.lab11.common;

public enum MessageType {
    CONNECT,       // Подключение клиента
    DISCONNECT,    // Отключение клиента
    BROADCAST,     // Широковещательное сообщение
    PRIVATE,       // Личное сообщение
    USERS_LIST,    // Запрос списка пользователей
    USER_JOINED,   // Пользователь подключился
    USER_LEFT      // Пользователь отключился
}