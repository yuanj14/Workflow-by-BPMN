package com.czu.workflow.listener;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import java.util.Map;

@Slf4j
public class VariablesListener implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        // 获取全流程变量
        Map<String, Object> variables = delegateTask.getVariables();
        variables.forEach((k, v) -> {
            log.info("Key: {}, Value: {}", k, v);
            if (v instanceof String) {
                delegateTask.setVariable(k, v + "Camunda");
            }
        });
    }
}
