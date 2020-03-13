package com.example.demo.config;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.demo.data.model.PatientInfo;
import com.example.demo.data.model.PatientMedicalInfo;
import com.example.demo.data.model.Policy;

import com.example.demo.data.model.Privilege;

import com.example.demo.data.model.Role;
import com.example.demo.data.model.User;
import com.example.demo.data.repository.PatientMedicalInfoRepository;
import com.example.demo.data.repository.PatientlnfoRepository;
import com.example.demo.data.repository.PolicyRepository;
import com.example.demo.data.repository.RoleRepository;
import com.example.demo.data.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LoadData implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(LoadData.class);

    boolean alreadySetup = false;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private PatientMedicalInfoRepository patientMedicalInfoRepository;

    @Autowired
    private PatientlnfoRepository patientInfoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {

        if (alreadySetup) {
            log.info("Already loaded stub data");
            return;
        } else
            log.info("Populating database with stub data");


        patientMedicalInfoRepository.saveAll(Arrays.asList(
        new PatientMedicalInfo(1L, "TBD", "Cardiac Arrest", false),
        new PatientMedicalInfo(2L, "3", "Brain Aneurysm",false),
        new PatientMedicalInfo(3L, "2", "Brain Aneurysm",false),
        new PatientMedicalInfo(4L, "4", "Cardiac Arrest",false),
        new PatientMedicalInfo(5L, "2", "Brain Aneurysm",false),
        new PatientMedicalInfo(6L, "TBD", "Brain Aneurysm",false),
        new PatientMedicalInfo(7L, "9", "Cardiac Arrest",false),
        new PatientMedicalInfo(8L, "7", "Cardiac Arrest",false)
        ));

        patientInfoRepository.saveAll(Arrays.asList(
            new PatientInfo("John Smith", "Oct 27, 2014", "Oct 31, 2014", "M", false),
            new PatientInfo("Mary Jane", "Oct 22, 2014", "Oct 31, 2014", "F", false),
            new PatientInfo("Patty Patterson", "Oct 24, 2014", "Oct 31, 2014", "F", false),
            new PatientInfo("Jimmy Jistle", "Oct 28, 2014", "Oct 31, 2014", "M", false),
            new PatientInfo("Tony Tiger", "Oct 29, 2014", "Oct 31, 2014", "M", false),
            new PatientInfo("Chris Campbell", "Oct 29, 2014", "Oct 31, 2014", "M", false),
            new PatientInfo("Fiona Fastener", "Oct 25, 2014", "TBD", "F", false),
            new PatientInfo("Horus Harvey", "Oct 20, 2014", "TBD", "M", false)
            ));

        Policy p = new Policy();
        p.setInputColumns(Arrays.asList("patient_medical_info.length_of_stay", "patient_info.date_of_entry",
                "patient_info.date_of_leave"));
        p.setBlockedColumns(Arrays.asList("patient_info.name"));
        p.setRelationship("patient_info.date_of_leave - patient_info.date_of_entry != patient_medical_info.length_of_stay");
        policyRepository.save(p);


        Role nurseRole = new Role(0, "ROLE_ NURSE", new ArrayList<Privilege>());

        Role doctorRole = new Role(0, "ROLE_DOCTOR", new ArrayList<Privilege>());
        Role adminRole = new Role(0, "ROLE_ADMIN", new ArrayList<Privilege>());

        roleRepository.saveAll(Arrays.asList(doctorRole, adminRole,nurseRole));

        List<User> users = new ArrayList<>();
        users.add(new User(1, "hasan", LocalDate.now(), null, passwordEncoder.encode("hasan"), Arrays.asList(adminRole)));
        users.add(new User(2, "ryan", LocalDate.now(), null, passwordEncoder.encode("ryan"), Arrays.asList(adminRole)));
        users.add(new User(3, "jason", LocalDate.now(), null, passwordEncoder.encode("jason"),Arrays.asList(doctorRole)));
        users.add(new User(4, "sasha", LocalDate.now(), null, passwordEncoder.encode("sasha"),Arrays.asList(adminRole)));
        users.add(new User(5, "tashfiq", LocalDate.now(), null, passwordEncoder.encode("tashfiq"),Arrays.asList(adminRole)));
        users.add(new User(6, "calvin", LocalDate.now(), null, passwordEncoder.encode("calvin"),Arrays.asList(adminRole)));
        users.add(new User(7, "admin", LocalDate.now(), null, passwordEncoder.encode("admin"),Arrays.asList(adminRole)));
        users.add(new User(8, "user", LocalDate.now(), null, passwordEncoder.encode("user"),Arrays.asList(doctorRole)));
        users.add(new User(9, "nurse", LocalDate.now(), null, passwordEncoder.encode("nurse"),Arrays.asList(nurseRole)));
        userRepository.saveAll(users);

        alreadySetup = true;
    }
}