package main.java.com.tgl.fanoutExchange;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class EmitLog {
    private static final String EXCHANGE_NAME  = "fanout_logs";
    public static void main(String[] args) throws Exception {
        //建立连接和通道
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        //声明路由及路由类型
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
        //发布消息
        String message = "msg...";
        channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes());//当发消息的时候，我们需要提供一个路由键routingKey，但是它的值会被fanout类型的路由器忽略
        //关闭连接和通道
        channel.close();
        connection.close();
    }
}
