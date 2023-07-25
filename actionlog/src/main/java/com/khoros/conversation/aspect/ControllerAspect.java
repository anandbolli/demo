package com.khoros.conversation.aspect;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Log4j2
@Component
public class ControllerAspect {

    @Around(value = "execution(* com.khoros.conversation.controller.*.*(..)) && args(..)")
    public Object beforeAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] methodArgs = joinPoint.getArgs();

        log.info("Entering method " + methodName + " in Controller " + className);
        log.debug("Method arguments: " + getArgumentsAsString(methodArgs));
        Object result;
        try {
            // Proceed with the original method execution
            result = joinPoint.proceed();
        } catch (Throwable e) {
            // Handle any exceptions that occur during the method execution
            log.error("Exception occurred in method " + methodName + ": " + e.getMessage());
            throw e;
        }

        log.debug("Exiting method " + methodName + " in Controller ");
        return result;

    }

    private String getArgumentsAsString(Object[] args) {
        StringBuilder sb = new StringBuilder();
        if (args != null && args.length > 0) {
            for (Object arg : args) {
                sb.append(arg).append(", ");
            }
            sb.delete(sb.length() - 2, sb.length());  // Remove trailing comma and space
        }
        return sb.toString();
    }


}
