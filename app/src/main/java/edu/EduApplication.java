package edu;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="EduApplication_table")
public class EduApplication {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long appId;
    private String userId;
    private String eduName;
    private Long eduId;
    private String status;

    @PostPersist
    public void onPostPersist(){
        EduApplied eduApplied = new EduApplied();
        BeanUtils.copyProperties(this, eduApplied);
        //eduApplied.setStatus("EduApplied");
        eduApplied.publishAfterCommit();

        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

        edu.external.Payment payment = new edu.external.Payment();
        // mappings goes here
        payment.setAppId(eduApplied.getAppId());
        payment.setEduId(eduApplied.getEduId());
        payment.setEduName(eduApplied.getEduName());
        payment.setStatus("Paid");
        payment.setUserId(eduApplied.getUserId());
        AppApplication.applicationContext.getBean(edu.external.PaymentService.class)
            .pay(payment);

    }

    @PostUpdate
    public void onPostUpdate(){
        System.out.println("\n\n##### app onPostUpdate, getStatus() : " + getStatus() + "\n\n");
        if(getStatus().equals("EduAppCancelled")) {
            EduAppCancelled eduAppCancelled = new EduAppCancelled();
            BeanUtils.copyProperties(this, eduAppCancelled);
            //eduAppCancelled.setStatus("EduAppCancelled");
            eduAppCancelled.publishAfterCommit();
        }
    }


    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getEduName() {
        return eduName;
    }

    public void setEduName(String eduName) {
        this.eduName = eduName;
    }
    public Long getEduId() {
        return eduId;
    }

    public void setEduId(Long eduId) {
        this.eduId = eduId;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }




}
