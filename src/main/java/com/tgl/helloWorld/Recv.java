package main.java.com.tgl.helloWorld;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;

/**
 * 消费者会一直监听声明的队列。运行一次生产者（即Send.java中的main方法），消费者就会打印出接受到的消息
 */
public class Recv {

    private static final String QUEUE_NAME = "hello";

    public static void main(String[] args) throws Exception{
        //建立连接和通道
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        //声明要消费的队列
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        //回调消费消息
        Consumer consumer = new DefaultConsumer(channel) {//DefaultConsumer是Consumer接口的实现类，我们使用它来缓冲从服务器push来的消息
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                super.handleDelivery(consumerTag, envelope, properties, body);
                String message = new String(body, "utf-8");
                System.out.println(" [x] Received '" + message + "'");
            }
        };
        channel.basicConsume(QUEUE_NAME, true, consumer);
    }

}
