package com.xuecheng.test.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 路由(统配符)符模式
 *  1、每个消费者监听自己的队列，并且设置带统配符的routingkey。
    2、生产者将消息发给broker，由交换机根据routingkey来转发消息到指定的队列
 * //队列绑定交换机指定通配符：
     //统配符规则：
     //中间以“.”分隔。
     //符号#可以匹配多个词，符号*可以匹配一个词语
 */
@Configuration//相当于spring的核心配置文件
public class RabbitmqConfig {

    //交换机名
    public static final String EXCHANGE_TOPICS_INFORM = "exchange_topics_inform";
    //邮件队列名
    public static final String QUEUE_INFORM_EMAIL = "queue_inform_email";
    //邮件带统配符的routingkey
    public static final String ROUTINGKEY_EMAIL ="inform.#.email.#";
    //信息队列名
    public static final String QUEUE_INFORM_SMS = "queue_inform_sms";
    //信息带统配符的routingkey
    public static final String ROUTINGKEY_SMS="inform.#.sms.#";

    //声明交换机
    //@Bean(相当于spring中的bean标签)中的相当于bean标签中的id
    @Bean(EXCHANGE_TOPICS_INFORM)
    public Exchange EXCHANGE_TOPICS_INFORM(){
        //durable(true) 将交换机持久化，mq重启之后交换机还在,topicExchange指使用topic类型的交换机
        Exchange exchange = ExchangeBuilder.topicExchange(EXCHANGE_TOPICS_INFORM).durable(true).build();
        return exchange;
    }
    //声明邮件队列
    @Bean(QUEUE_INFORM_EMAIL)
    public Queue QUEUE_INFORM_EMAIL(){
        Queue queue = new Queue(QUEUE_INFORM_EMAIL);
        return queue;
    }
    //声明邮件队列
    @Bean(QUEUE_INFORM_SMS)
    public Queue QUEUE_INFORM_SMS(){
        Queue queue = new Queue(QUEUE_INFORM_SMS);
        return queue;
    }
    //ROUTINGKEY_EMAIL队列绑定交换机，指定带统配符的routingkey
    //@Autowired自动按类型注入,@Qualifier(注入参数是可单独使用)是spring中的根据名字注入,两者一般结合使用
    @Bean
    public Binding BINDING_QUEUE_INFORM_EMAIL(@Qualifier(EXCHANGE_TOPICS_INFORM)Exchange exchange, @Qualifier(QUEUE_INFORM_EMAIL)Queue queue){
        //bind(queue).to(exchange)交换机和队列进行绑定,with(ROUTINGKEY_EMAIL)指定带统配符的routingkey,noargs()指定一些参数
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(ROUTINGKEY_EMAIL).noargs();
        return binding;
    }
    //ROUTINGKEY_SMS队列绑定交换机，指定带统配符的routingkey
    @Bean
    public Binding BINDING_ROUTINGKEY_SMS(@Qualifier(EXCHANGE_TOPICS_INFORM)Exchange exchange, @Qualifier(QUEUE_INFORM_SMS)Queue queue){
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(ROUTINGKEY_SMS).noargs();
        return binding;
    }
}
