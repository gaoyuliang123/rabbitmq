package main.java.com.tgl.directExchange;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;

public class ReceiveLogsDirect {
    private static final String EXCHANGE_NAME  = "direct_logs";
    public static void main(String[] args) throws Exception {
        //建立连接和通道
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        //声明路由及路由类型
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
        //声明一个随机的队列名:调用无参的queueDeclare()的时候，意味着创建了一个非持久、独特的、自动删除的队列，并返回一个自动生成的名字。
        // 这个名字看起来形如：amq.gen-JzTY20BRgKO-HjmUJj0wLg。
        String queueName = channel.queueDeclare().getQueue();
        //绑定队列到路由上
        //String[] bindingKeys = {"info", "waring", "error"};
        String[] bindingKeys = {"error"};
        //根据绑定键绑定
        for (String bindingKey : bindingKeys) {
            channel.queueBind(queueName, EXCHANGE_NAME, bindingKey);
        }
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        //开始监听消息
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                super.handleDelivery(consumerTag, envelope, properties, body);
                String message = new String(body, "utf-8");
                System.out.println(" [x] Received '" + envelope.getRoutingKey() + "':'" + message + "'");
            }
        };
        channel.basicConsume(queueName, true, consumer);
    }

    /**
     * 测试方法：
     * 首先，启动一个消费者实例（ReceiveLogsDirect.java），
     * 然后将其中的要监听的级别改为String[] routingKeys = {"error"};，再启动另一个消费者实例。此时，这两个消费者都开始监听了，一个监听所有级别的日志消息，另一个监听error日志消息。
     * 然后，启动生产者（EmitLogDirect.java），之后将String routingKey = "info";中的info，分别改为warning、error后运行。
     */
}
