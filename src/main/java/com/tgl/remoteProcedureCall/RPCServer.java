package main.java.com.tgl.remoteProcedureCall;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeoutException;

/**
 * RPC系统的工作流程如下：
 * 当客户端启动后，它会创建一个异步的独特的回调队列。
 * 对于一个RPC请求，客户端将会发送一个配置了两个属性的消息：一个是replyTo属性，设置为这个回调队列；
 * 另一个是correlation id属性，每一个请求都会设置为一个具有唯一性的值。这个请求将会发送到rpc_queue队列。
 *
 * RPC工作者（即图中的server）将会等待rpc_queue队列的请求。
 * 当有请求到来时，它就会开始干活（计算斐波那契数）并将结果通过发送消息来返回，该返回消息发送到replyTo指定的队列。
 * 客户端将等待回调队列返回数据。当返回的消息到达时，它将检查correlation id属性。如果该属性值和请求匹配，就将响应返回给程序。

 */
public class RPCServer {

    private static final String RPC_QUEUE_NAME = "RPC_QUEUE";
    private static final String RPC_EXCHANGE_NAME = "RPC_EXCHANGE";

    //模拟的耗时任务，即计算斐波那契数
    private static int fib(int n) {
        if (0 == n) {
            return 0;
        }
        if (1 == n) {
            return 1;
        }
        return fib(n - 1) + fib(n - 2);
    }

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
            //声明队列
            channel.queueDeclare(RPC_QUEUE_NAME, false, false,false, null);
            //队列绑定到路由器上
            String bindingKey = "rpc";
            channel.queueBind(RPC_QUEUE_NAME, RPC_EXCHANGE_NAME, bindingKey);
            //一次只从队列中消费一条消息
            channel.basicQos(1);
            System.out.println(" [x] Awaiting RPC requests");
            // 回调消费（监听消息）
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    super.handleDelivery(consumerTag, envelope, properties, body);
                    AMQP.BasicProperties replayProps = new AMQP.BasicProperties()
                            .builder()
                            .correlationId(properties.getCorrelationId())
                            .build();
                    //收到RPC请求后开始处理
                    String response = "";
                    try {
                        String message = new String(body, "utf-8");
                        int n = Integer.parseInt(message);
                        System.out.println(" [.] fib(" + message + ")");
                        response += fib(n);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    } finally {
                        //处理完之后，返回响应（即发布消息）
                        System.out.println("[server current time] : " + System.currentTimeMillis());
                        channel.basicPublish("", properties.getReplyTo(), replayProps, response.getBytes());
                        channel.basicAck(envelope.getDeliveryTag(),false);
                    }
                }
            };
            channel.basicConsume(RPC_QUEUE_NAME, false, consumer);
            //loop to prevent reaching finally block
            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException _ignore) {
                }
            }
        } catch (IOException | TimeoutException e) {
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

    /**
     * 测试方法：
     * 首先，执行RPC服务端，让它等待请求的到来。
     * 然后，执行RPC客户端，即RPCClient中的main方法，发起请求。
     */

    /**
     * 思考：
     * 上面这种设计并不是RPC服务端的唯一实现，但是它有以下几个重要的优势：
     * ①如果RPC服务端很慢，你可以通过运行多个实例就可以实现扩展。
     * ②在RPC客户端，RPC要求发送和接受一个消息。非同步的方法queueDeclare是必须的。这样，RPC客户端只需要为一个RPC请求只进行一次网络往返。

     * 但我们的代码仍然太简单，并没有处理更复杂但也非常重要的问题，像：
     * ①如果没有服务端在运行，客户端该怎么办
     * ②客户端应该为一次RPC设置超时吗
     * ③如果服务端发生故障并抛出异常，它还应该返回给客户端吗？
     * ④在处理消息前，先通过边界检查、类型判断等手段过滤掉无效的消息等
     */
}
