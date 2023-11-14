package be.ucll.da.apigateway.domain;

import be.ucll.da.apigateway.domain.appointment.Appointment;
import be.ucll.da.apigateway.domain.appointment.FinalizedAppointmentRepository;
import be.ucll.da.apigateway.domain.doctor.Doctor;
import be.ucll.da.apigateway.domain.doctor.DoctorRepository;
import be.ucll.da.apigateway.domain.patient.Patient;
import be.ucll.da.apigateway.domain.patient.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class HospitalService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final FinalizedAppointmentRepository finalizedAppointmentRepository;

    @Autowired
    public HospitalService(PatientRepository patientRepository, DoctorRepository doctorRepository, FinalizedAppointmentRepository finalizedAppointmentRepository) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.finalizedAppointmentRepository = finalizedAppointmentRepository;
    }

    public void savePatient(be.ucll.da.apigateway.client.patient.model.PatientValidatedEvent event) {
        Patient patient = new Patient(
                event.getPatientId(),
                event.getFirstName(),
                event.getLastName(),
                event.getEmail()
        );

        patientRepository.save(patient);
    }

    public void saveDoctorsOnPayroll(be.ucll.da.apigateway.client.doctor.model.DoctorsOnPayrollEvent event) {
        if (event.getDoctors() == null) return;

        event.getDoctors().forEach(doctorOnPayroll -> {

            Doctor doctor = new Doctor (
                    doctorOnPayroll.getId(),
                    doctorOnPayroll.getAge(),
                    doctorOnPayroll.getAddress(),
                    doctorOnPayroll.getFirstName(),
                    doctorOnPayroll.getLastName()
            );

            doctorRepository.save(doctor);
        });
    }

    public void saveFinalizedAppointment(be.ucll.da.apigateway.client.appointment.model.AppointmentFinalizedEvent event) {
        Appointment appointment = new Appointment(
                event.getAppointmentRequestNumber(),
                event.getDay(),
                event.getAccountId(),
                event.getRoomId(),
                getPatient(event.getPatientId()),
                getDoctor(event.getDoctorId())
        );

        finalizedAppointmentRepository.save(appointment);
    }

    private Patient getPatient(Integer patientId) {
        return patientRepository.findById(patientId).orElseThrow(
                () -> new HospitalServiceException(String.format("Patient with id '%d' not found!", patientId))
        );
    }

    private Doctor getDoctor(Integer doctorId) {
        return doctorRepository.findById(doctorId).orElseThrow(
                () -> new HospitalServiceException(String.format("Doctor with id '%d' not found!", doctorId))
        );
    }

}
