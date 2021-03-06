package xyz.rpolnx.spring_bank.account.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import xyz.rpolnx.spring_bank.account.model.enums.CustomerEventHandler;
import xyz.rpolnx.spring_bank.account.service.AccountService;
import xyz.rpolnx.spring_bank.common.model.dto.CustomerEvent;

import static java.util.Objects.isNull;

@Component
@RequiredArgsConstructor
@Log4j2
public class CustomerListener {
    private final AccountService service;

    @RabbitListener(queues = "${customer-queue-name}")
    public void receive(@Payload final CustomerEvent event) {
        log.info("Consuming message from customer queue: {}", event);

        CustomerEventHandler customerEventHandler = CustomerEventHandler.fromEventType(event.getType());

        if (isNull(customerEventHandler)) {
            log.error("Unknown event type for message: {}", event);
            return;
        }

        customerEventHandler.getCallable().accept(service, event);

        log.info("Finalize message consume from customer queue: {}", event.getCustomer().getId());
    }
}
