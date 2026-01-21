package com.czu.workflow.listener;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

/**
 * 自定义TaskListener
 */
public class AssigneeListener implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        System.out.println("AssigneeListener监听器触发");
        if (EVENTNAME_CREATE.equals(delegateTask.getEventName())) {
            delegateTask.setAssignee("demo");
        }
    }
}
