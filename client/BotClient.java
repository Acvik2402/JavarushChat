package com.javarush.task.task30.task3008.client;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class BotClient extends Client{

    @Override
    protected String getUserName () {
        return "date_bot_"+ (int) (Math.random() * 100);
    }

    @Override
    protected boolean shouldSendTextFromConsole () {
        return false;
    }

    @Override
    protected SocketThread getSocketThread () {
        return new BotSocketThread();
    }


    public class BotSocketThread extends SocketThread {
        @Override
        protected void processIncomingMessage (String message) {
            super.processIncomingMessage(message);
            if (message.contains(":")) {
                String[] mess = message.split(":");
                if (mess[1].trim().equals("дата")) {
                    printFormatted(mess[0].trim(), "d.MM.YYYY");
                } else if (mess[1].trim().equals("день")) {
                    printFormatted(mess[0].trim(), "d");
                }else if (mess[1].trim().equals("месяц")) {
                    printFormatted(mess[0].trim(), "MMMM");
                }else if (mess[1].trim().equals("год")) {
                    printFormatted(mess[0].trim(), "YYYY");
                }else if (mess[1].trim().equals("время")) {
                    printFormatted(mess[0].trim(), "H:mm:ss");
                }else if (mess[1].trim().equals("час")) {
                    printFormatted(mess[0].trim(), "H");
                }else if (mess[1].trim().equals("минуты")) {
                    printFormatted(mess[0].trim(), "m");
                }else if (mess[1].trim().equals("секунды")) {
                    printFormatted(mess[0].trim(), "s");
                }
            }

        }

        private void printFormatted (String userName, String pattern) {
            Calendar cal = new GregorianCalendar();
            SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
            dateFormat.setTimeZone(cal.getTimeZone());
            sendTextMessage("Информация для " + userName.trim() + ": " + dateFormat.format(cal.getTime()));
        }

        @Override
        protected void clientMainLoop () throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }
    }
    public static void main (String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();
    }
}
