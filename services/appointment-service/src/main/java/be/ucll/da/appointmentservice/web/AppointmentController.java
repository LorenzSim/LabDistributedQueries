package be.ucll.da.appointmentservice.web;

import be.ucll.da.appointmentservice.api.AppointmentApiDelegate;
import be.ucll.da.appointmentservice.api.model.*;
import be.ucll.da.appointmentservice.domain.appointment.Appointment;
import be.ucll.da.appointmentservice.domain.appointment.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class AppointmentController implements AppointmentApiDelegate {

    private final AppointmentService appointmentService;

    @Autowired
    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @Override
    public ResponseEntity<be.ucll.da.appointmentservice.api.model.ApiAppointmentRequestResponse> apiV1AppointmentRequestPost(be.ucll.da.appointmentservice.api.model.ApiAppointmentRequest apiAppointmentRequest) {
        ApiAppointmentRequestResponse response = new be.ucll.da.appointmentservice.api.model.ApiAppointmentRequestResponse();
        response.appointmentRequestNumber(appointmentService.registerRequest(apiAppointmentRequest));

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> apiV1AppointmentConfirmationPost(be.ucll.da.appointmentservice.api.model.ApiAppointmentConfirmation apiAppointmentConfirmation) {
        appointmentService.finalizeAppointment(apiAppointmentConfirmation);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<ApiAppointmentOverview> apiV1AppointmentDayGet(String dayString) {
        LocalDate day = LocalDate.parse(dayString, DateTimeFormatter.ISO_DATE);

        ApiAppointmentOverview overview = new ApiAppointmentOverview();
        overview.setDay(day);

        for (Appointment appointment : appointmentService.getAppointmentsOnDay(day)) {
            ApiAppointment apiAppointment = new ApiAppointment();
            apiAppointment.setAccountId(appointment.getAccountId());
            apiAppointment.setPatientId(appointment.getPatientId());
            apiAppointment.setDoctorId(appointment.getDoctor());
            apiAppointment.setRoomId(appointment.getRoomId());

            overview.addAppointmentsItem(apiAppointment);
        }

        return ResponseEntity.ok(overview);
    }
}
