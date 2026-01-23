package com.czu.workflow;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Slf4j
@SpringBootTest(classes = Application.class)
public class TenantTest {
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

    // ==================== 租户管理模块 ====================

    /**
     * 创建租户
     */
    @Test
    public void createTenant() {
        Tenant tenant = identityService.newTenant("tenant01");
        tenant.setName("深圳分公司");
        identityService.saveTenant(tenant);
        log.info("租户创建成功: ID={}, 名称={}", tenant.getId(), tenant.getName());
    }

    /**
     * 更新租户
     */
    @Test
    public void updateTenant() {
        Tenant tenant = identityService.createTenantQuery().tenantId("tenant01").singleResult();
        if (tenant != null) {
            tenant.setName("深圳分公司-更新");
            identityService.saveTenant(tenant);
            log.info("租户更新成功: ID={}, 新名称={}", tenant.getId(), tenant.getName());
        } else {
            log.warn("未找到租户: tenant01");
        }
    }

    /**
     * 删除租户
     */
    @Test
    public void deleteTenant() {
        String tenantId = "tenant01";
        identityService.deleteTenant(tenantId);
        log.info("租户删除成功: ID={}", tenantId);
    }

    /**
     * 根据租户ID查询单个租户
     */
    @Test
    public void queryTenantById() {
        String tenantId = "tenant01";
        Tenant tenant = identityService.createTenantQuery().tenantId(tenantId).singleResult();
        if (tenant != null) {
            log.info("=== 租户信息 ===");
            log.info("租户ID: {}", tenant.getId());
            log.info("租户名称: {}", tenant.getName());
        } else {
            log.warn("未找到租户: {}", tenantId);
        }
    }

    /**
     * 查询所有租户
     */
    @Test
    public void queryAllTenants() {
        List<Tenant> tenantList = identityService.createTenantQuery().list();
        log.info("查询到租户总数: {}", tenantList != null ? tenantList.size() : 0);
        if (tenantList != null && !tenantList.isEmpty()) {
            for (Tenant tenant : tenantList) {
                log.info("=== 租户信息 ===");
                log.info("租户ID: {}", tenant.getId());
                log.info("租户名称: {}", tenant.getName());
            }
        }
    }

    /**
     * 根据租户名称模糊查询
     */
    @Test
    public void queryTenantByName() {
        String nameLike = "%区域%";
        List<Tenant> tenantList = identityService.createTenantQuery()
                .tenantNameLike(nameLike)
                .list();
        log.info("根据名称 '{}' 查询到租户数: {}", nameLike, tenantList != null ? tenantList.size() : 0);
        if (tenantList != null && !tenantList.isEmpty()) {
            for (Tenant tenant : tenantList) {
                log.info("租户ID: {}, 租户名称: {}", tenant.getId(), tenant.getName());
            }
        }
    }

    // ==================== 租户与用户关联 ====================

    /**
     * 将用户添加到租户
     */
    @Test
    public void addUserToTenant() {
        String userId = "guodd";
        String tenantId = "tenant01";
        identityService.createTenantUserMembership(tenantId, userId);
        log.info("用户 {} 已添加到租户 {}", userId, tenantId);
    }

    /**
     * 将用户从租户中移除
     */
    @Test
    public void removeUserFromTenant() {
        String userId = "zhangsan";
        String tenantId = "tenant01";
        identityService.deleteTenantUserMembership(tenantId, userId);
        log.info("用户 {} 已从租户 {} 中移除", userId, tenantId);
    }

    /**
     * 查询租户下的所有用户
     */
    @Test
    public void queryUsersByTenant() {
        String tenantId = "tenant01";
        List<User> userList = identityService.createUserQuery()
                .memberOfTenant(tenantId)
                .list();
        log.info("租户 '{}' 下的用户数: {}", tenantId, userList != null ? userList.size() : 0);
        if (userList != null && !userList.isEmpty()) {
            for (User user : userList) {
                log.info("用户ID: {}, 姓名: {} {}", user.getId(), user.getFirstName(), user.getLastName());
            }
        }
    }

    /**
     * 查询用户所属的所有租户
     */
    @Test
    public void queryTenantsByUser() {
        String userId = "zhangsan";
        List<Tenant> tenantList = identityService.createTenantQuery()
                .userMember(userId)
                .list();
        log.info("用户 '{}' 所属租户数: {}", userId, tenantList != null ? tenantList.size() : 0);
        if (tenantList != null && !tenantList.isEmpty()) {
            for (Tenant tenant : tenantList) {
                log.info("租户ID: {}, 租户名称: {}", tenant.getId(), tenant.getName());
            }
        }
    }

    // ==================== 租户与分组关联 ====================

    /**
     * 将分组添加到租户
     */
    @Test
    public void addGroupToTenant() {
        String groupId = "jxGroup";
        String tenantId = "tenant01";
        identityService.createTenantGroupMembership(tenantId, groupId);
        log.info("分组 {} 已添加到租户 {}", groupId, tenantId);
    }

    /**
     * 将分组从租户中移除
     */
    @Test
    public void removeGroupFromTenant() {
        String groupId = "jxGroup";
        String tenantId = "tenant01";
        identityService.deleteTenantGroupMembership(tenantId, groupId);
        log.info("分组 {} 已从租户 {} 中移除", groupId, tenantId);
    }

