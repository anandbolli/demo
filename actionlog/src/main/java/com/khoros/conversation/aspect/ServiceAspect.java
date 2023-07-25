package com.khoros.conversation.aspect;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Log4j2
public class ServiceAspect {

    @Autowired
    RetryTemplate retryTemplate;


    @Around(value = "execution(* com.khoros.conversation.service.*.*(..)) && args(..)")
    public Object beforeAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] methodArgs = joinPoint.getArgs();

        log.info("Entering method " + methodName + " in class " + className);
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

        log.info("Exiting method " + methodName + " in class " + className );
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

    @AfterThrowing(value = "execution(* com.khoros.conversation.service.*.*(..)) and args(str1,str2)")
    public void afterAdviceThrowing(JoinPoint joinPoint, String str1, String str2) {
        log.error("Exception thrown from method:" + joinPoint.getSignature());

    }



    @Pointcut("execution(* com.khoros.conversation.service.Downloader.getActionLogExportDownloadURL(..)) && args(..)")
    public void serviceMethods() {

    }

    @Around("serviceMethods()")
    public Object aroundServiceMethods(ProceedingJoinPoint joinPoint) {
        try {
            log.info("Initializing Retry Aspect method");
            return retryTemplate.execute(retryContext -> joinPoint.proceed());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }




    @Around(value = "execution(* com.khoros.conversation.repo.*.*(..)) && args(..)")
    public Object aroundRepoMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] methodArgs = joinPoint.getArgs();

        log.info("Entering method " + methodName + " in class " + className);
        log.debug("Method arguments: " + getArgumentsAsString(methodArgs));

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            log.error("Exception occurred in method " + methodName + ": " + e.getMessage());
            throw e;
        }

        log.info("Exiting method " + methodName + " in class " + className );
        return result;

    }

    @AfterThrowing(value = "execution(* com.khoros.conversation.repo.*.*(..)) and args(str1,str2)")
    public void exceptionRepoMethods(JoinPoint joinPoint, String str1, String str2) {
        log.error("Exception thrown from method:" + joinPoint.getSignature());

    }

}
