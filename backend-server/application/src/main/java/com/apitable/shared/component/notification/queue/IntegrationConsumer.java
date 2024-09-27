/*
 * APITable <https://github.com/apitable/apitable>
 * Copyright (C) 2022 APITable Ltd. <https://apitable.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.apitable.shared.component.notification.queue;

import static com.apitable.shared.component.notification.queue.QueueConfig.INTEGRATION_QUEUE;

import com.apitable.integration.ro.IntegrationCreateRo;
import com.apitable.interfaces.social.event.IntegrationEvent;
import com.apitable.interfaces.social.facade.SocialServiceFacade;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Integration consumer.
 */
@Slf4j
@Component
public class IntegrationConsumer {

    @Resource
    private SocialServiceFacade socialServiceFacade;

    /**
     * Integration consumer.
     *
     * @param event   event
     * @param message message
     * @param channel channel
     * @throws IOException exception
     */
    @RabbitListener(queues = INTEGRATION_QUEUE)
    public void onMessageReceived(IntegrationCreateRo event, Message message, Channel channel)
        throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        log.info("Integration received deliveryTag:{}; type:{}", deliveryTag, event.getType());
        if (event.getType() == 0 || event.getType() == 1) {
            try {
                socialServiceFacade.eventCall(new IntegrationEvent(event));
            } catch (Exception e) {
                log.warn("Failed to process message: {}:{}:{}", event.getSpaceId(), event.getProcessCode(), event.getComponent(), e);
            }
        }
        channel.basicAck(deliveryTag, false);
    }
}
