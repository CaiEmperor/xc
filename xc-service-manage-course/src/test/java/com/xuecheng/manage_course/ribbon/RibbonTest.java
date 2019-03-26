package com.xuecheng.manage_course.ribbon;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 *Ribbon负载均衡测试
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class RibbonTest {

    @Autowired
    //远程调用访问
    private RestTemplate restTemplate;

    @Test
    public void ribbonTest(){
        //获取服务名
        String serviceId = "XC-SERVICE-MANAGE-CMS";
        for (int i = 0; i < 10; i++) {
            //ribbon客户端根据服务id从eurekaServer中获取服务列表
            ResponseEntity<Map> forEntity = restTemplate.getForEntity("http://" + serviceId + "/cms/page/get/5a754adf6abb500ad05688d9", Map.class);
            //获取调用体
            Map body = forEntity.getBody();
            System.out.println(body);
        }
    }

}
