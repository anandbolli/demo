package com.khoros.conversation.config;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.classify.Classifier;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;


@Log4j2
@NoArgsConstructor
public class CustomRetryPolicy extends ExceptionClassifierRetryPolicy {

    public CustomRetryPolicy(int count) {
        final SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy();
        simpleRetryPolicy.setMaxAttempts(count);
        this.setExceptionClassifier(new Classifier<Throwable, RetryPolicy>() {
            @Override
            public RetryPolicy classify(Throwable classifiable) {
                log.info("Entered Custom RetryPolicy ");
                if (classifiable instanceof UnAvailableException) {
                    log.info("Filtered UnAvailableException ");
                    return simpleRetryPolicy;
                }
                return new NeverRetryPolicy();
            }
        });
    }
}