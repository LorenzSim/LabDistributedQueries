package be.ucll.da.apigateway.domain.doctor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Doctor {
    @Id
    private Integer id;

    private Integer age;

    private String address;

    private String firstName;

    private String lastName;

    public Doctor() {
    }

    public Doctor(Integer id, Integer age, String address, String firstName, String lastName) {
        this.id = id;
        this.age = age;
        this.address = address;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Doctor doctor = (Doctor) o;

        if (!id.equals(doctor.id)) return false;
        if (!age.equals(doctor.age)) return false;
        if (!address.equals(doctor.address)) return false;
        if (!firstName.equals(doctor.firstName)) return false;
        return lastName.equals(doctor.lastName);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + age.hashCode();
        result = 31 * result + address.hashCode();
        result = 31 * result + firstName.hashCode();
        result = 31 * result + lastName.hashCode();
        return result;
    }
}
