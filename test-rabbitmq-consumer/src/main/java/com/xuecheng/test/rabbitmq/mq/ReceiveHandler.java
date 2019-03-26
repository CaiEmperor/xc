package com.xuecheng.test.rabbitmq.mq;

import com.rabbitmq.client.Channel;
import com.xuecheng.test.rabbitmq.config.RabbitmqConfig;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 消费者
 */
@Component
public class ReceiveHandler {

    //该注解进行监听队列
    @RabbitListener(queues = {RabbitmqConfig.QUEUE_INFORM_EMAIL})

    public void receive_email(String msg, Message message, Channel channel){
        System.out.println("receive message"+msg);
        System.out.println("receive message"+message);
        System.out.println("receive message"+channel);
    }
}
