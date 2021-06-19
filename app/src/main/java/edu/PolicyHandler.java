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
    @Autowired EduApplicationRepository eduApplicationRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverEduCancelled_ChangeStatus(@Payload EduCancelled eduCancelled){

        if(!eduCancelled.validate()) return;

        System.out.println("\n\n##### listener ChangeStatus : " + eduCancelled.toJson() + "\n\n");

        // 상태 변경 - 수강취소됨 //
        EduApplication eduApplication = eduApplicationRepository.findByAppId(eduCancelled.getAppId());
        eduApplication.setStatus(eduCancelled.getStatus());
        eduApplicationRepository.save(eduApplication);
            
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverEduCompleted_ChangeStatus(@Payload EduCompleted eduCompleted){

        if(!eduCompleted.validate()) return;

        System.out.println("\n\n##### listener ChangeStatus : " + eduCompleted.toJson() + "\n\n");

        // 상태 변경 - 수강완료됨 //
        EduApplication eduApplication = eduApplicationRepository.findByAppId(eduCompleted.getAppId());
        eduApplication.setStatus(eduCompleted.getStatus());
        eduApplicationRepository.save(eduApplication);
            
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverEduRegistered_ChangeStatus(@Payload EduRegistered eduRegistered){

        if(!eduRegistered.validate()) return;

        System.out.println("\n\n##### listener ChangeStatus : " + eduRegistered.toJson() + "\n\n");

        // 상태 변경 - 수강등록됨 //
        EduApplication eduApplication = eduApplicationRepository.findByAppId(eduRegistered.getAppId());
        eduApplication.setStatus(eduRegistered.getStatus());
        eduApplicationRepository.save(eduApplication);
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
