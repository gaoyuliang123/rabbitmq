package main.java.com.tgl.workQueues;

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
public class Work {

    private static final String QUEUE_NAME = "work";

    public static void main(String[] args) throws Exception{
        //建立连接和通道
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        //声明要消费的队列
        boolean durable = true; //持久化队列
        channel.queueDeclare(QUEUE_NAME, durable, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        //回调消费消息
        channel.basicQos(1);//公平分发：保证在接收端一个消息没有处理完时不会接收另一个消息，即接收端发送了ack后才会接收下一个消息。在这种情况下发送端会尝试把消息发送给下一个not busy的接收端。
        final Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                super.handleDelivery(consumerTag, envelope, properties, body);
                String message = new String(body, "utf-8");
                System.out.println(" [x] Received '" + message + "'");
                try {
                    doWork(message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println(" [x] Done");
                    channel.basicAck(envelope.getDeliveryTag(), false);//入参：deliveryTag:该消息的index；multiple：是否批量.true:将一次性ack所有小于deliveryTag的消息。
                }
            }
        };
        boolean autoAck = false;
        channel.basicConsume(QUEUE_NAME, autoAck, consumer);
    }

    //假任务的执行
    private static void doWork(String task) throws InterruptedException{
        for (char c : task.toCharArray()) {
            if ('.'== c) {
                Thread.sleep(1000); //用Thread.sleep()方法来伪造耗时任务
            }
        }
    }

    /**
     * 测试方法：
     * 首先，我们来尝试同时运行两个工作者实例（Worker.java）。
     * 启动NewTask，之后，可以依次将message修改为"2.."、"3..."、"4...."、"5....."等，每修改一次就运行一次。观察console中两个工作者的接收消息情况。
     *
     * 可以看出，默认情况下，RabbitMQ是轮流发送消息给下一个消费者，平均每个消费者接收到的消息数量是相等的。
     * 这种分发消息的方式叫做循环分发。你可以试一下开3个或更多工作者的情况。
     */

}
