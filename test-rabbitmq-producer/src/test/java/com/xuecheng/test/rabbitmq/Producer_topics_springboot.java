package com.xuecheng.test.rabbitmq;

import com.xuecheng.test.rabbitmq.config.RabbitmqConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class Producer_topics_springboot {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 已邮件形式发送信息
     */
    @Test
    public void testSendEmail(){

        String message = "send email message to user";
        //发送消息:参数,1:交换机名称,2:routingkey,3:消息内容
        rabbitTemplate.convertAndSend(RabbitmqConfig.EXCHANGE_TOPICS_INFORM, "inform.email", message);
    }
}