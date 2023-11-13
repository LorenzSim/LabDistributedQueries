package be.ucll.da.apigateway.domain.appointment;

import be.ucll.da.apigateway.domain.doctor.Doctor;
import be.ucll.da.apigateway.domain.patient.Patient;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.time.LocalDate;

@Entity
public class Appointment {
    @Id
    private Integer id;

    private LocalDate appointmentDay;

    private Integer accountId;

    private Integer roomId;

    @ManyToOne
    private Patient patient;

    @ManyToOne
    private Doctor doctor;

    public Appointment() {
    }

    public Appointment(String id, LocalDate appointmentDay, Integer accountId, Integer roomId, Patient patient, Doctor doctor) {
        this.id = Integer.parseInt(id);
        this.appointmentDay = appointmentDay;
        this.accountId = accountId;
        this.roomId = roomId;
        this.patient = patient;
        this.doctor = doctor;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getAppointmentDay() {
        return appointmentDay;
    }

    public void setAppointmentDay(LocalDate day) {
        this.appointmentDay = day;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }
}
