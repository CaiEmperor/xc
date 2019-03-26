package com.xuecheng.manage_cms.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitmqConfig配置类
 * 由于cms作为页面发布方要面对很多不同站点的服务器，
 * 面对很多页面发布队列，所以这里不再配置队列，只需要配置交换机即可。
 */
@Configuration
public class RabbitmqConfig {

    //交换机的名称
    public static final String EX_ROUTING_CMS_POSTPAGE="ex_routing_cms_postpage";

    //声明交换机(可以不用定义)
    //@Bean(相当于spring中的bean标签)中的相当于bean标签中的id
    @Bean(EX_ROUTING_CMS_POSTPAGE)
    public Exchange EX_ROUTING_CMS_POSTPAGE(){
        //directExchange指使用topic类型的交换机,durable(true) 将交换机持久化，mq重启之后交换机还在
        Exchange exchange = ExchangeBuilder.directExchange(EX_ROUTING_CMS_POSTPAGE).durable(true).build();
        return exchange;
    }
}
