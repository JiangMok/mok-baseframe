package com.mok.baseframe.ratelimiter.strategy;

import com.mok.baseframe.ratelimiter.core.RateLimitStrategy;
import com.mok.baseframe.ratelimiter.enums.RateLimitType;
import com.mok.baseframe.utils.LogUtils;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 限流策略工厂
 */
@Component
public class RateLimitStrategyFactory implements ApplicationContextAware {
    private static final Logger log = LogUtils.getLogger(RateLimitStrategyFactory.class);

    private final Map<String, RateLimitStrategy> strategyMap = new ConcurrentHashMap<>();

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        initializeStrategies();
    }

    private void initializeStrategies() {
        Map<String, RateLimitStrategy> strategies = applicationContext.getBeansOfType(RateLimitStrategy.class);
        for (RateLimitStrategy strategy : strategies.values()) {
            strategyMap.put(strategy.getType(), strategy);
        }
    }

    public RateLimitStrategy getStrategy(RateLimitType type) {

        if (type == null) {
            return getDefaultStrategy();
        }

        RateLimitStrategy strategy = strategyMap.get(type.getValue());
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的限流类型: " + type);
        }
        return strategy;
    }

    public RateLimitStrategy getStrategy(String type) {
        RateLimitStrategy strategy = strategyMap.get(type);
        if (strategy == null) {
            return getDefaultStrategy();
        }
        return strategy;
    }

    private RateLimitStrategy getDefaultStrategy() {
        return strategyMap.get(RateLimitType.SLIDING_WINDOW.getValue());
    }
}