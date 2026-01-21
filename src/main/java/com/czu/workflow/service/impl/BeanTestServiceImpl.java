package com.czu.workflow.service.impl;

import com.czu.workflow.service.BeanTestService;
import org.springframework.stereotype.Service;

@Service("BeanTestService")
public class BeanTestServiceImpl implements BeanTestService {
    /**
     * 通过Bean调用获取处理人
     *
     * @return
     */
    @Override
    public String getAssignee() {
        System.out.println("Bean调用获取处理人");
        return "demo";
    }
}
