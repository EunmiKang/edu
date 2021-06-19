package edu;

public class EduCompleted extends AbstractEvent {

    private Long takingId;
    private Long appId;
    private String userId;
    private String eduName;
    private Long eduId;
    private String status;

    public Long getTakingId() {
        return takingId;
    }

    public void setTakingId(Long takingId) {
        this.takingId = takingId;
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