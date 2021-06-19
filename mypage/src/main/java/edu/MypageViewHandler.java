package edu;

import edu.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class MypageViewHandler {


    @Autowired
    private MypageRepository mypageRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenPaid_then_CREATE_1 (@Payload Paid paid) {
        try {

            if (!paid.validate()) return;

            System.out.println("\n\n##### listener paid : " + paid.toJson() + "\n\n");

            // view 객체 생성
            Mypage mypage = new Mypage();
            // view 객체에 이벤트의 Value 를 set 함
            mypage.setUserId(paid.getUserId());
            mypage.setEduId(paid.getAppId());
            mypage.setEduName(paid.getEduName());
            mypage.setEduId(paid.getEduId());
            mypage.setAppId(paid.getAppId());
            mypage.setStatus(paid.getStatus());
            // view 레파지 토리에 save
            mypageRepository.save(mypage);
        
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenEduRegistered_then_UPDATE_1(@Payload EduRegistered eduRegistered) {
        try {
            if (!eduRegistered.validate()) return;
            /*
            try {
                Thread.currentThread().sleep((long) (400 + Math.random() * 220));
            } catch (InterruptedException e) {
                    e.printStackTrace();
            }
            */
            System.out.println("\n\n##### listener eduRegistered : " + eduRegistered.toJson() + "\n\n");

            // view 객체 조회
            Mypage mypage = mypageRepository.findByAppId(eduRegistered.getAppId());
            // view 객체에 이벤트의 eventDirectValue 를 set 함
            mypage.setStatus("EduRegistered");
            // view 레파지 토리에 save
            mypageRepository.save(mypage);
            
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenEduAppCancelled_then_UPDATE_2(@Payload EduAppCancelled eduAppCancelled) {
        try {
            if (!eduAppCancelled.validate()) return;
                // view 객체 조회
            Mypage mypage = mypageRepository.findByAppId(eduAppCancelled.getAppId());
            // view 객체에 이벤트의 eventDirectValue 를 set 함
            mypage.setStatus(eduAppCancelled.getStatus());
            // view 레파지 토리에 save
            mypageRepository.save(mypage);
            
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    @StreamListener(KafkaProcessor.INPUT)
    public void whenEduCompleted_then_UPDATE_3(@Payload EduCompleted eduCompleted) {
        try {
            if (!eduCompleted.validate()) return;
                // view 객체 조회
            Mypage mypage = mypageRepository.findByAppId(eduCompleted.getAppId());
            // view 객체에 이벤트의 eventDirectValue 를 set 함
            mypage.setStatus(eduCompleted.getStatus());
            // view 레파지 토리에 save
            mypageRepository.save(mypage);
            
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenEduCancelled_then_UPDATE_4(@Payload EduCancelled eduCancelled) {
        try {
            if (!eduCancelled.validate()) return;
                // view 객체 조회
            Mypage mypage = mypageRepository.findByAppId(eduCancelled.getAppId());
            // view 객체에 이벤트의 eventDirectValue 를 set 함
            mypage.setStatus(eduCancelled.getStatus());
            // view 레파지 토리에 save
            mypageRepository.save(mypage);
            
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}