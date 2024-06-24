package com.cg.schedule.manager;

import com.cg.schedule.exception.ErrorCode;
import com.cg.schedule.common.conf.MigratorAppConf;
import com.cg.schedule.enums.TaskStatus;
import com.cg.schedule.enums.TimerStatus;
import com.cg.schedule.exception.BusinessException;
import com.cg.schedule.mapper.TaskMapper;
import com.cg.schedule.mapper.TimerMapper;
import com.cg.schedule.model.TaskModel;
import com.cg.schedule.model.TimerModel;
import com.cg.common.redis.ReentrantDistributeLock;
import com.cg.schedule.redis.TaskCache;
import com.cg.schedule.utils.TimerUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class MigrateManagerImpl implements MigratorManager{

    @Autowired
    private TimerMapper timerMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    ReentrantDistributeLock reentrantDistributeLock;

    @Autowired
    private MigratorAppConf migratorAppConf;

    @Autowired
    private TaskCache taskCache;

    @Override
    public void migrateTimer(TimerModel timerModel) {
        // 2. 校验状态
        if(timerModel.getStatus() != TimerStatus.Enable.getStatus()){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"Timer非Enable状态，迁移失败，timerId:"+timerModel.getTimerId());
        }

        // 3.取得批量的执行时机
        CronExpression cronExpression;
        try {
            cronExpression = new CronExpression(timerModel.getCron());
        } catch (ParseException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"解析cron表达式失败："+timerModel.getCron());
        }
        Date now = new Date();
        //为什么是2个小时？为了解决当第一次迁移时失败，第二次迁移还能迁移回前面一个小时的任务，因为我的迁移周期的1个小时，所以每次都会重复1个小时的数据迁移，这样就可以一定程度上避免第一次失败。
        Date end = TimerUtils.GetForwardTwoMigrateStepEnd(now,migratorAppConf.getMigrateStepMinutes());

        List<Long> executeTimes = TimerUtils.GetCronNextsBetween(cronExpression,now,end);
        if (CollectionUtils.isEmpty(executeTimes) ){
            log.warn("获取执行时机 executeTimes 为空");
            return;
        }
        // 执行时机加入数据库
        List<TaskModel> taskList = batchTasksFromTimer(timerModel,executeTimes);
        // 基于 timer_id + run_timer 唯一键，保证任务不被重复插入
        taskMapper.batchSave(taskList);

        // 执行时机加入 redis ZSet
        boolean cacheRes = taskCache.cacheSaveTasks(taskList);
        if(!cacheRes){
            log.error("Zset存储taskList失败");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"ZSet存储taskList失败，timerId:"+timerModel.getTimerId());
        }
    }

    private List<TaskModel> batchTasksFromTimer(TimerModel timerModel, List<Long> executeTimes){
        if(timerModel == null || CollectionUtils.isEmpty(executeTimes)){
            return null;
        }

        List<TaskModel> taskList = new ArrayList<>();
        for (Long runTimer:executeTimes) {
            TaskModel task = new TaskModel();
            task.setApp(timerModel.getApp());
            task.setTimerId(timerModel.getTimerId());
            task.setRunTimer(runTimer);
            task.setStatus(TaskStatus.NotRun.getStatus());
            taskList.add(task);
        }
        return taskList;
    }
}
