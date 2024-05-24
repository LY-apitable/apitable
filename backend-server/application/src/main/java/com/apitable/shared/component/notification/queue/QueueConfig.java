package com.apitable.shared.component.notification.queue;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * queue config.
 */
@Configuration(proxyBeanMethods = false)
public class QueueConfig {

    /**
     * notification queue.
     */
    public static final String NOTIFICATION_QUEUE = "apitable.notification.queue";

    /**
     * notification route key.
     */
    public static final String NOTIFICATION_ROUTING_KEY = "notification.#";

    /**
     * notification exchange.
     */
    public static final String NOTIFICATION_EXCHANGE = "apitable.notification.exchange";

    /**
     * automation queue.
     */
    public static final String AUTOMATION_QUEUE = "apitable.automation.running";

    /**
     * automation route key.
     */
    public static final String AUTOMATION_ROUTING_KEY = "automation.running";

    /**
     * automation exchange.
     */
    public static final String AUTOMATION_EXCHANGE = "apitable.automation.exchange";

    /**
     * integration queue.
     */
    public static final String INTEGRATION_QUEUE = "apitable.integration.queue";

    /**
     * integration route key.
     */
    public static final String INTEGRATION_ROUTING_KEY = "integration.#";

    /**
     * integration exchange.
     */
    public static final String INTEGRATION_EXCHANGE = "apitable.integration.exchange";

    /**
     * notification queue.
     */
    @Bean("notificationQueue")
    public Queue notificationQueue() {
        return new Queue(NOTIFICATION_QUEUE);
    }

    /**
     * define notification exchange.
     */
    @Bean("notificationExchange")
    TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    /**
     * bind notification exchange and queue.
     */
    @Bean
    public Binding bindNotificationExchange(Queue notificationQueue,
                                            TopicExchange notificationExchange) {
        return BindingBuilder.bind(notificationQueue)
            .to(notificationExchange)
            .with(NOTIFICATION_ROUTING_KEY);

    }

    /**
     * automation queue.
     */
    @Bean("automationQueue")
    public Queue automationQueue() {
        return new Queue(AUTOMATION_QUEUE);
    }

    /**
     * define automation exchange.
     */
    @Bean("automationExchange")
    DirectExchange automationExchange() {
        return new DirectExchange(AUTOMATION_EXCHANGE);
    }

    /**
     * bind automation exchange and queue.
     */
    @Bean
    public Binding bindAutomationExchange(Queue automationQueue,
                                            DirectExchange automationExchange) {
        return BindingBuilder.bind(automationQueue)
            .to(automationExchange)
            .with(AUTOMATION_ROUTING_KEY);

    }

    /**
     * integration queue.
     */
    @Bean("integrationQueue")
    public Queue integrationQueue() {
        return new Queue(INTEGRATION_QUEUE);
    }

    /**
     * define integration exchange.
     */
    @Bean("integrationExchange")
    TopicExchange integrationExchange() {
        return new TopicExchange(INTEGRATION_EXCHANGE);
    }

    /**
     * bind integration exchange and queue.
     */
    @Bean
    public Binding bindIntegrationExchange(Queue integrationQueue,
                                            TopicExchange integrationExchange) {
        return BindingBuilder.bind(integrationQueue)
            .to(integrationExchange)
            .with(INTEGRATION_ROUTING_KEY);

    }
}
