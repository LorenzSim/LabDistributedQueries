package be.ucll.da.apigateway.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import be.ucll.da.apigateway.client.patient.model.PatientValidatedEvent;
import javax.transaction.Transactional;

@Component
@Transactional
public class MessageListener {
    private final static Logger LOGGER = LoggerFactory.getLogger(MessageListener.class);

    @RabbitListener(queues = {"q.patient-validated.api-gateway"})
    public void handlePatientValidatedEvent(PatientValidatedEvent event) {
        LOGGER.info("Patient validated: " + event);

    }

}
