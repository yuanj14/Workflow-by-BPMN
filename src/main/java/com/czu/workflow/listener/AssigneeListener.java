package com.czu.workflow.listener;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

/**
 * 自定义TaskListener
 */
@Slf4j
public class AssigneeListener implements TaskListener {


    @Override
    public void notify(DelegateTask delegateTask) {
        log.info("AssigneeListener监听器触发");
        // 每个任务节点状态 开始✅ 运行❌ 结束❌ 所有节点的assignee都会被指定为demo
        if (EVENTNAME_CREATE.equals(delegateTask.getEventName())) {
            delegateTask.setAssignee("demo");
        }
    }
}
