package com.czu.workflow;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest(classes = Application.class)
public class CandidateTest {
    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private IdentityService identityService;

    /**
     * 流程部署
     */
    @Test
    public void deployFlow() {
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("static/processResource/flow/候选人分配任务.bpmn")
                .name("候选人分配任务-01")
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
        String processInstanceId = "Process_1c6zsko:1:c6707e89-f754-11f0-83ff-581031f89bd6";
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(processInstanceId);
        System.out.println("processInstance.getId() = " + processInstance.getId());
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
     * 查询任务的所有候选人
     */
    @Test
    public void getTaskCandidateUser() {
        String taskId = "e38d5869-f754-11f0-93d9-581031f89bd6";
        List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);
        if (linksForTask != null && !linksForTask.isEmpty()) {
            for (IdentityLink link : linksForTask) {
                System.out.println("link.getUserId() = " + link.getUserId());
            }
        }
    }


    /**
     * 查询候选人的所有可拾取任务
     */
    @Test
    public void getUserCandidateTask() {
        List<Task> tasks = taskService.createTaskQuery()
                .taskCandidateUser("Demo")
                .list();
        if (tasks != null && !tasks.isEmpty()) {
            for (Task task : tasks) {
                System.out.println("task.getId() = " + task.getId());
                System.out.println("task.getName() = " + task.getName());
            }
        } else {
            System.out.println("当前登录用户没有候选的Task");
        }
    }

    /**
     * 根据候选人组查询所有任务
     */
    @Test
    public void getTasksByCandidateGroup() {
        String candidateGroup = "jxGroup";
        
        // 1. 通过 taskCandidateGroup 查询分配给指定候选组的所有任务
        List<Task> tasks = taskService.createTaskQuery()
                .taskCandidateGroup(candidateGroup)
                .list();
        
        log.info("候选人组 '{}' 的任务总数: {}", candidateGroup, tasks != null ? tasks.size() : 0);
        
        // 2. 遍历任务列表，输出任务详情
        if (tasks != null && !tasks.isEmpty()) {
            for (Task task : tasks) {
                log.info("=== 任务信息 ===");
                log.info("任务ID: {}", task.getId());
                log.info("任务名称: {}", task.getName());
                log.info("流程实例ID: {}", task.getProcessInstanceId());
                log.info("当前执行人: {}", task.getAssignee() != null ? task.getAssignee() : "未分配");
                log.info("创建时间: {}", task.getCreateTime());
                
                // 3. 查询该任务的所有候选组（一个任务可能分配给多个组）
                List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(task.getId());
                if (identityLinks != null && !identityLinks.isEmpty()) {
                    for (IdentityLink link : identityLinks) {
                        if (link.getGroupId() != null) {
                            log.info("候选组: {}, 类型: {}", link.getGroupId(), link.getType());
                        }
                    }
                }
            }
        } else {
            log.info("候选人组 '{}' 当前没有待处理的任务", candidateGroup);
        }
    }

    /**
     * 查询用户所在组的所有可拾取任务
     */
    @Test
    public void getTasksByUserGroups() {
        String userId = "wowo";
        
        // 1. 先查询用户所属的所有分组
        List<Group> groups = identityService.createGroupQuery()
                .groupMember(userId)
                .list();
        
        log.info("用户 '{}' 所属分组数: {}", userId, groups != null ? groups.size() : 0);
        
        if (groups != null && !groups.isEmpty()) {
            // 2. 遍历用户所属的每个分组，查询该组的任务
            for (Group group : groups) {
                log.info("--- 查询分组 '{}' 的任务 ---", group.getName());
                
                List<Task> tasks = taskService.createTaskQuery()
                        .taskCandidateGroup(group.getId())
                        .list();
                
                log.info("分组 '{}' 的任务数: {}", group.getName(), tasks != null ? tasks.size() : 0);
                
                if (tasks != null && !tasks.isEmpty()) {
                    for (Task task : tasks) {
                        log.info("任务ID: {}, 任务名称: {}, 当前执行人: {}", 
                            task.getId(), 
                            task.getName(), 
                            task.getAssignee() != null ? task.getAssignee() : "未分配");
                    }
                }
            }
        } else {
            log.warn("用户 '{}' 未加入任何分组", userId);
        }
    }

