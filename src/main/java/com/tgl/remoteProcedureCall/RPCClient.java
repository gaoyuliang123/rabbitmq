package main.java.com.tgl.remoteProcedureCall;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

/**
 * 远程过程调用RPC
 */
public class RPCClient {

    private static final String RPC_EXCHANGE_NAME = "RPC_EXCHANGE";
    public static void main(String[] args){
        //连接及建立通道
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = null;
        try {
            connection = factory.newConnection();
            Channel channel = connection.createChannel();
            //声明路由及路由类型
            channel.exchangeDeclare(RPC_EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
            //声明返回的队列
            String replayToQueueName = channel.queueDeclare().getQueue();
            System.out.println(" [x] Requesting fib(30)");
            //请求
            String routingKey = "rpc";
            String message = "30";
            final String correlationId = UUID.randomUUID().toString();
            System.out.println("correlationId:" + correlationId);
            /**
             * AMPQ 0-9-1协议预定义了消息的14种属性。大部分属性都很少用到，除了下面的几种：
             * ① deliveryMode：标记一个消息是持久的（值为2）还是短暂的（2以外的任何值），你可能还记得我们的第二个教程中用到过这个属性。
             * ② contentType：描述编码的mime-type（mime-type of the encoding）。比如最常使用JSON格式，就可以将该属性设置为application/json。
             * ③replyTo：通常用来命名一个回调队列。
             * ④correlationId：用来关联RPC的响应和请求。
             */
            AMQP.BasicProperties replayProps = new AMQP.BasicProperties()
                    .builder()
                    .replyTo(replayToQueueName)
                    .correlationId(correlationId) //将一个响应和一个请求进行关联
                    .build();
            //发布消息
            channel.basicPublish(RPC_EXCHANGE_NAME, routingKey, replayProps, message.getBytes("utf-8"));
            //获取响应信息
            //创建了一个阻塞队列ArrayBlockingQueue并将它的容量设为1，因为我们只需要接受一个响应.
            final BlockingQueue<String> response =  new ArrayBlockingQueue<String>(1);
            channel.basicConsume(replayToQueueName, true, new DefaultConsumer(channel){
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    super.handleDelivery(consumerTag, envelope, properties, body);
                    if (properties.getCorrelationId().equals(correlationId)) {//匹配的话就放到阻塞队列ArrayBlockingQueue中。同时，主线程正等待影响
                        System.out.println("[client current time] : " + System.currentTimeMillis());
                        response.offer(new String(body,"utf-8"));
                    }

                }


            });
            String returnMessage = response.take();
            System.out.println(" [.] Got '" + returnMessage + "'");
        } catch (IOException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
        }  finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
