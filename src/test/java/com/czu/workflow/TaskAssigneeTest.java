package com.czu.workflow;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;

@SpringBootTest(classes = Application.class)
public class TaskAssigneeTest {
    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;


    /**
     * 流程部署
     */
    @Test
    public void deployFlow() {
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("static/processResource/flow/任务分配-listener.bpmn")
                .name("任务分配-Listener部署")
                .deploy();
        System.out.println("deploy.getId() = " + deploy.getId());
        System.out.println("deploy.getName() = " + deploy.getName());
    }

    /**
     * 启动流程实例 BY KEY
     * 用于启动最新版本流程实例
     */
    @Test
    public void startProcessInstanceByKey() {
        // startProcessInstanceByKey() 方法：
        // - key: 对应 ACT_RE_PROCDEF 表的 KEY_ 字段（流程定义的key）
        // - 返回的 ProcessInstance 的 id: 对应 ACT_RU_EXECUTION 表的 ID_ 字段（流程实例ID）
        //   同时也对应 ACT_HI_PROCINST 表的 PROC_INST_ID_ 字段（历史流程实例ID）
        String processDefinitionKey = "Process_15zd19s";
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey);
        System.out.println("流程实例ID (ACT_RU_EXECUTION.ID_): " + processInstance.getId());
        System.out.println("流程定义KEY (ACT_RE_PROCDEF.KEY_): " + processInstance.getProcessDefinitionId());
    }

    /**
     * 启动流程实例 BY ID
     * 通过流程变量分配处理人
     * NEXT 状态变量需要指定
     */
    @Test
    public void startProcessInstanceByIdWithVariables() {
        String processDefinitionId = "Process_0a0q6t3:4:26ee1289-f692-11f0-821e-30560f01377c";
        HashMap<String, Object> variables = new HashMap<>();
        variables.put("user1", "demo");
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinitionId, variables);
        System.out.println("流程实例ID (ACT_RU_EXECUTION.ID_): " + processInstance.getId());
    }

    /**
     * 启动流程实例 BY ID
     * 用于启动旧版本流程实例
     */
    @Test
    public void startProcessInstanceById() {
        String processDefinitionId = "Process_0a0q6t3:1:42620b84-f600-11f0-a5d5-30560f01377c";
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinitionId);
        System.out.println("流程实例ID (ACT_RU_EXECUTION.ID_): " + processInstance.getId());
    }

    /**
     * 查询任务
     */
    @Test

    public void queryTask() {
        TaskQuery taskQuery = taskService.createTaskQuery();
        List<Task> tasks = taskQuery.taskAssignee("user1").list();
        for (Task task : tasks) {
            System.out.println("task.getId() = " + task.getId());
            System.out.println("task.getName() = " + task.getName());
            System.out.println("task.getAssignee() = " + task.getAssignee());
            System.out.println("task.getProcessInstanceId() = " + task.getProcessInstanceId());
        }
    }

    /**
     * 完成任务
     */
    @Test
    public void completeTask() {
        // taskId 对应的是 Camunda 数据库中的 ACT_RU_TASK 表的 ID_ 字段
        String taskId = "f105514c-f696-11f0-9388-30560f01377c";
        HashMap<String, Object> variables = new HashMap<>();
        variables.put("user2", "demo");
        taskService.complete(taskId, variables);
        System.out.println("审批完成，任务ID: " + taskId + " (对应表 ACT_RU_TASK 中的 ID_ 字段)");
    }
}

