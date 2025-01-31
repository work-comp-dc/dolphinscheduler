/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.server.master.runner.operator;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.server.master.runner.execute.DefaultTaskExecuteRunnable;

import java.util.Date;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaskTimeoutOperator implements TaskOperator {

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    @Override
    public void handle(DefaultTaskExecuteRunnable taskExecuteRunnable) {
        // Right now, if the task is running in worker, the timeout strategy will be handled at worker side.
        // if the task is in master, the timeout strategy will be handled at master side.
        // todo: we should unify this, the master only need to handle the timeout strategy. and send request to worker
        // to kill the task, if the strategy is timeout_failed.
        TaskInstance taskInstance = taskExecuteRunnable.getTaskInstance();
        TaskTimeoutStrategy taskTimeoutStrategy = taskInstance.getTaskDefine().getTimeoutNotifyStrategy();
        if (TaskTimeoutStrategy.FAILED != taskTimeoutStrategy
                && TaskTimeoutStrategy.WARNFAILED != taskTimeoutStrategy) {
            log.warn("TaskInstance: {} timeout, the current timeout strategy is {}, will continue running",
                    taskInstance.getName(), taskTimeoutStrategy.name());
            return;
        }
        log.info("TaskInstance: {} timeout, will kill the task instance", taskInstance.getName());
        taskInstance.setState(TaskExecutionStatus.FAILURE);
        taskInstance.setEndTime(new Date());
        taskInstanceDao.upsertTaskInstance(taskInstance);
    }
}
