package be.ucll.da.apigateway.messaging;

import be.ucll.da.apigateway.domain.CQRSHospitalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.transaction.Transactional;
import be.ucll.da.apigateway.client.patient.model.PatientValidatedEvent;
import be.ucll.da.apigateway.client.doctor.model.DoctorsOnPayrollEvent;
import be.ucll.da.apigateway.client.appointment.model.AppointmentFinalizedEvent;

@Component
@Transactional
public class MessageListener {
    private final static Logger LOGGER = LoggerFactory.getLogger(MessageListener.class);

    private final CQRSHospitalService hospitalService;

    @Autowired
    public MessageListener(CQRSHospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }

    @RabbitListener(queues = {"q.patient-validated.api-gateway"})
    public void onPatientValidatedEvent(PatientValidatedEvent event) {
        LOGGER.info("Received patient validated event: " + event);
        if (Boolean.TRUE.equals(event.getIsClient())){
            hospitalService.savePatient(event);
        }
    }

    @RabbitListener(queues = {"q.doctors-employed.api-gateway"})
    public void onDoctorsEmployedEvent(DoctorsOnPayrollEvent event) {
        LOGGER.info("Received doctors on payroll event: " + event);
        hospitalService.saveDoctorsOnPayroll(event);
    }

    @RabbitListener(queues = {"q.appointment-finalized.api-gateway"})
    public void onAppointmentFinalizedEvent(AppointmentFinalizedEvent event) {
        LOGGER.info("Received appointment finalized: " + event);
        if (Boolean.TRUE.equals(event.getAccepted())) {
            hospitalService.saveFinalizedAppointment(event);
        }
    }
}
