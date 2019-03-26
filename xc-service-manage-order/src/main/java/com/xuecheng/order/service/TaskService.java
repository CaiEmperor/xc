package com.xuecheng.order.service;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.order.dao.XcTaskHisRepository;
import com.xuecheng.order.dao.XcTaskRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    @Autowired
    private XcTaskRepository xcTaskRepository;
    @Autowired
    private XcTaskHisRepository xcTaskHisRepository;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 查询某个时间之前的前n条任务
     * @param updateTime 更新时间
     * @param size 查询的条数
     * @return
     */
    public List<XcTask> findXcTaskList(Date updateTime, int size) {
        //设置分页参数
        Pageable pageable = PageRequest.of(0, size);
        //查询某个时间之前的前n条任务
        Page<XcTask> xcTasks = xcTaskRepository.findByUpdateTimeBefore(pageable, updateTime);
        List<XcTask> xcTasksContent = xcTasks.getContent();
        return xcTasksContent;
    }

    /**
     * 向mq发布添加选课消息
     * @param xcTask
     * @param ex 交换机
     * @param routingKey
     */
    public void publish(XcTask xcTask, String ex, String routingKey) {
        //根据id查询选课消息
        Optional<XcTask> xcTaskOptional = xcTaskRepository.findById(xcTask.getId());
        if (xcTaskOptional.isPresent()){
            //向mq发送添加选课的消息
            rabbitTemplate.convertAndSend(ex, routingKey, xcTask);
            XcTask xcTask1 = xcTaskOptional.get();
            //设置更新时间
            xcTask1.setUpdateTime(new Date());
            xcTaskRepository.save(xcTask1);
        }
    }

    /**
     *获取任务
     * @param id
     * @param version
     * @return
     */
    @Transactional
    public int getTask(String id, Integer version) {
        //通过乐观锁的方式来更新数据表，如果结果大于0说明取到任务
        int count = xcTaskRepository.updateTaskVersion(id, version);
        return count;
    }

    /**
     * 完成任务
     * @param id
     */
    @Transactional
    public void finishTask(String id) {
        Optional<XcTask> optionalXcTask = xcTaskRepository.findById(id);
        if(optionalXcTask.isPresent()){
            //当前任务
            XcTask xcTask = optionalXcTask.get();
            //历史任务
            XcTaskHis xcTaskHis = new XcTaskHis();
            BeanUtils.copyProperties(xcTask,xcTaskHis);
            xcTaskHisRepository.save(xcTaskHis);
            xcTaskRepository.delete(xcTask);
        }
    }
}
