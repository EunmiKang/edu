package edu;

import edu.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired EducationRepository educationRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaid_RegisterEdu(@Payload Paid paid){

        if(!paid.validate()) return;

        System.out.println("\n\n##### listener RegisterEdu : " + paid.toJson() + "\n\n");

        // 수강 등록 //
        Education education = new Education();
        education.setAppId(paid.getAppId());
        education.setEduId(paid.getEduId());
        education.setEduName(paid.getEduName());
        education.setStatus("EduRegistered");
        education.setUserId(paid.getUserId());
        educationRepository.save(education);
            
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverEduAppCancelled_CancelEdu(@Payload EduAppCancelled eduAppCancelled){

        if(!eduAppCancelled.validate()) return;

        System.out.println("\n\n##### listener CancelEdu : " + eduAppCancelled.toJson() + "\n\n");

        // 수강 취소 //
        Education education = educationRepository.findByAppId(eduAppCancelled.getAppId());
        education.setStatus("EduCancelled");
        educationRepository.delete(education);
        
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
