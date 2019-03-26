package com.xuecheng.manage_cms_client.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_cms_client.dao.CmsPageRepository;
import com.xuecheng.manage_cms_client.service.PageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * ConsumerPostPage作为发布页面的消费客户端，监听页面发布队列的消息，
 * 收到消息后从mongodb下载文件，保存在本地。
 */
@Component
public class ConsumerPostPage {

    //日志
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerPostPage.class);

    @Autowired
    private PageService pageService;
    @Autowired
    private CmsPageRepository cmsPageRepository;

    /**
     * 监听队列中的消息
     * @param message
     */
    //监听列表注解
    @RabbitListener(queues = {"${xuecheng.mq.queue}"})
    public void postPage(String message){

        //解析收到的消息
        Map map = JSON.parseObject(message, Map.class);
        //打印日志信息
        LOGGER.info("receive cms post page:{}", message.toString());
        //取出页面id
        String pageId = (String) map.get("pageId");
        //查询页面信息
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if (!optional.isPresent()){
            //打印错误日志信息
            LOGGER.error("receive cms post page,cmsPage is null:{}",message.toString());
            return ;
        }
        //将静态化页面保存到本地(物理路径)
        pageService.savePageToServerPath(pageId);
    }
}
