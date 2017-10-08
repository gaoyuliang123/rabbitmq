package main.java.com.tgl.topicExchange;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;

/**
 * Topic exchange非常强大，可以实现其他任意路由器的功能。
 * 当一个队列以绑定键#绑定，它将会接收到所有的消息，而无视路由键（实际是绑定键#匹配了任意的路由键）。这和fanout路由器一样了。
 * 当*和#这两个特殊的字符不出现在绑定键中，Topic exchange就会和direct exchange类似了。

 * *（星号）仅代表一个单词
 * #（井号）代表任意个单词
 */
public class ReceiveLogsTopic {
    private static final String EXCHANGE_NAME  = "topic_logs";
    public static void main(String[] args) throws Exception {
        //建立连接和通道
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        //声明路由及路由类型
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        //声明一个随机的队列名:调用无参的queueDeclare()的时候，意味着创建了一个非持久、独特的、自动删除的队列，并返回一个自动生成的名字。
        // 这个名字看起来形如：amq.gen-JzTY20BRgKO-HjmUJj0wLg。
        String queueName = channel.queueDeclare().getQueue();
        //绑定队列到路由上
        String[] bindingKeys = {"#"};
//        String[] bindingKeys = {"kern.*"};
//        String[] bindingKeys = {"*.critical"};
//        String[] bindingKeys = {"kern.*",  "*.critical"};
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
     * 消费者：
     * 将String[] bindingKeys = {""}改为String bingingKeys[] = {"#"}，启动第一个消费者；
     * 再改为String[] bindingKeys = {"kern.*"}，启动第二个消费者；
     * 再改为String[] bindingKeys = {"*.critical"}，启动第三个消费者；
     * 再改为SString[] bindingKeys = {"kern.*",  "*.critical"}，启动第四个消费者。
     * 生产者，发送多个消息，如：
     * 路由键为kern.critical 的消息：A critical kernel error；
     * 路由键为kern.info 的消息：A kernel info；
     * 路由键为kern.warn 的消息：A kernel warning；
     * 路由键为auth.critical 的消息：A critical auth error；
     * 路由键为cron.warn 的消息：A cron waning；
     * 路由键为cron.critical 的消息：A critical cron error；
     *
     * 最后的结果：第一个消费者将会接收到所有的消息，
     * 第二个消费者将会kern的所有严重级别的日志，
     * 第三个消费者将会接收到所有设备的critical消息，
     * 第四个消费者将会接收到kern设备的所有消息和所有critical消息。
     */
}
