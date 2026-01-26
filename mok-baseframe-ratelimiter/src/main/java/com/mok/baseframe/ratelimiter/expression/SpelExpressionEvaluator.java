package com.mok.baseframe.ratelimiter.expression;

import org.aspectj.lang.JoinPoint;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * SpEL表达式解析器
 */
@Component
public class SpelExpressionEvaluator {
    
    private final ExpressionParser parser = new SpelExpressionParser();
    
    /**
     * 解析SpEL表达式
     */
    public String evaluate(String expression, JoinPoint joinPoint) {
        try {
            StandardEvaluationContext context = new StandardEvaluationContext();
            
            // 设置参数
            Object[] args = joinPoint.getArgs();
            Method method = ((org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature()).getMethod();
            String[] paramNames = ((org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature()).getParameterNames();
            
            for (int i = 0; i < args.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
            
            Expression exp = parser.parseExpression(expression);
            Object value = exp.getValue(context);
            
            return value != null ? value.toString() : "";
        } catch (Exception e) {
            return expression;
        }
    }
}