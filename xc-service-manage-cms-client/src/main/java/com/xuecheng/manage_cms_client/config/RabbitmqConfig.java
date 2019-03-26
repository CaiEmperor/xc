package com.xuecheng.manage_cms_client.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitmqConfig配置类
 */
@Configuration
public class RabbitmqConfig {

    //交换机的名称
    public static final String EX_ROUTING_CMS_POSTPAGE="ex_routing_cms_postpage";
    //队列bean的名称
    public static final String QUEUE_CMS_POSTPAGE = "queue_cms_postpage";

    //@Value从application.yml中取数据
    //队列的名称
    @Value("${xuecheng.mq.queue}")
    public String queue_cms_postpage_name;
    //routingkey:站点id
    @Value("${xuecheng.mq.routingKey}")
    public String routingKey;

    //声明交换机
    //@Bean(相当于spring中的bean标签)中的相当于bean标签中的id
    @Bean(EX_ROUTING_CMS_POSTPAGE)
    public Exchange EX_ROUTING_CMS_POSTPAGE(){
        //directExchange指使用topic类型的交换机,durable(true) 将交换机持久化，mq重启之后交换机还在
        Exchange exchange = ExchangeBuilder.directExchange(EX_ROUTING_CMS_POSTPAGE).durable(true).build();
        return exchange;
    }
    //声明队列
    @Bean(QUEUE_CMS_POSTPAGE)
    public Queue QUEUE_CMS_POSTPAGE(){
        Queue queue = new Queue(queue_cms_postpage_name);
        return queue;
    }

    //绑定队列到交换机
    //QUEUE_CMS_POSTPAGE队列绑定交换机，指定routingkey
    //@Autowired自动按类型注入,@Qualifier(注入参数是可单独使用)是spring中的根据名字注入,两者一般结合使用
    @Bean
    public Binding BINDING_QUEUE_INFORM_SMS(@Qualifier(QUEUE_CMS_POSTPAGE) Queue queue,@Qualifier(EX_ROUTING_CMS_POSTPAGE) Exchange exchange){
        //bind(queue).to(exchange)交换机和队列进行绑定,with(routingKey)指定带统配符的routingkey,noargs()指定一些参数
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(routingKey).noargs();
        return binding;
    }
}
