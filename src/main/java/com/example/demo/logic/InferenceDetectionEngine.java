package com.example.demo.logic;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.demo.data.model.DBLogEntry;
import com.example.demo.data.model.DBLogEntry2;
import com.example.demo.data.model.PatientInfo;
import com.example.demo.data.model.PatientMedicalInfo;
import com.example.demo.data.model.Policy;
import com.example.demo.data.model.QueryResult;
import com.example.demo.data.repository.DBLogEntryRepository;
import com.example.demo.data.repository.PatientMedicalInfoRepository;
import com.example.demo.data.repository.PolicyRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class InferenceDetectionEngine {

    private final static Logger logger = LoggerFactory.getLogger(InferenceDetectionEngine.class);

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private DBLogEntryRepository dbLogEntryRepository;
    
    @Autowired
    private PatientMedicalInfoRepository patientMedicallnfoRepository;
    

    public List<PatientInfo> checkInferenceForPatientInfo(List<PatientInfo> resultList, List<String> tablesAndColumnsAccessed) {
        
        boolean policiesFound = false;

        //1 - First step for the inference detection is to fetch the user 
        String currentUserName = getUser();
        logger.info("Step 1: getUser() => " + currentUserName);
        
        //2. get policies related to the query
        List<Policy> policies = policyRepository.findDistinctByInputColumnsInAndBlockedColumnsIn(tablesAndColumnsAccessed, tablesAndColumnsAccessed);
        logger.info("Step 2: get policies, found [" + policies.size() + "] policies => " + policies);
        if(!policies.isEmpty()) {
            policiesFound = true;
        }
        
        if(policiesFound) {
            //for each item in the result list check if it causes potential inference attack
            Map<PatientInfo, Boolean> inferenceDetectionResults = new HashMap<>();
            for(PatientInfo pi : resultList) {
                for(Policy p: policies) {
                    //3. get the input columns that are not part of this query
                    List<String> policyInputColumns = new ArrayList<>(p.getInputColumns());
                    policyInputColumns.removeIf(x-> tablesAndColumnsAccessed.contains(x));
                    //get information from the logs based on the policy input columns
                    List<DBLogEntry> logEntries = dbLogEntryRepository.findDistinctByTablesColumnsAccessedInAndUserName(policyInputColumns, currentUserName);
                    //logger.info("Logs=>" + logEntries);
                    //TODO: if we have duplicate information, then we will process into unique set
                    //TODO: for each log entry, check the policy criteria
                    for(DBLogEntry entry: logEntries) {
                        for(String id: entry.getIdsAccessed()) {
                            String lengthOfStay = patientMedicallnfoRepository.findById(Long.valueOf(id)).get().getLengthOfStay();
                            String dateOfEntry = pi.getDateOfEntry();
                            String dateOfLeave = pi.getDateOfLeave();
                            
                            boolean isInference = false;
                            //=========================================================================================
                            //TODO: how to make it generic, an idea, implement and replace hardcoded parts
                            Map<String, String> policyCriteriaInputMap = new HashMap<>();
                            policyCriteriaInputMap.put("patient_medical_info.length_of_stay", lengthOfStay);
                            policyCriteriaInputMap.put("patient_info.date_of_entry", dateOfEntry);
                            policyCriteriaInputMap.put("patient_info.date_of_leave", dateOfLeave);
                            //boolean isInference = p.processCriteria(policyCriteriaInputMap);
                            //=========================================================================================
                            // Hard coded part
                            //=========================================================================================
                            if("TBD".equals(lengthOfStay) || "TBD".equals(dateOfLeave)) {
                                continue; //ignore this and continue to the next one
                            }
                            else {
                                int lengthOfStayN = Integer.valueOf(lengthOfStay);
                                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
                                LocalDate dateOfEntryN = LocalDate.parse(dateOfEntry, dateFormatter);
                                LocalDate dateOfLeaveN = LocalDate.parse(dateOfLeave, dateFormatter);
                                int daysBetween = (int) ChronoUnit.DAYS.between(dateOfEntryN, dateOfLeaveN);
                                isInference = daysBetween == lengthOfStayN; //the hard coded criteria, if they are equal
                                logger.info("pi.name=" + pi.getName() + ", pmi.id=" + id +", days b/w=" + daysBetween + ", length of Stay=" + lengthOfStayN + ", inference=" + isInference);
                            }
                            //=========================================================================================
                            
                            //TODOif it matches, mark it as reference attack
                            if(isInference) {
                                pi.setInference(true);
                                break;
                            }
                        }
                    }
                    
                }
            }
        }

        //last step. record a log of the query performed (TODO: add inference logic)
        List<String> values = resultList.stream().map(e -> String.valueOf(e.getName())).collect(Collectors.toList());
        DBLogEntry dbLogEntry = new DBLogEntry(null, currentUserName, tablesAndColumnsAccessed, values, LocalDateTime.now());
        
        logger.info("Step last: recording log => " + dbLogEntry);
        //NOTE: the save opertion also resulted into updating the patient info data retrieved with modification to the inference flag (deprecated), so hibernate picks up changes automatically. be careful of this to avoid unwanted changes to database
        dbLogEntryRepository.save(dbLogEntry);

        return resultList;
    }

    public List<PatientMedicalInfo> checkInferenceForPatientMedicalInfo(List<PatientMedicalInfo> resultList, List<String> tablesAndColumnsAccessed) {
        //1 - First step for the inference detection is to fetch the user 
        String currentUserName = getUser();
        logger.info("Step 1: getUser() => " + currentUserName);
        
        List<String> values = resultList.stream().map(e -> String.valueOf(e.getPatientId())).collect(Collectors.toList());
        DBLogEntry dbLogEntry = new DBLogEntry(null, currentUserName, tablesAndColumnsAccessed, values, LocalDateTime.now());
        logger.info("Step last: recording log => " + dbLogEntry);
        dbLogEntryRepository.save(dbLogEntry);
    
        return resultList;
    }

    //We get the user if they are logged in. If they are not logged in we return null.
    private String getUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (!(authentication instanceof AnonymousAuthenticationToken)) {
                return authentication.getName();
            }
            else return null;
    }

    

    public boolean checkForInference(List<String> currentQueryTablesColumnsAccessed, Map<String, String> currentQueryResultRowMap, Policy policy, List<DBLogEntry2> currentUserLogs) {
        // check if one of the query input columns are in the policy blocked columns 
        // this determines an inference attack is possible if the input columns is returned to user
        boolean isInPolicy = false;
        for(String columnInQuery : currentQueryTablesColumnsAccessed) {
            if(policy.getInputColumns().contains(columnInQuery)) {
                isInPolicy = true;
                break;
            }
        }
        
        if(!isInPolicy) return false;

        // first check if the policy columns have been all searched before
        Map<String, Boolean> policyTablesColumnsFlags = new HashMap<>();
        for(DBLogEntry2 currentLog : currentUserLogs) {
            for(String columnInQuery : currentLog.getTablesColumnsAccessed()) {
            if(policy.getInputColumns().contains(columnInQuery)) {
                //columnPolicyFlag  = true;
            }
        }
        }
        //boolean isInference = policyTablesColumnsFlags.stream().reduce(Boolean::logicalAnd).get();
        // second check if the policy criteria has been met for these query results that are flagged as potential inference

        


        return false;
    }
}
