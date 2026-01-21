package com.czu.workflow;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest(classes = Application.class)
public class FlowVariablesTest {
    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    /**
     * 流程部署
     */
    @Test
    public void deployFlow() {
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("static/processResource/flow/任务分配-Assignee.bpmn")
                .name("任务分配-Assignee")
                .deploy();
        System.out.println("deploy.getId() = " + deploy.getId());
        System.out.println("deploy.getName() = " + deploy.getName());
    }

    /**
     * 发起流程实例 by 流程ID
     * 添加流程变量
     */
    @Test
    public void startProcessById() {
        String processInstanceId = "Process_0a0q999:1:357c8272-f6ae-11f0-9e1f-581031f89bd6";
        HashMap<String, Object> variables = new HashMap<>();
        variables.put("user1", "wowo");
        variables.put("user2", 23);
        variables.put("user3", false);
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(processInstanceId, variables);
        System.out.println("processInstance.getId() = " + processInstance.getId());
    }

    /**
     * 查看流程变量
     */
    @Test
    public void getProcessVariables() {
        // 流程实例ID(act_ru_task) 或执行ID(act_ru_execution)
        String executionId = "53a96c95-f6ae-11f0-8212-581031f89bd6";
        Map<String, Object> variables = runtimeService.getVariables(executionId);
        variables.forEach((k, v) -> {
            log.info("Key: " + k + ", Value: " + v);
        });
    }

    /**
     * 设置流程变量 redis setNX
     */
    @Test
    public void setProcessVariables() {
        String executionId = "53a96c95-f6ae-11f0-8212-581031f89bd6";
        Map<String, Object> variables = new HashMap<>();
        variables.put("count", 1);
        variables.put("count2", 2);
        runtimeService.setVariables(executionId, variables);
        log.info("设置流程变量，执行ID: {}", executionId);
    }

    /**
     * 完成任务
     */
    @Test
    public void completeTask() {
        // taskId 对应的是 Camunda 数据库中的 ACT_RU_TASK 表的 ID_ 字段
        String taskId = "53b7012e-f6ae-11f0-8212-581031f89bd6";
        taskService.complete(taskId);
        log.info("审批完成，任务ID: {} (对应表 ACT_RU_TASK 中的 ID_ 字段)", taskId);
    }

    /**
     * 查看变量日志
     */
    @Test
    public void historyDetails() {
        // 需要指定具体的流程实例ID
        String processInstanceId = "53a96c95-f6ae-11f0-8212-581031f89bd6";
        
        // 查询历史变量详情
        List<HistoricDetail> list = historyService.createHistoricDetailQuery()
                .processInstanceId(processInstanceId)
                .list();
        
        log.info("查询到历史详情记录数: {}", list != null ? list.size() : 0);
        
        if(list != null && !list.isEmpty()) {
            list.forEach(historicDetail -> {
                log.info("=== 历史详情记录 ===");
                log.info("ID: {}", historicDetail.getId());
                log.info("流程实例ID: {}", historicDetail.getProcessInstanceId());
                log.info("执行ID: {}", historicDetail.getExecutionId());
                log.info("活动实例ID: {}", historicDetail.getActivityInstanceId());
                log.info("任务ID: {}", historicDetail.getTaskId());
                log.info("时间: {}", historicDetail.getTime());
                
                if(historicDetail instanceof HistoricVariableUpdate variableUpdate) {
                    log.info("变量名: {}", variableUpdate.getVariableName());
                    log.info("变量值: {}", variableUpdate.getValue());
                }
            });
        }
    }

    /**
     * 查询变量最终快照
     */
    @Test
    public void historicVariables() {
        // 需要指定具体的流程实例ID
        String processInstanceId = "53a96c95-f6ae-11f0-8212-581031f89bd6";
        
        // 查询历史变量实例
        List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(processInstanceId)
                .list();
        
        log.info("查询到历史变量记录数: {}", variables != null ? variables.size() : 0);
        
        if(variables != null && !variables.isEmpty()) {
            variables.forEach(variable -> {
                log.info("=== 历史变量记录 ===");
                log.info("变量ID: {}", variable.getId());
                log.info("变量名: {}", variable.getName());
                log.info("变量值: {}", variable.getValue());
                log.info("变量类型: {}", variable.getTypeName());
                log.info("创建时间: {}", variable.getCreateTime());
            });
        }
    }
}
