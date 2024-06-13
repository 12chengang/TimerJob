package com.cg.schedule.mapper;


import com.cg.schedule.model.TaskModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskMapper {

    /**
     * 批量插入taskModel
     *
     * @param taskList
     */
    void batchSave(@Param("taskList") List<TaskModel> taskList);

    /**
     * 根据timerId删除taskModel
     *
     * @param taskId
     */
    void deleteById(@Param("taskId") Long taskId);

    /**
     * 更新TimerModel
     *
     * @param taskModel
     */
    void update(@Param("taskModel") TaskModel taskModel);

    /**
     * 根据taskId查询Task
     *
     * @param startTime
     * @param endTime
     * @param taskStatus
     * @return TaskModel
     */
    List<TaskModel> getTasksByTimeRange(@Param("startTime") Long startTime, @Param("endTime") Long endTime, @Param("taskStatus") int taskStatus);

    /**
     * 根据taskId查询Task
     *
     * @param timerId
     * @param runTimer
     * @return TaskModel
     */
    TaskModel getTasksByTimerIdUnix(@Param("timerId") Long timerId, @Param("runTimer") Long runTimer);

}
