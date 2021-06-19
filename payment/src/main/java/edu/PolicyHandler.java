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
    @Autowired PaymentRepository paymentRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverEduAppCancelled_PayCancel(@Payload EduAppCancelled eduAppCancelled){

        if(!eduAppCancelled.validate()) return;

        System.out.println("\n\n##### listener PayCancel : " + eduAppCancelled.toJson() + "\n\n");

        // 결제 취소 //
        Payment payment = paymentRepository.findByAppId(eduAppCancelled.getAppId());
        payment.setStatus("PayCancelled");
        paymentRepository.save(payment);
            
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
