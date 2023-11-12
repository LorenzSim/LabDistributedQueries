package be.ucll.da.apigateway.web;

import be.ucll.da.apigateway.api.HospitalApiDelegate;
import be.ucll.da.apigateway.api.model.ApiAppointment;
import be.ucll.da.apigateway.api.model.ApiAppointmentDoctor;
import be.ucll.da.apigateway.api.model.ApiAppointmentOverview;
import be.ucll.da.apigateway.api.model.ApiAppointmentPatient;
import be.ucll.da.apigateway.client.appointment.api.AppointmentApi;
import be.ucll.da.apigateway.client.doctor.api.DoctorApi;
import be.ucll.da.apigateway.client.patient.api.PatientApi;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Component
public class HospitalController implements HospitalApiDelegate {

    private final CircuitBreakerFactory circuitBreakerFactory;

    private final EurekaClient discoveryClient;
    private final be.ucll.da.apigateway.client.appointment.api.AppointmentApi appointmentApi;

    private final be.ucll.da.apigateway.client.doctor.api.DoctorApi doctorApi;

    private final be.ucll.da.apigateway.client.patient.api.PatientApi patientApi;

    @Autowired
    public HospitalController(CircuitBreakerFactory circuitBreakerFactory, EurekaClient discoveryClient, AppointmentApi appointmentApi, DoctorApi doctorApi, PatientApi patientApi) {
        this.circuitBreakerFactory = circuitBreakerFactory;
        this.discoveryClient = discoveryClient;
        this.appointmentApi = appointmentApi;
        this.doctorApi = doctorApi;
        this.patientApi = patientApi;
    }

    @Override
    public ResponseEntity<ApiAppointmentOverview> apiV1AppointmentDayGet(String dayString, Boolean useCqrs) {
        LocalDate day = LocalDate.parse(dayString, DateTimeFormatter.ISO_DATE);

        if (useCqrs) {
            return getUsingCqrs(day);
        } else {
            return getUsingApiComposition(day);
        }
    }

    // --- Code For Composition ---

    private ResponseEntity<ApiAppointmentOverview> getUsingApiComposition(LocalDate day) {
        InstanceInfo appointmentServiceInstance = discoveryClient.getNextServerFromEureka("appointment-service", false);
        appointmentApi.getApiClient().setBasePath(appointmentServiceInstance.getHomePageUrl());

        InstanceInfo patientServiceInstance = discoveryClient.getNextServerFromEureka("patient-service", false);
        patientApi.getApiClient().setBasePath(patientServiceInstance.getHomePageUrl());

        InstanceInfo doctorServiceInstance = discoveryClient.getNextServerFromEureka("doctor-service", false);
        doctorApi.getApiClient().setBasePath(doctorServiceInstance.getHomePageUrl());

        ApiAppointmentOverview appointmentOverview =
                circuitBreakerFactory.create("appointmentApi").run(
                        () -> new ApiAppointmentOverview()
                                .day(day)
                                .appointments(
                                Objects.requireNonNull(appointmentApi.apiV1AppointmentDayGet(day.toString()).getAppointments()).stream().map(
                                        apiAppointment ->
                                                new ApiAppointment()
                                                        .accountId(apiAppointment.getAccountId())
                                                        .roomId(apiAppointment.getRoomId())
                                                        .doctor(circuitBreakerFactory.create("doctorApi").run(() ->{
                                                            be.ucll.da.apigateway.client.doctor.model.ApiDoctor doctor = doctorApi.getDoctorById(apiAppointment.getDoctorId());
                                                            return new ApiAppointmentDoctor()
                                                                    .id(doctor.getId())
                                                                    .age(doctor.getAge())
                                                                    .address(doctor.getAddress())
                                                                    .firstName(doctor.getFirstName())
                                                                    .lastName(doctor.getLastName());
                                                        })).patient(circuitBreakerFactory.create("patientApi").run(() -> {

                                                            be.ucll.da.apigateway.client.patient.model.ApiPatient apiPatient = patientApi.getPatientById(apiAppointment.getDoctorId());
                                                            return new ApiAppointmentPatient()
                                                                    .id(apiPatient.getPatientId())
                                                                    .firstName(apiPatient.getFirstName())
                                                                    .lastName(apiPatient.getLastName())
                                                                    .email(apiPatient.getEmail());
                                                        }))
                                ).toList()
                        )
                );

        return ResponseEntity.ok(appointmentOverview);
    }

    // --- Code For CQRS

    private ResponseEntity<ApiAppointmentOverview> getUsingCqrs(LocalDate day) {
        throw new RuntimeException("Implement me!!");
    }
}
