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

import java.util.List;

@SpringBootTest
public class ApplicationTest {
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
                .addClasspathResource("static/processResource/process.bpmn")
                .name("部署2th")
                .deploy();
        System.out.println("deploy.getId() = " + deploy.getId());
        System.out.println("deploy.getName() = " + deploy.getName());
    }

    /**
     * 发起流程实例 by 流程ID
     */
    @Test
    public void startProcessById() {
        String processInstanceId = "Camunda-demo-process:1:f0abbf9a-ef68-11f0-ac65-581031f89bd6";
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(processInstanceId);
        System.out.println("processInstance.getId() = " + processInstance.getId());
    }

    /**
     * 发起流程实例 by 流程KEY
     */
    @Test
    public void startProcessByKey() {
        String processInstanceKey = "Camunda-demo-process";
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processInstanceKey);
        System.out.println("processInstance.getId() = " + processInstance.getId());
    }

    /**
     * 任务查询query
     */
    @Test
    public void queryTasks() {
        TaskQuery taskQuery = taskService.createTaskQuery();
        List<Task> tasks = taskQuery.taskCandidateUser("demo").list();
        for (Task task : tasks) {
            String taskId = task.getId();
            System.out.println("taskId = " + taskId);
        }
    }

    /**
     * 任务审批
     */
    @Test
    public void completeTask() {
        // taskId 对应的是 Camunda 数据库中的 ACT_RU_TASK 表的 ID_ 字段
        String taskId = "b347893f-f603-11f0-b87d-30560f01377c";
        taskService.complete(taskId);
        System.out.println("审批完成，任务ID: " + taskId + " (对应表 ACT_RU_TASK 中的 ID_ 字段)");
    }
}
