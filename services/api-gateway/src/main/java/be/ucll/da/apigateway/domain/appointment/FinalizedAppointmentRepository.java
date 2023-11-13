package be.ucll.da.apigateway.domain.appointment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FinalizedAppointmentRepository extends JpaRepository<Appointment, String> {
    List<Appointment> getAppointmentsByAppointmentDay(LocalDate day);
}
