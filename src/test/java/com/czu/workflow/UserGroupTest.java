package com.czu.workflow;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
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
public class UserGroupTest {
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
     * 创建分组
     * tips: 分组命名不能包含特殊字符 - _ !
     */
    @Test
    public void creatGroup() {
        Group gp = identityService.newGroup("jxGroup3");
        gp.setName("Activity教学组");
        gp.setType("admin-department");
        identityService.saveGroup(gp);
    }

    /**
     * 更新分组
     */
    @Test
    public void updateGroup() {
        Group jxGroup = identityService.createGroupQuery().groupId("jxGroup").singleResult();
        jxGroup.setName("Camunda教学组");
        identityService.saveGroup(jxGroup);
    }

    /**
     * 删除分组
     */
    @Test
    public void deleteGroup() {
        identityService.deleteGroup("jxGroup");
    }

    /**
     * 查询所有分组
     */
    @Test
    public void queryAllGroups() {
        List<Group> groups = identityService.createGroupQuery().list();
        if (groups != null && !groups.isEmpty()){
            for (Group group : groups) {
                log.info("group.getName() = " + group.getName());
            }
        }
    }

    // ==================== 用户管理模块 ====================

    /**
     * 创建用户
     */
    @Test
    public void createUser() {
        User user = identityService.newUser("zhangsan");
        user.setFirstName("张");
        user.setLastName("三");
        user.setEmail("zhangsan@example.com");
        user.setPassword("123456");
        identityService.saveUser(user);
        log.info("用户创建成功: ID={}, 姓名={} {}", user.getId(), user.getFirstName(), user.getLastName());
    }

    /**
     * 更新用户
     */
    @Test
    public void updateUser() {
        User user = identityService.createUserQuery().userId("wowo").singleResult();
        if (user != null) {
            user.setPassword("wowo");
            identityService.saveUser(user);
            log.info("用户更新成功: ID={}, 邮箱={}, 姓名={} {}", user.getId(), user.getEmail(), user.getFirstName(), user.getLastName());
        } else {
            log.warn("未找到用户: zhangsan");
        }
    }

    /**
     * 删除用户
     */
    @Test
    public void deleteUser() {
        String userId = "zhangsan";
        identityService.deleteUser(userId);
        log.info("用户删除成功: ID={}", userId);
    }

    /**
     * 根据用户ID查询单个用户
     */
    @Test
    public void queryUserById() {
        String userId = "zhangsan";
        User user = identityService.createUserQuery().userId(userId).singleResult();
        if (user != null) {
            log.info("=== 用户信息 ===");
            log.info("用户ID: {}", user.getId());
            log.info("姓名: {} {}", user.getFirstName(), user.getLastName());
            log.info("邮箱: {}", user.getEmail());
        } else {
            log.warn("未找到用户: {}", userId);
        }
    }

    /**
     * 查询所有用户
     */
    @Test
    public void queryAllUsers() {
        List<User> userList = identityService.createUserQuery().list();
        log.info("查询到用户总数: {}", userList != null ? userList.size() : 0);
        if (userList != null && !userList.isEmpty()) {
            for (User user : userList) {
                log.info("=== 用户信息 ===");
                log.info("用户ID: {}", user.getId());
                log.info("姓名: {} {}", user.getFirstName(), user.getLastName());
                log.info("邮箱: {}", user.getEmail());
            }
        }
    }

    /**
     * 根据邮箱模糊查询用户
     */
    @Test
    public void queryUserByEmail() {
        String emailLike = "%@example.com";
        List<User> userList = identityService.createUserQuery()
                .userEmailLike(emailLike)
                .list();
        log.info("根据邮箱 '{}' 查询到用户数: {}", emailLike, userList != null ? userList.size() : 0);
        if (userList != null && !userList.isEmpty()) {
            for (User user : userList) {
                log.info("用户ID: {}, 姓名: {} {}, 邮箱: {}", 
                    user.getId(), user.getFirstName(), user.getLastName(), user.getEmail());
            }
        }
    }

    /**
     * 将用户添加到分组
     */
    @Test
    public void addUserToGroup() {
        String userId = "wowo";
        String groupId = "jxGroup";
        identityService.createMembership(userId, groupId);
        log.info("用户 {} 已添加到分组 {}", userId, groupId);
    }

    /**
     * 将用户从分组中移除
     */
    @Test
    public void removeUserFromGroup() {
        String userId = "zhangsan";
        String groupId = "jxGroup";
        identityService.deleteMembership(userId, groupId);
        log.info("用户 {} 已从分组 {} 中移除", userId, groupId);
    }

    /**
     * 查询分组下的所有用户
     */
    @Test
    public void queryUsersByGroup() {
        String groupId = "jxGroup";
        List<User> userList = identityService.createUserQuery()
                .memberOfGroup(groupId)
                .list();
        log.info("分组 '{}' 下的用户数: {}", groupId, userList != null ? userList.size() : 0);
        if (userList != null && !userList.isEmpty()) {
            for (User user : userList) {
                log.info("用户ID: {}, 姓名: {} {}", user.getId(), user.getFirstName(), user.getLastName());
            }
        }
    }

    /**
     * 查询用户所属的所有分组
     */
    @Test
    public void queryGroupsByUser() {
        String userId = "zhangsan";
        List<Group> groupList = identityService.createGroupQuery()
                .groupMember(userId)
                .list();
        log.info("用户 '{}' 所属分组数: {}", userId, groupList != null ? groupList.size() : 0);
        if (groupList != null && !groupList.isEmpty()) {
            for (Group group : groupList) {
                log.info("分组ID: {}, 分组名: {}, 类型: {}", 
                    group.getId(), group.getName(), group.getType());
            }
        }
    }
}