    /**
     * 查询租户下的所有分组
     */
    @Test
    public void queryGroupsByTenant() {
        String tenantId = "tenant01";
        List<Group> groupList = identityService.createGroupQuery()
                .memberOfTenant(tenantId)
                .list();
        log.info("租户 '{}' 下的分组数: {}", tenantId, groupList != null ? groupList.size() : 0);
        if (groupList != null && !groupList.isEmpty()) {
            for (Group group : groupList) {
                log.info("分组ID: {}, 分组名称: {}, 类型: {}",
                        group.getId(), group.getName(), group.getType());
            }
        }
    }

    /**
     * 查询分组所属的所有租户
     */
    @Test
    public void queryTenantsByGroup() {
        String groupId = "jxGroup";
        List<Tenant> tenantList = identityService.createTenantQuery()
                .groupMember(groupId)
                .list();
        log.info("分组 '{}' 所属租户数: {}", groupId, tenantList != null ? tenantList.size() : 0);
        if (tenantList != null && !tenantList.isEmpty()) {
            for (Tenant tenant : tenantList) {
                log.info("租户ID: {}, 租户名称: {}", tenant.getId(), tenant.getName());
            }
        }
    }

    // ==================== 租户与流程定义关联 ====================

    /**
     * 部署流程到指定租户
     */
    @Test
    public void deployFlowWithTenant() {
        String tenantId = "tenant01";
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("static/processResource/flow/租户管理.bpmn")
                .name("租户流程部署")
                .tenantId(tenantId)
                .deploy();
        log.info("流程部署成功: ID={}, 名称={}, 租户ID={}",
                deploy.getId(), deploy.getName(), deploy.getTenantId());
    }

    /**
     * 查询租户下的所有流程定义
     */
    @Test
    public void queryProcessDefinitionsByTenant() {
        String tenantId = "tenant01";
        List<ProcessDefinition> processList = repositoryService.createProcessDefinitionQuery()
                .tenantIdIn(tenantId)
                .list();
        log.info("租户 '{}' 下的流程定义数: {}", tenantId, processList != null ? processList.size() : 0);
        if (processList != null && !processList.isEmpty()) {
            for (ProcessDefinition process : processList) {
                log.info("流程ID: {}, 流程Key: {}, 流程名称: {}, 版本: {}",
                        process.getId(), process.getKey(), process.getName(), process.getVersion());
            }
        }
    }

    /**
     * 查询公共的流程定义
     */
    @Test
    public void queryProcessDefinitionsWithoutTenant() {
        List<ProcessDefinition> processList = repositoryService.createProcessDefinitionQuery()
                .withoutTenantId()
                .list();
        log.info("没有租户的流程定义数: {}", processList != null ? processList.size() : 0);
        if (processList != null && !processList.isEmpty()) {
            for (ProcessDefinition process : processList) {
                log.info("流程ID: {}, 流程Key: {}, 流程名称: {}",
                        process.getId(), process.getKey(), process.getName());
            }
        }
    }

    // ==================== 租户流程启动示例 ====================

    /**
     * 通过流程定义KEY启动租户流程（推荐方式）
     */
    @Test
    public void startProcessByKeyWithTenant() {
        String processDefinitionKey = "Process_0a0q123";
        String tenantId = "tenant01";
        // 使用 startProcessInstanceByKey 方法启动指定租户的最新版本流程
        ProcessInstance processInstance = runtimeService.createProcessInstanceByKey(processDefinitionKey)
                .processDefinitionTenantId(tenantId)
                .execute();
    }

    // ==================== 租户隔离任务查询 ====================

    /**
     * 根据当前用户查询租户任务（带租户隔离）
     */
    @Test
    public void queryAllTasksByCurrentUser() {
        String userId = "guodd";
        // 1. 查询用户所属的所有租户
        List<Tenant> userTenants = identityService.createTenantQuery()
                .userMember(userId)
                .list();

        log.info("用户 '{}' 所属租户数: {}", userId, userTenants != null ? userTenants.size() : 0);

        // 2. 提取租户ID数组
        String[] tenantIds = null;
        if (userTenants != null && !userTenants.isEmpty()) {
            tenantIds = userTenants.stream()
                    .map(Tenant::getId)
                    .toArray(String[]::new);
        }

        // 3. 根据租户ID列表查询任务
        TaskQuery taskQuery = taskService.createTaskQuery();
        if (tenantIds != null && tenantIds.length > 0) {
            taskQuery.tenantIdIn(tenantIds);
        } else {
            // 如果用户不属于任何租户，可以根据业务逻辑决定是查询公共任务还是不查询
            taskQuery.withoutTenantId();
        }

        List<Task> taskList = taskQuery.list();
        log.info("查询到租户关联任务数: {}", taskList != null ? taskList.size() : 0);

        if (taskList != null && !taskList.isEmpty()) {
            for (Task task : taskList) {
                log.info("任务ID: {}, 任务名称: {}, 租户ID: {}", 
                        task.getId(), task.getName(), task.getTenantId());
            }
        }
    }
}
