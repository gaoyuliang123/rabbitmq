package main.java.com.tgl.topicExchange;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class EmitLogTopic {
    private static final String EXCHANGE_NAME  = "topic_logs";
    public static void main(String[] args) throws Exception {
        //建立连接和通道
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        //声明路由及路由类型
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        //发布消息
        String routingKey = "kern.critical";
        String message = "...topic msg...";
        channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes());
        //关闭连接和通道
        channel.close();
        connection.close();
    }
}
