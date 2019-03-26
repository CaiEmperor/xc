package com.xuecheng.test.rabbitmq;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 路由模式：
 1、每个消费者监听自己的队列，并且设置routingkey。
 2、生产者将消息发给交换机，由交换机根据routingkey来转发消息到指定的队列。
 */

/**
 * 根据用户的通知设置去通知用户，设置接收Email的用户只接收Email，设置接收sms的用户只接收sms，设置两种
    通知类型都接收的则两种通知都有效。
 * 1、生产者
     声明exchange_routing_inform交换机。
     声明两个队列并且绑定到此交换机，绑定时需要指定routingkey
     发送消息时需要指定routingkey
 */
public class Producer_routing {

    //交换机名
    private static final String EXCHANGE_ROUTING_INFORM = "exchange_routing_inform";
    //邮件队列名
    private static final String QUEUE_INFORM_EMAIL = "queue_inform_email";
    //邮件routingkey
    private static final String ROUTINGKEY_EMAIL="inform_email";
    //信息队列名
    private static final String QUEUE_INFORM_SMS = "queue_inform_sms";
    //信息routingkey
    private static final String ROUTINGKEY_SMS="inform_sms";
    //共有routingkey
    private static final String ROUTINGKEY_INFORM="inform";

    public static void main(String[] args) {

        //通过连接工厂创建新的连接和mq建立连接
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("127.0.0.1");//IP地址
        connectionFactory.setPort(5672);//端口号
        connectionFactory.setUsername("guest");//用户名
        connectionFactory.setPassword("guest");//密码
        //设置虚拟机，一个mq服务可以设置多个虚拟机，每个虚拟机就相当于一个独立的mq
        connectionFactory.setVirtualHost("/");

        Connection connection = null;
        Channel channel = null;
        try {
            //建立新连接
            connection = connectionFactory.newConnection();
            //创建会话通道,生产者和mq服务所有通信都在channel通道中完成
            channel = connection.createChannel();
            //声明一个交换机
            //参数：String exchange, String type
            /**
             * 参数明细：
             * 1、交换机的名称
             * 2、交换机的类型
             * fanout：对应的rabbitmq的工作模式是 publish/subscribe
             * direct：对应的Routing	工作模式
             * topic：对应的Topics工作模式
             * headers： 对应的headers工作模式
             */
            channel.exchangeDeclare(EXCHANGE_ROUTING_INFORM, BuiltinExchangeType.DIRECT);
            //声明队列，如果队列在mq 中没有则要创建
            //参数：String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
            /**
             * 参数明细
             * 1、queue 队列名称
             * 2、durable 是否持久化，如果持久化，mq重启后队列还在
             * 3、exclusive 是否独占连接，队列只允许在该连接中访问，如果connection连接关闭队列则自动删除,如果将此参数设置true可用于临时队列的创建
             * 4、autoDelete 自动删除，队列不再使用时是否自动删除此队列，如果将此参数和exclusive参数设置为true就可以实现临时队列（队列不用了就自动删除）
             * 5、arguments 参数，可以设置一个队列的扩展参数，比如：可设置存活时间
             */
            //邮件队列
            channel.queueDeclare(QUEUE_INFORM_EMAIL, true, false, false, null);
            //信息队列
            channel.queueDeclare(QUEUE_INFORM_SMS, true, false, false, null);
            //发送消息
            //进行交换机和队列绑定
            //参数：String queue, String exchange, String routingKey
            /**
             * 参数明细：
             * 1、queue 队列名称
             * 2、exchange 交换机名称
             * 3、routingKey 路由key，作用是交换机根据路由key的值将消息转发到指定的队列中，在发布订阅模式中调协为空字符串
             */
            //邮件队列和交换机绑定
            channel.queueBind(QUEUE_INFORM_EMAIL, EXCHANGE_ROUTING_INFORM, ROUTINGKEY_EMAIL);
            channel.queueBind(QUEUE_INFORM_EMAIL, EXCHANGE_ROUTING_INFORM, ROUTINGKEY_INFORM);
            //信息队列和交换机绑定
            channel.queueBind(QUEUE_INFORM_SMS, EXCHANGE_ROUTING_INFORM, ROUTINGKEY_SMS);
            channel.queueBind(QUEUE_INFORM_SMS, EXCHANGE_ROUTING_INFORM, ROUTINGKEY_INFORM);
            //发送消息
            //参数：String exchange, String routingKey, BasicProperties props, byte[] body
            /**
             * 参数明细：
             * 1、exchange，交换机，如果不指定将使用mq的默认交换机（设置为""）
             * 2、routingKey，路由key，交换机根据路由key来将消息转发到指定的队列，如果使用默认交换机，routingKey设置为队列的名称
             * 3、props，消息的属性
             * 4、body，消息内容
             */
            for (int i = 0; i < 5; i++) {
                //消息内容
                String message = "send email inform message to user";
                //发送消息,指定routingKey
                channel.basicPublish(EXCHANGE_ROUTING_INFORM, ROUTINGKEY_EMAIL, null, message.getBytes());
                System.out.println("send to mq:"+message);
            }
            for (int i = 0; i < 5; i++) {
                //消息内容
                String message = "send sms inform message to user";
                //发送消息,指定routingKey
                channel.basicPublish(EXCHANGE_ROUTING_INFORM, ROUTINGKEY_SMS, null, message.getBytes());
                System.out.println("send to mq:"+message);
            }
            for (int i = 0; i < 5; i++) {
                //消息内容
                String message = "send inform message to user";
                //发送消息,指定routingKey
                channel.basicPublish(EXCHANGE_ROUTING_INFORM, ROUTINGKEY_INFORM, null, message.getBytes());
                System.out.println("send to mq:"+message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } finally {
            //关闭连接,先关通道,再关连接
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
