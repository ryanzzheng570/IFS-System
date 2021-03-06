package com.example.demo.config;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.demo.data.model.PatientInfo;
import com.example.demo.data.model.PatientMedicalInfo;
import com.example.demo.data.model.BillingInfo;

import com.example.demo.data.model.Policy;

import com.example.demo.data.model.Privilege;

import com.example.demo.data.model.Role;
import com.example.demo.data.model.User;
import com.example.demo.data.repository.PatientMedicalInfoRepository;
import com.example.demo.data.repository.PatientlnfoRepository;
import com.example.demo.data.repository.BillingInfoRepository;
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
    private BillingInfoRepository billingInfoRepository;

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


        Policy p = new Policy();
        p.setInputColumns(Arrays.asList("patient_medical_info.length_of_stay", "patient_info.date_of_entry",
                "patient_info.date_of_leave"));
        p.setBlockedColumns(Arrays.asList("patient_info.name", "patient_medical_info.reason_of_visit"));
        p.setRelationship("patient_info.date_of_leave - patient_info.date_of_entry != patient_medical_info.length_of_stay");
        policyRepository.save(p);


        Policy p1 = new Policy();
        p1.setInputColumns(Arrays.asList("billing_info.total_medical_costs", "patient_medical_info.length_of_stay",
                "patient_medical_info.daily_medical_cost"));
        p1.setBlockedColumns(Arrays.asList("billing_info.account_number"));
        p1.setRelationship("patient_medical_info.daily_medical_cost * patient_medical_info.length_of_stay != billing_info.total_medical_costs");
        policyRepository.save(p1);

        patientMedicalInfoRepository.saveAll(Arrays.asList(new PatientMedicalInfo(null, "TBD", "Cardiac Arrest", 1500, false),
                new PatientMedicalInfo(null, "2", "Brain Aneurysm", 1000, false),
                new PatientMedicalInfo(null, "2", "Brain Aneurysm", 1000, false),
                new PatientMedicalInfo(null, "1", "Cardiac Arrest", 5000, false),
                new PatientMedicalInfo(null, "5", "Brain Aneurysm", 1000, false),
                new PatientMedicalInfo(null, "TBD", "Brain Aneurysm", 1000, false),
                new PatientMedicalInfo(null, "5", "Cardiac Arrest", 1500, false),
                new PatientMedicalInfo(null, "7", "Cardiac Arrest", 1500, false)));

        patientInfoRepository
        .saveAll(Arrays.asList(new PatientInfo("John Smith", "Oct 27, 2014", "Oct 31, 2014", "M", false),
                        new PatientInfo("Mary Jane", "Oct 22, 2014", "Oct 31, 2014", "F", false),
                        new PatientInfo("Patty Patterson", "Oct 24, 2014", "Oct 31, 2014", "F", false),
                        new PatientInfo("Jimmy Jistle", "Oct 28, 2014", "Oct 31, 2014", "M", false),
                        new PatientInfo("Tony Tiger", "Oct 29, 2014", "Oct 31, 2014", "M", false),
                        new PatientInfo("Chris Campbell", "Oct 29, 2014", "Oct 31, 2014", "M", false),
                        new PatientInfo("Fiona Fastener", "Oct 25, 2014", "TBD", "F", false),
                        new PatientInfo("Horus Harvey", "Oct 20, 2014", "TBD", "M", false)));

        billingInfoRepository
        .saveAll(Arrays.asList(new BillingInfo("85720", "99 Jaymarry Cres", 13500, false),
                new BillingInfo("85721", "1348 Millfair Way", 6000, false),
                new BillingInfo("85722", "34 Bensay Ave", 3000, false),
                new BillingInfo("85723", "3281 Beavertree Dr", 5000, false),
                new BillingInfo("85724", "1200 Martin St Apt 11", 2000, false),
                new BillingInfo("85725", "523 Menpearl St", 0, false),
                new BillingInfo("85726", "2 Brysonmount Dr", 0, false),
                new BillingInfo("85727", "4488 Tonsinville Way", 10500, false)));
                

        Role doctorRole = new Role(0, "ROLE_DOCTOR", new ArrayList<Privilege>());
        Role adminRole = new Role(0, "ROLE_ADMIN", new ArrayList<Privilege>());
        Role nurseRole = new Role(0, "ROLE_NURSE", new ArrayList<Privilege>());


        roleRepository.saveAll(Arrays.asList(doctorRole, adminRole, nurseRole));

        List<User> users = new ArrayList<>();
        users.add(new User(1, "hasan", LocalDate.now(), null, passwordEncoder.encode("hasan"), adminRole));
        users.add(new User(2, "ryan", LocalDate.now(), null, passwordEncoder.encode("ryan"), adminRole));
        users.add(new User(3, "jason", LocalDate.now(), null, passwordEncoder.encode("jason"),doctorRole));
        users.add(new User(4, "sasha", LocalDate.now(), null, passwordEncoder.encode("sasha"),adminRole));
        users.add(new User(5, "tashfiq", LocalDate.now(), null, passwordEncoder.encode("tashfiq"),adminRole));
        users.add(new User(6, "calvin", LocalDate.now(), null, passwordEncoder.encode("calvin"),adminRole));
        users.add(new User(7, "admin", LocalDate.now(), null, passwordEncoder.encode("admin"),adminRole));
        users.add(new User(8, "user", LocalDate.now(), null, passwordEncoder.encode("user"),nurseRole));
        users.add(new User(9, "nurse", LocalDate.now(), null, passwordEncoder.encode("nurse"),nurseRole));



        userRepository.saveAll(users);

        alreadySetup = true;
    }
}