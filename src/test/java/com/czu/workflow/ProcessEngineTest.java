package com.czu.workflow;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

//@SpringBootTest(classes = Application.class)
public class ProcessEngineTest {

    /**
     * 从代码中加载流程引擎
     */

    @Test
    public void processEngineCode() {
        ProcessEngine processEngine = new StandaloneProcessEngineConfiguration()
                .setJdbcUrl("jdbc:mysql://localhost:3306/camunda?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8")
                .setJdbcUsername("root")
                .setJdbcPassword("1234")
                .setJdbcDriver("com.mysql.cj.jdbc.Driver")
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE)
                .setHistory(ProcessEngineConfiguration.HISTORY_FULL)
                .setSkipIsolationLevelCheck(true)
                .buildProcessEngine();

        System.out.println("流程引擎: " + processEngine);
    }
}
