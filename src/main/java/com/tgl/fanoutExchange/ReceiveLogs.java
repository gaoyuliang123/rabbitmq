package main.java.com.tgl.fanoutExchange;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;

public class ReceiveLogs {
    private static final String EXCHANGE_NAME  = "fanout_logs";
    public static void main(String[] args) throws Exception {
        //建立连接和通道
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        //声明路由及路由类型
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
        //声明一个随机的队列名:调用无参的queueDeclare()的时候，意味着创建了一个非持久、独特的、自动删除的队列，并返回一个自动生成的名字。
        // 这个名字看起来形如：amq.gen-JzTY20BRgKO-HjmUJj0wLg。
        String queueName = channel.queueDeclare().getQueue();
        //绑定队列到路由上
        channel.queueBind(queueName, EXCHANGE_NAME,"" );
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        //开始监听消息
        Consumer consumer = new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                super.handleDelivery(consumerTag, envelope, properties, body);
                String message = new String(body, "utf-8");
                System.out.println(" [x] Received '" + message + "'");
            }
        };
        channel.basicConsume(queueName, true, consumer);
    }

    /**
     * 测试方法：
     * 首先运行两个消费者实例ReceiveLogs.java，然后运行生产者EmitLog.java。
     * 看看两个消费者实例是不是都接收到了所有的消息。
     */
}
