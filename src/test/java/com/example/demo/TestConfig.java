package com.example.demo;

import com.example.demo.data.repository.PatientMedicalInfoRepository;
import com.example.demo.data.repository.PatientlnfoRepository;
import com.example.demo.data.repository.PolicyRepository;
import com.example.demo.logic.PatientInfoManager;
import com.example.demo.logic.PatientMedicalInfoManager;
import com.example.demo.logic.PolicyManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {

    @Bean
    public PolicyManager policyManager(PolicyRepository policyRepository) {
        return new PolicyManager(policyRepository);
    }

    @Bean
    public PatientMedicalInfoManager PatientMedicalInfoManager(
            PatientMedicalInfoRepository patientMedicalInfoRepository) {
        return new PatientMedicalInfoManager(patientMedicalInfoRepository);
    }

    @Bean
    public PatientInfoManager patientInfoManager(PatientlnfoRepository patientInfoRepository) {
        return new PatientInfoManager(patientInfoRepository);
    }
}
