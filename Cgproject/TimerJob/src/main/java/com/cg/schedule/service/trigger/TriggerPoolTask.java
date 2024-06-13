package com.cg.schedule.service.trigger;

import com.cg.schedule.model.TaskModel;
import com.cg.common.redis.ReentrantDistributeLock;
import com.cg.schedule.service.executor.ExecutorWorker;
import com.cg.schedule.utils.TimerUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TriggerPoolTask {

    @Autowired
    ReentrantDistributeLock reentrantDistributeLock;

    @Autowired
    ExecutorWorker executorWorker;

    @Async("triggerPool")
    public void runExecutor(TaskModel task) {
        if(task == null){
            return;
        }
        log.info("start runExecutor");

        executorWorker.work(TimerUtils.UnionTimerIDUnix(task.getTimerId(),task.getRunTimer()));

        log.info("end executeAsync");
    }
}
