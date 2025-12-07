# Лабораторная работа №11 - Консольный чат с сокетами
# Выполнил Киселев Максим
# ФИТ-241

## Описание
Многопользовательский консольный чат с использованием TCP-сокетов, многопоточности и логирования.

## Функциональность
✅ Многопользовательский чат-сервер  
✅ Подключение клиентов с никнеймами  
✅ Широковещательные сообщения  
✅ Личные сообщения (/private)  
✅ Список онлайн-пользователей (/users)  
✅ Логирование через SLF4J + Logback  
✅ Корректное отключение клиентов

## Структура проекта
src/main/java/com/lab11/
├── common/ # Общие классы
│ ├── Message.java
│ └── MessageType.java
├── server/ # Серверная часть
│ ├── ChatServer.java
│ └── ClientHandler.java
└── client/ # Клиентская часть
└── ChatClient.java


## Запуск

### 1. Запуск сервера

mvn compile exec:java -Dexec.mainClass="com.lab11.server.ChatServer"

### 2. Запуск клиента

mvn compile exec:java -Dexec.mainClass="com.lab11.client.ChatClient"
