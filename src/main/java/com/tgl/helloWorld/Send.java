package main.java.com.tgl.helloWorld;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Send {
    //定义队列的名字
    private static final String QUEUE_NAME = "hello";

    public static void main(String[] args) throws Exception {
        //创建一个连接到Rabbit服务器的连接
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();//创建一个通道（channel），大部分的API操作均在这里完成.

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);//必须指明消息要发到哪个队列
        String message = "hello wrold";
        //发布消息
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
        System.out.println("[x] Send'" + message + "'");

        //必须将通道和连接关闭
        channel.close();
        connection.close();
    }
}
