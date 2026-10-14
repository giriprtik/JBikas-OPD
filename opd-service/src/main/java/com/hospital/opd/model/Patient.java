package com.hospital.opd.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "patients")
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private int age;
    private String gender;
    private String contactNumber;
    private String chiefComplaint;
    private String status; // QUEUED, IN_CONSULTATION, DISCHARGED, REFERRED_TO_LAB

    private LocalDateTime registrationTime;

    public Patient() {}
    public Patient(String name, int age, String gender, String contactNumber, String chiefComplaint) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.contactNumber = contactNumber;
        this.chiefComplaint = chiefComplaint;
        this.status = "QUEUED";
        this.registrationTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public String getChiefComplaint() { return chiefComplaint; }
    public void setChiefComplaint(String chiefComplaint) { this.chiefComplaint = chiefComplaint; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getRegistrationTime() { return registrationTime; }
}
