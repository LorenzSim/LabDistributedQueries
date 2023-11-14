package be.ucll.da.apigateway.web;

import be.ucll.da.apigateway.api.HospitalApiDelegate;
import be.ucll.da.apigateway.api.model.ApiAppointment;
import be.ucll.da.apigateway.api.model.ApiAppointmentDoctor;
import be.ucll.da.apigateway.api.model.ApiAppointmentOverview;
import be.ucll.da.apigateway.api.model.ApiAppointmentPatient;
import be.ucll.da.apigateway.client.appointment.api.AppointmentApi;
import be.ucll.da.apigateway.client.doctor.api.DoctorApi;
import be.ucll.da.apigateway.client.doctor.model.ApiDoctor;
import be.ucll.da.apigateway.client.patient.api.PatientApi;
import be.ucll.da.apigateway.domain.appointment.Appointment;
import be.ucll.da.apigateway.domain.appointment.FinalizedAppointmentRepository;
import be.ucll.da.apigateway.domain.doctor.Doctor;
import be.ucll.da.apigateway.domain.patient.Patient;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class HospitalController implements HospitalApiDelegate {

    private final CircuitBreakerFactory circuitBreakerFactory;

    private final EurekaClient discoveryClient;
    private final be.ucll.da.apigateway.client.appointment.api.AppointmentApi appointmentApi;

    private final be.ucll.da.apigateway.client.doctor.api.DoctorApi doctorApi;

    private final be.ucll.da.apigateway.client.patient.api.PatientApi patientApi;

    private final FinalizedAppointmentRepository finalizedAppointmentRepository;

    @Autowired
    public HospitalController(CircuitBreakerFactory circuitBreakerFactory, EurekaClient discoveryClient, AppointmentApi appointmentApi, DoctorApi doctorApi, PatientApi patientApi, FinalizedAppointmentRepository finalizedAppointmentRepository) {
        this.circuitBreakerFactory = circuitBreakerFactory;
        this.discoveryClient = discoveryClient;
        this.appointmentApi = appointmentApi;
        this.doctorApi = doctorApi;
        this.patientApi = patientApi;
        this.finalizedAppointmentRepository = finalizedAppointmentRepository;

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

    private void refreshApiConfig() {
        InstanceInfo appointmentServiceInstance = discoveryClient.getNextServerFromEureka("appointment-service", false);
        appointmentApi.getApiClient().setBasePath(appointmentServiceInstance.getHomePageUrl());

        InstanceInfo patientServiceInstance = discoveryClient.getNextServerFromEureka("patient-service", false);
        patientApi.getApiClient().setBasePath(patientServiceInstance.getHomePageUrl());

        InstanceInfo doctorServiceInstance = discoveryClient.getNextServerFromEureka("doctor-service", false);
        doctorApi.getApiClient().setBasePath(doctorServiceInstance.getHomePageUrl());
    }

    private ResponseEntity<ApiAppointmentOverview> getUsingApiComposition(LocalDate day) {
        refreshApiConfig();

        be.ucll.da.apigateway.client.appointment.model.ApiAppointmentOverview clientAppointmentOverview
                = appointmentApi.apiV1AppointmentDayGet(day.toString());

        ApiAppointmentOverview appointmentOverview =
                circuitBreakerFactory.create("appointmentApi").run(
                        () -> new ApiAppointmentOverview()
                                .day(day)
                                .appointments((clientAppointmentOverview.getAppointments()).stream().map(this::createApiAppointment).toList())
                );

        return ResponseEntity.ok(appointmentOverview);
    }


    // --- Code For CQRS

    private ResponseEntity<ApiAppointmentOverview> getUsingCqrs(LocalDate day) {
        List<Appointment> appointments = finalizedAppointmentRepository.getAppointmentsByAppointmentDay(day);

        ApiAppointmentOverview appointmentOverview =
                new ApiAppointmentOverview()
                .day(day)
                .appointments(appointments.stream().map(this::createApiAppointment).toList());

        return ResponseEntity.ok(appointmentOverview);
    }

    private ApiAppointment createApiAppointment(Appointment appointment) {
        return createApiAppointment(appointment.getAccountId(), appointment.getRoomId(), createApiAppointmentDoctor(appointment.getDoctor()), createApiAppointmentPatient(appointment.getPatient()));
    }

    private ApiAppointment createApiAppointment(be.ucll.da.apigateway.client.appointment.model.ApiAppointment apiAppointment) {
        ApiAppointmentDoctor apiAppointmentDoctor = createApiAppointmentDoctor(getApiDoctor(apiAppointment.getDoctorId()));
        ApiAppointmentPatient apiAppointmentPatient = createApiAppointmentPatient(getApiPatient(apiAppointment.getPatientId()));
        return createApiAppointment(apiAppointment.getAccountId(), apiAppointment.getRoomId(), apiAppointmentDoctor, apiAppointmentPatient);
    }

    private ApiAppointment createApiAppointment(Integer accountId, Integer roomId, ApiAppointmentDoctor doctor, ApiAppointmentPatient patient) {
        return new ApiAppointment()
                .accountId(accountId)
                .roomId(roomId)
                .doctor(doctor)
                .patient(patient);
    }


    private ApiAppointmentDoctor createApiAppointmentDoctor(Doctor doctor) {
        return createApiAppointmentDoctor(doctor.getId(), doctor.getFirstName(), doctor.getLastName(), doctor.getAge(), doctor.getAddress());
    }

    private ApiAppointmentDoctor createApiAppointmentDoctor(be.ucll.da.apigateway.client.doctor.model.ApiDoctor doctor) {
        return createApiAppointmentDoctor(doctor.getId(), doctor.getFirstName(), doctor.getLastName(), doctor.getAge(), doctor.getAddress());
    }

    private ApiAppointmentDoctor createApiAppointmentDoctor(Integer id, String firstName, String lastName, Integer age, String address) {
        return new ApiAppointmentDoctor()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .age(age)
                .address(address);
    }

    private ApiAppointmentPatient createApiAppointmentPatient(be.ucll.da.apigateway.client.patient.model.ApiPatient patient) {
        return createApiAppointmentPatient(patient.getPatientId(), patient.getFirstName(), patient.getLastName(), patient.getEmail());
    }

    private ApiAppointmentPatient createApiAppointmentPatient(Patient patient) {
        return createApiAppointmentPatient(patient.getId(), patient.getFirstName(), patient.getLastName(), patient.getEmail());
    }

    private ApiAppointmentPatient createApiAppointmentPatient(Integer id, String firstName, String lastName, String email) {
        return new ApiAppointmentPatient()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .email(email);
    }

    private ApiDoctor getApiDoctor(Integer id) {
        return circuitBreakerFactory.create("doctorApi").run(() -> doctorApi.getDoctorById(id));
    }

    private be.ucll.da.apigateway.client.patient.model.ApiPatient getApiPatient(Integer id) {
        return circuitBreakerFactory.create("patientApi").run(() -> patientApi.getPatientById(id));
    }

}
