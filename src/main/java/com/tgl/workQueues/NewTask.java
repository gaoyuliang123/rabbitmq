package main.java.com.tgl.workQueues;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

/**
 * 耗时的任务分发给多个工作者:
 * 循环分发使用任务队列的一个优势在于容易并行处理。
 * 如果积压了大量的工作，我们只需要添加更多的工作者（Worker.java），这样很容易扩展
 */
public class NewTask {
    //定义队列的名字
    private static final String QUEUE_NAME = "work";

    public static void main(String[] args) throws Exception {
        //创建一个连接到Rabbit服务器的连接
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();//创建一个通道（channel），大部分的API操作均在这里完成.

        boolean durable = true; //持久化队列
        channel.queueDeclare(QUEUE_NAME, durable, false, false, null);//必须指明消息要发到哪个队列
        String message = "10.........."; //模拟消息中的点:代表耗时。假装我们很忙。我们用字符串中的点号.来表示任务的复杂性，一个点就表示需要耗时1秒
        //发布消息
        //MessageProperties.PERSISTENT_TEXT_PLAIN 持久化消息
        channel.basicPublish("", QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
        System.out.println("[x] Send'" + message + "'");

        //必须将通道和连接关闭
        channel.close();
        connection.close();
    }
}
