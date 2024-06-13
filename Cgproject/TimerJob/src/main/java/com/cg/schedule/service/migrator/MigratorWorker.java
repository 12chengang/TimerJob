package com.cg.schedule.service.migrator;

import com.cg.schedule.common.conf.MigratorAppConf;
import com.cg.schedule.enums.TimerStatus;
import com.cg.schedule.manager.MigratorManager;
import com.cg.schedule.mapper.TimerMapper;
import com.cg.schedule.model.TimerModel;
import com.cg.common.redis.ReentrantDistributeLock;
import com.cg.schedule.utils.TimerUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class MigratorWorker {

    @Autowired
    TimerMapper timerMapper;

    @Autowired
    MigratorAppConf migratorAppConf;

    @Autowired
    MigratorManager migratorManager;

    @Autowired
    ReentrantDistributeLock reentrantDistributeLock;

    @Scheduled(fixedRate = 60*60*1000) // 60*60*1000 一小时执行一次
    public void work() {
        log.info("开始迁移时间：" + LocalDateTime.now());
        String name = Thread.currentThread().getName();
        Date startHour = getStartHour(new Date());
        String lockToken = TimerUtils.GetTokenStr();
        boolean ok = reentrantDistributeLock.lock(
                TimerUtils.GetMigratorLockKey(startHour),
                lockToken,
                60L*migratorAppConf.getMigrateTryLockMinutes());
        if(!ok){
            log.warn("migrator get lock failed！"+TimerUtils.GetMigratorLockKey(startHour));
            return;
        }

        //迁移
        migrate();

        // 更新分布式锁过期时间
        reentrantDistributeLock.expireLock(
                TimerUtils.GetMigratorLockKey(startHour),
                lockToken,
                60L*migratorAppConf.getMigrateSuccessExpireMinutes());
    }

    private Date getStartHour(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH");
        try {
            return sdf.parse(sdf.format(date));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


    private void migrate(){
        List<TimerModel> timers= timerMapper.getTimersByStatus(TimerStatus.Enable.getStatus());
        if(CollectionUtils.isEmpty(timers)){
            log.info("migrate timers is empty");
            return;
        }

        for (TimerModel timerModel:timers) {
            migratorManager.migrateTimer(timerModel);
        }
    }
}

