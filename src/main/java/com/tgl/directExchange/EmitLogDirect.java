package main.java.com.tgl.directExchange;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class EmitLogDirect {
    private static final String EXCHANGE_NAME  = "direct_logs";
    public static void main(String[] args) throws Exception {
        //建立连接和通道
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        //声明路由及路由类型
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
        //发布消息
//        String routingKey = "info";
        String routingKey = "error";
        String message = "...direct msg...";
        channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes());
        //关闭连接和通道
        channel.close();
        connection.close();
    }
}
