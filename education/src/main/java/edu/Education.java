package edu;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Education_table")
public class Education {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long takingId;
    private String userId;
    private String eduName;
    private Long eduId;
    private String status;
    private Long appId;

    @PostPersist
    public void onPostPersist(){
        try {
            Thread.currentThread().sleep((long) (400 + Math.random() * 220));
        } catch (InterruptedException e) {
                e.printStackTrace();
        }
        
        EduRegistered eduRegistered = new EduRegistered();
        BeanUtils.copyProperties(this, eduRegistered);
        eduRegistered.publishAfterCommit();

    }

    @PostUpdate
    public void onPostUpdate(){
        EduCompleted eduCompleted = new EduCompleted();
        BeanUtils.copyProperties(this, eduCompleted);
        eduCompleted.publishAfterCommit();


    }

    @PreRemove
    public void onPreRemove(){
        EduCancelled eduCancelled = new EduCancelled();
        BeanUtils.copyProperties(this, eduCancelled);
        eduCancelled.publishAfterCommit();


    }


    public Long getTakingId() {
        return takingId;
    }

    public void setTakingId(Long takingId) {
        this.takingId = takingId;
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
    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }




}