    /**
     * 候选组成员拾取任务
     * Camunda 7 自动维护组成员权限，只有组成员才能拾取该组的任务
     */
    @Test
    public void claimTaskByGroupMember() {
        String userId = "wowo";
        
        // 1. 查询用户所属的所有分组
        List<org.camunda.bpm.engine.identity.Group> userGroups = identityService.createGroupQuery()
                .groupMember(userId)
                .list();
        
        log.info("用户 '{}' 所属分组数: {}", userId, userGroups != null ? userGroups.size() : 0);
        
        if (userGroups == null || userGroups.isEmpty()) {
            log.warn("用户 '{}' 未加入任何分组，无法拾取组任务", userId);
            return;
        }
        
        // 2. 遍历用户所属的每个分组，查询可拾取的任务
        boolean taskClaimed = false;
        for (Group group : userGroups) {
            log.info("\n--- 检查分组 '{}' (ID: {}) 的可拾取任务 ---", group.getName(), group.getId());
            
            // 3. 查询该分组的未分配任务
            List<Task> tasks = taskService.createTaskQuery()
                    .taskCandidateGroup(group.getId())
                    .taskUnassigned()  // 只查询未分配执行人的任务
                    .list();

            log.info("分组 '{}' 的待拾取任务数: {}", group.getName(), tasks != null ? tasks.size() : 0);

            if (tasks != null && !tasks.isEmpty()) {
                // 4. 拾取该分组的第一个任务
                Task task = tasks.get(0);
                
                // Camunda 7 会自动验证 userId 是否属于该 candidateGroup
                // 如果不属于会抛出异常，但由于我们是从用户所属组中查询的，所以一定有权限
                taskService.claim(task.getId(), userId);
                
                log.info("✓ 任务拾取成功！");
                log.info("  任务ID: {}", task.getId());
                log.info("  任务名称: {}", task.getName());
                log.info("  拾取人: {}", userId);
                log.info("  所属候选组: {} ({})", group.getName(), group.getId());
                log.info("  流程实例ID: {}", task.getProcessInstanceId());
                
                taskClaimed = true;
                break;  // 拾取一个任务后退出循环
            }
        }
        
        // 5. 如果所有分组都没有可拾取的任务
        if (!taskClaimed) {
            log.info("\n用户 '{}' 所属的所有分组当前都没有可拾取的任务", userId);
        }
    }

    /**
     * 候选组成员查看所有可拾取的任务
     */
    @Test
    public void viewAllClaimableTasks() {
        String userId = "wowo";
        
        // 1. 查询用户所属的所有分组
        List<org.camunda.bpm.engine.identity.Group> userGroups = identityService.createGroupQuery()
                .groupMember(userId)
                .list();
        
        log.info("用户 '{}' 所属分组数: {}", userId, userGroups != null ? userGroups.size() : 0);
        
        if (userGroups == null || userGroups.isEmpty()) {
            log.warn("用户 '{}' 未加入任何分组", userId);
            return;
        }
        
        int totalTasks = 0;
        
        // 2. 遍历每个分组，查询可拾取的任务
        for (org.camunda.bpm.engine.identity.Group group : userGroups) {
            log.info("\n=== 分组: {} (ID: {}, 类型: {}) ===", group.getName(), group.getId(), group.getType());
            
            List<Task> tasks = taskService.createTaskQuery()
                    .taskCandidateGroup(group.getId())
                    .taskUnassigned()
                    .list();
            
            if (tasks != null && !tasks.isEmpty()) {
                log.info("可拾取任务数: {}", tasks.size());
                totalTasks += tasks.size();
                
                for (Task task : tasks) {
                    log.info("  - 任务ID: {}, 任务名称: {}, 创建时间: {}", 
                        task.getId(), task.getName(), task.getCreateTime());
                }
            } else {
                log.info("该分组暂无可拾取任务");
            }
        }
        
        log.info("\n总计: 用户 '{}' 在所有分组中共有 {} 个可拾取任务", userId, totalTasks);
    }

    /**
     * 候选人拾取任务
     */
    @Test
    public void claimTask() {
        String userId = "demo";
        // 1. 先查询任务是否存在，并且确认该任务是否可以被该用户拾取
        List<Task> tasks = taskService.createTaskQuery()
                .taskCandidateUser(userId)
                .list();

        if (tasks != null && !tasks.isEmpty()) {
            // 2. 只有查询到任务，才进行拾取操作
            taskService.claim(tasks.get(0).getId(), userId);
            log.info("任务拾取成功，任务ID: {}，拾取人: {}", tasks.get(0).getId(), userId);
        } else {
            // 3. 如果任务不存在或者不属于该候选人，给出提示
            log.info("任务拾取失败：未找到候选人为 {} 的任务，或者任务已被他人拾取", userId);
        }
    }

    /**
     * 候选人归还任务
     */
    @Test
    public void returnTask() {
        Task task = taskService.createTaskQuery()
                .taskAssignee("demo")
                .singleResult();
        if (task != null) {
            taskService.claim(task.getId(), null);
            log.info("任务归还成功：执行人为 {} 的任务", "demo");
        } else {
            log.info("任务归还失败：未找到执行人为 {} 的任务", "demo");
        }
    }

    /**
     * 获取UserTask指定变量,作为流程变量传递给下一节点
     */
    @Test
    public void getVariablesFromIdentityLink() {
        String taskId = "e38d5869-f754-11f0-93d9-581031f89bd6";

        // 1. 从 IdentityLink 获取候选人的 userId
        List<IdentityLink> linksForTask = taskService.getIdentityLinksForTask(taskId);

        if (linksForTask != null && !linksForTask.isEmpty()) {
            // 2. 准备流程变量 Map，用于下一个节点的值表达式 ${user1}, ${user2}, ${user3}
            Map<String, Object> variables = new HashMap<>();

            // 3. 遍历 IdentityLink，将 userId 读取到流程变量中
            int index = 1;
            for (IdentityLink link : linksForTask) {
                String userId = link.getUserId();
                String variableKey = "user" + index++;
                variables.put(variableKey, userId);
                log.info("{}: {}", variableKey, userId);
            }

            // 4. 通过 complete 设置流程变量并完成任务，供下一个节点的值表达式使用
            taskService.complete(taskId, variables);
            log.info("任务完成，流程变量已设置.");
        } else {
            log.info("未找到任务 {} 的候选人", taskId);
        }
    }
}


