package com.javarush.task.task30.task3008;

import com.javarush.task.task30.task3008.client.Client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.*;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void sendBroadcastMessage (Message message) {
        for (Map.Entry<String, Connection> entry : connectionMap.entrySet()) {
            try {
                entry.getValue().send(message);
            } catch (Exception e) {
                System.out.println("Не удалось отправить сообщение");
            }
        }
    }

    private static class Handler extends Thread {
        private Socket socket;

        public Handler (Socket socket) {
            this.socket = socket;
        }

        public void run () {
            ConsoleHelper.writeMessage("Было установлено соединение с удаленным адресом : " + socket.getRemoteSocketAddress());
            String userName = "";
            try (Connection connection = new Connection(socket)) {
//                if (socket.getRemoteSocketAddress()!=null)
//                ConsoleHelper.writeMessage("Установлено новое соединение с удаленным адресом"+socket.getRemoteSocketAddress());
                userName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                notifyUsers(connection, userName);
                serverMainLoop(connection, userName);
            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("произошла ошибка при обмене данными с удаленным адресом");
            }finally {
                if (userName != null) {
                    connectionMap.remove(userName);
                    sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
                }
            }

        }

        private String serverHandshake (Connection connection) throws IOException, ClassNotFoundException {
            Message message;
            do {
                connection.send(new Message(MessageType.NAME_REQUEST));
                message = connection.receive();
            } while (message.getType() != MessageType.USER_NAME
                    || message.getData().isEmpty()
                    || connectionMap.containsKey(message.getData()));
            connectionMap.put(message.getData(), connection);
            connection.send(new Message(MessageType.NAME_ACCEPTED, "User name accepted"));

            return message.getData();
        }

        private void notifyUsers (Connection connection, String userName) throws IOException {
            for (Map.Entry<String, Connection> entry : connectionMap.entrySet()) {
                if (entry.getKey().equals(userName)) {
                    continue;
                } else {
                    connection.send(new Message(MessageType.USER_ADDED, entry.getKey()));
                }
            }

        }

        private void serverMainLoop (Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    sendBroadcastMessage(new Message(MessageType.TEXT, String.format("%s: %s", userName, message.getData())));
                } else {
                    ConsoleHelper.writeMessage("Не верный тип сообщения");
                }
            }
        }

    }

    public static void main (String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(ConsoleHelper.readInt())) {
            ConsoleHelper.writeMessage("Сервер запущен");
            while (true) {
                new Handler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
