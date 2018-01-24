package com.duangframework.rule.core;

import com.duangframework.core.exceptions.EmptyNullException;
import com.duangframework.core.exceptions.ServiceException;
import com.duangframework.core.kit.ToolsKit;
import com.duangframework.rule.entity.RuleParam;
import com.duangframework.rule.entity.RuleResult;
import org.drools.core.base.RuleNameEndsWithAgendaFilter;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Created by laotang
 * @date createed in 2018/1/24.
 */
public class RuleFactory {

    private static Logger logger = LoggerFactory.getLogger(RuleFactory.class);
    private static KieSessionHolder.Builder builder = null;

    public static void init(String ruleDir) {
        if(ToolsKit.isEmpty(builder)) {
            builder = new KieSessionHolder.Builder().ruleDir(ruleDir);
        }
    }

    public static RuleResult  execute(List<RuleParam> ruleParams) {
        if(ToolsKit.isEmpty(ruleParams)) {
            throw new EmptyNullException("ruleParams is null");
        }
        if(ToolsKit.isEmpty(builder)) {
            throw new EmptyNullException("KieSessionHolder.builder is null");
        }
        KieSession kieSession = builder.builder().kieSession();
        RuleResult ruleResult = new RuleResult(200, "success");
        try {
            for (RuleParam<?> ruleParam : ruleParams) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put(ruleParam.getKey(), ruleParam.getValue());
                kieSession.insert(paramMap);
                int ruleFiredCount = kieSession.fireAllRules(new RuleNameEndsWithAgendaFilter(ruleParam.getRuleName()));
                if (ruleFiredCount <= 0) {
                    throw new ServiceException("验证[" + ruleParam.getRuleName() + "]不通过");
                }
            }
        } catch (Exception e) {
            ruleResult.setCode(500);
            ruleResult.setMessage(e.getMessage());
            logger.warn(e.getMessage(), e);
        }
        kieSession.destroy();
        return ruleResult;
    }

}
