package edu;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="Mypage_table")
public class Mypage {

        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private Long id;
        private String userId;
        private Long appId;
        private String eduName;
        private Long eduId;
        private String status;


        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
        public Long getAppId() {
            return appId;
        }

        public void setAppId(Long appId) {
            this.appId = appId;
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
