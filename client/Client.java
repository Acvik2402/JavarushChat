package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.Connection;
import com.javarush.task.task30.task3008.ConsoleHelper;
import com.javarush.task.task30.task3008.Message;
import com.javarush.task.task30.task3008.MessageType;

import java.io.IOException;
import java.net.Socket;

public class Client {
    protected Connection connection;
    private volatile boolean clientConnected = false;

    protected String getServerAddress () {
        ConsoleHelper.writeMessage("Введите адрес сервера.");
        return ConsoleHelper.readString();
    }

    protected int getServerPort () {
        ConsoleHelper.writeMessage("Введите номер порта.");
        return ConsoleHelper.readInt();
    }

    protected String getUserName () {
        ConsoleHelper.writeMessage("Введите имя пользователя.");
        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole () {
        return true;
    }

    protected SocketThread getSocketThread () {
        return new SocketThread();
    }

    protected void sendTextMessage (String text) {
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException exception) {
            ConsoleHelper.writeMessage("Не удалось отправить сообщение");
            clientConnected = false;
        }
    }

    public class SocketThread extends Thread {
        protected void processIncomingMessage (String message) {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser (String userName) {
            ConsoleHelper.writeMessage("Пользователь " + userName + " подключился к чату.");
        }

        protected void informAboutDeletingNewUser (String userName) {
            ConsoleHelper.writeMessage("Пользователь " + userName + " покинул чат.");

        }

        protected void notifyConnectionStatusChanged (boolean clientConnected) {
            synchronized (Client.this) {
                Client.this.clientConnected = clientConnected;
                Client.this.notify();
            }
        }

        protected void clientHandshake () throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.NAME_REQUEST) {
                    connection.send(new Message(MessageType.USER_NAME, getUserName()));
                } else if (message.getType() == MessageType.NAME_ACCEPTED) {
                    notifyConnectionStatusChanged(true);
                    return;
                } else throw new IOException("Unexpected MessageType");
            }
        }

        protected void clientMainLoop () throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == null) {
                    throw new IOException("Unexpected MessageType");
                } else if (message.getType().equals(MessageType.TEXT)) {
                    processIncomingMessage(message.getData());
                } else if (message.getType().equals(MessageType.USER_ADDED)) {
                    informAboutAddingNewUser(message.getData());
                } else if (message.getType().equals(MessageType.USER_REMOVED)) {
                    informAboutDeletingNewUser(message.getData());
                } else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }

        public void run () {

            try {
//                Socket socket = new Socket(getServerAddress(), getServerPort());
                Client.this.connection = new Connection(new Socket(getServerAddress(), getServerPort()));
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
                e.printStackTrace();
            }

        }

    }

    public void run () {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
                ConsoleHelper.writeMessage("Ошибка при запуске клиента!");
                return;
            }
        }
        if (clientConnected) {
            ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду 'exit'.");

        } else {
            ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
            return;
        }
        while (clientConnected) {
            String message = ConsoleHelper.readString();
            if (message.equals("exit")) {
                break;
            }
            if (shouldSendTextFromConsole()) {
                sendTextMessage(message);
            }
        }

    }

    public static void main (String[] args) {
        Client client = new Client();
        client.run();
    }
}
