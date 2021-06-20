# 2021 MSA On Azure 개인 Project - Edu
## 온라인 교육(수강) 신청 MSA 프로그램 구현


*전체 소스 받기*
```
git clone https://github.com/EunmiKang/edu.git
```

# 서비스 시나리오

## 기능적 요구사항

* 고객이 교육을 선택하여 수강 신청한다
* 고객이 결제한다
* 신청이 되면 신청 내역이 수강시스템에 전달된다
* 수강시스템은 신청 내역을 전달받으면 수강등록한다
* 고객이 교육을 수강한다
* 고객이 수강 신청을 취소할 수 있다
* 고객은 교육신청내역을 조회할 수 있다  


## 비기능적 요구사항
* 트랜잭션
    * 결제가 되지 않은 신청 건은 아예 수강 신청이 성립되지 않아야 한다 (Sync 호출)
* 장애격리
    * 수강 기능이 수행되지 않더라도 수강 신청은 365일 24시간 받을 수 있어야 한다 Async(event-driven), Eventual Consistency
    * 결제시스템이 과중되면 사용자를 잠시동안 받지 않고 결제를 잠시후에 하도록 유도한다 Circuit breaker, fallback
* 성능
    * 고객이 마이페이지에서 수강 신청 내역을 조회할 수 있다 (CQRS)  
***
# 분석/설계
### Event 도출
<img src="https://user-images.githubusercontent.com/18115456/122646871-649ae000-d15c-11eb-8a7d-c5c04119e162.PNG" width="90%" />

## 부적격 이벤트 제거
<img src="https://user-images.githubusercontent.com/18115456/122646914-9ad85f80-d15c-11eb-8743-d4f1a7b330f9.PNG" width="90%" />  

```
- 중복되거나 잘못된 도메인 이벤트들을 걸러내는 작업을 수행함
- 현업이 사용하는 용어를 그대로 사용(Ubiquitous Language)
```

## 액터, 커맨드 부착
<img src="https://user-images.githubusercontent.com/18115456/122646964-dd01a100-d15c-11eb-8fa3-cd0733ed5ea8.PNG" width="75%" />

## 어그리게잇으로 묶기
<img src="https://user-images.githubusercontent.com/18115456/122646996-09b5b880-d15d-11eb-9fd4-3358d446df37.PNG" width="75%" />

## 바운디드 컨텍스트로 묶기
<img src="https://user-images.githubusercontent.com/18115456/122647020-2baf3b00-d15d-11eb-9a6f-c4947ba48906.PNG" width="75%" />

## 폴리시 부착/이동 및 컨텍스트 매핑
![image](https://user-images.githubusercontent.com/18115456/122647039-41246500-d15d-11eb-8e69-a67b07d150c4.PNG)

## 시나리오 점검 후, Event Storming 최종 결과
![image](https://user-images.githubusercontent.com/18115456/122647108-847ed380-d15d-11eb-9f83-9f335e9f61ca.PNG)

## 헥사고날 아키텍처 다이어그램 도출

***
# 구현
분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라,구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다
(각자의 포트넘버는 8081 ~ 8084, 8088 이다)
```shell
cd app
mvn spring-boot:run

cd payment
mvn spring-boot:run 

cd education
mvn spring-boot:run 

cd mypage 
mvn spring-boot:run

cd gateway
mvn spring-boot:run 
```
***
## DDD(Domain-Driven-Design)의 적용
msaez.io 를 통해 구현한 Aggregate 단위로 Entity 를 선언 후, 구현을 진행하였다.
Entity Pattern 과 Repository Pattern을 적용하기 위해 Spring Data REST 의 RestRepository 를 적용하였다.

app 서비스의 EduApplication.java

```java

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
```

 app 서비스의 PolicyHandler.java

```java
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

```

 app 서비스의 BookingRepository.java


```java
package edu;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="eduApplications", path="eduApplications")
public interface EduApplicationRepository extends PagingAndSortingRepository<EduApplication, Long>{
    EduApplication findByAppId(Long appId);

}
```

DDD 적용 후 REST API의 테스트를 통하여 정상적으로 동작하는 것을 확인할 수 있었다.
***
## Gateway 적용
API GateWay를 통하여 마이크로 서비스들의 진입점을 통일할 수 있다. 
다음과 같이 GateWay를 적용하였다.

```yaml
server:
  port: 8088

---

spring:
  profiles: default
  cloud:
    gateway:
      routes:
        - id: app
          uri: http://localhost:8081
          predicates:
            - Path=/eduApplications/** 
        - id: education
          uri: http://localhost:8082
          predicates:
            - Path=/educations/** 
        - id: mypage
          uri: http://localhost:8083
          predicates:
            - Path= /mypages/**
        - id: payment
          uri: http://localhost:8084
          predicates:
            - Path=/payments/** 
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "*"
            allowedMethods:
              - "*"
            allowedHeaders:
              - "*"
            allowCredentials: true

---

spring:
  profiles: docker
  cloud:
    gateway:
      routes:
        - id: app
          uri: http://app:8080
          predicates:
            - Path=/eduApplications/** 
        - id: education
          uri: http://education:8080
          predicates:
            - Path=/educations/** 
        - id: mypage
          uri: http://mypage:8080
          predicates:
            - Path= /mypages/**
        - id: payment
          uri: http://payment:8080
          predicates:
            - Path=/payments/** 
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "*"
            allowedMethods:
              - "*"
            allowedHeaders:
              - "*"
            allowCredentials: true

server:
  port: 8080
```  
***
## CQRS
타 마이크로서비스의 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이)도 내 서비스의 수강 신청 내역 조회가 가능하게 구현해 두었다.
본 프로젝트에서 View 역할은 mypage 서비스가 수행한다.

수강 신청 후 mypage 조회
![image](https://user-images.githubusercontent.com/18115456/122647383-fb689c00-d15e-11eb-86a8-37a503a45bfd.PNG)
<image src="https://user-images.githubusercontent.com/18115456/122647384-fc99c900-d15e-11eb-8e65-7c63617eaf63.PNG" width="60%" />
*** 

## 폴리글랏 퍼시스턴스
mypage 서비스에서 타 서비스들과 다른 DB를 사용하여 MSA간 서로 다른 종류의 DB간에도 문제 없이 동작하여 다형성을 만족하는지 확인하였다.
(폴리글랏을 만족)

|서비스|DB|pom.xml|
| :--: | :--: | :--: |
|app| H2 |![image](https://user-images.githubusercontent.com/2360083/121104579-4f10e680-c83d-11eb-8cf3-002c3d7ff8dc.png)|
|payment| H2 |![image](https://user-images.githubusercontent.com/2360083/121104579-4f10e680-c83d-11eb-8cf3-002c3d7ff8dc.png)|
|education| H2 |![image](https://user-images.githubusercontent.com/2360083/121104579-4f10e680-c83d-11eb-8cf3-002c3d7ff8dc.png)|
|mypage| HSQL |![image](https://user-images.githubusercontent.com/2360083/120982836-1842be00-c7b4-11eb-91de-ab01170133fd.png)|
***

## 동기식 호출과 Fallback 처리
수강신청(app)->결제(payment) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 호출 프로토콜은 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다.

app 서비스 내 external.PaymentService

```java
package edu.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@FeignClient(name="payment", url="${api.url.Payment}")
public interface PaymentService {

    @RequestMapping(method= RequestMethod.POST, path="/payments")
    public void pay(@RequestBody Payment payment);

}
```

app 서비스 내에서 pay() 호출

```java
edu.external.Payment payment = new edu.external.Payment();
payment.setAppId(eduApplied.getAppId());
payment.setEduId(eduApplied.getEduId());
payment.setEduName(eduApplied.getEduName());
payment.setStatus("Paid");
payment.setUserId(eduApplied.getUserId());
AppApplication.applicationContext.getBean(edu.external.PaymentService.class)
    .pay(payment);
```

동작 확인  
- Payment 서비스 내린 후, 수강신청시 에러 발생
![image](https://user-images.githubusercontent.com/18115456/122647658-4d5df180-d160-11eb-9110-487e41ad71ba.PNG)

  
***
# 운영  
## Deploy/ Pipeline
각 구현체들은 각자의 source repository 에 구성되었고, 사용한 CI/CD 플랫폼은 Azure를 사용하였으며, pipeline build script 는 각 프로젝트 폴더 이하에 cloudbuild.yml 에 포함되었다.

- git에서 소스 가져오기

```
git clone https://github.com/EunmiKang/edu.git
```

- Build 하기

```bash
cd app
mvn package

cd education
mvn package

cd payment
mvn package

cd mypage
mvn package

cd gateway
mvn package
```

- Docker Image Push/deploy/service 생성 (yml이용)

```sh
-- 기본 namespace 설정 (혹시 kubectl 명령어 칠 때 namespace 빼먹을까봐)
kubectl config set-context --current --namespace=edu

-- namespace 생성
kubectl create ns edu

cd app
az acr build --registry eunmi-acr --image eunmi-acr.azurecr.io/app:v1 .
cd kubernetes
kubectl apply -f deployment.yml
kubectl apply -f service.yaml

cd payment
az acr build --registry eunmi-acr --image eunmi-acr.azurecr.io/payment:v1 .
cd kubernetes
kubectl apply -f deployment.yml
kubectl apply -f service.yaml

cd education
az acr build --registry eunmi-acr --image eunmi-acr.azurecr.io/education:v1 .
cd kubernetes
kubectl apply -f deployment.yml
kubectl apply -f service.yaml

cd mypage
az acr build --registry eunmi-acr --image eunmi-acr.azurecr.io/mypage:v1 .
cd kubernetes
kubectl apply -f deployment.yml
kubectl apply -f service.yaml

cd gateway
az acr build --registry eunmi-acr --image eunmi-acr.azurecr.io/gateway:v1 .
cd kubernetes
kubectl apply -f deployment.yml -n edu
kubectl apply -f service.yaml

```

- edu/gateway/kubernetes/deployment.yml 파일 

```yml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: gateway
  namespace: edu
  labels:
    app: gateway
spec:
  replicas: 1
  selector:
    matchLabels:
      app: gateway
  template:
    metadata:
      labels:
        app: gateway
    spec:
      containers:
        - name: gateway
          image: skccanticorona.azurecr.io/gateway:latest
          ports:
            - containerPort: 8080
```	  

- edu/gateway/kubernetes/service.yaml 파일 

```yml
apiVersion: v1
kind: Service
metadata:
  name: gateway
  namespace: anticorona
  labels:
    app: gateway
spec:
  ports:
    - port: 8080
      targetPort: 8080
  type: LoadBalancer
  selector:
    app: gateway
```	  

- deploy 완료  
![image]

***

## Config Map

- 변경 가능성이 있는 설정을 ConfigMap을 사용하여 관리  
  - app 서비스에서 바라보는 paynebt 서비스 url을 ConfigMap 사용하여 구현​  

- in app src (app/src/main/java/edu/external/PaymentService.java)  
    ![image](https://user-images.githubusercontent.com/18115456/122647963-e04b5b80-d161-11eb-9d8a-7e3d2fb24165.png)

- app application.yml (app/src/main/resources/application.yml)​  
    ![image](https://user-images.githubusercontent.com/18115456/122648000-096bec00-d162-11eb-8a25-27db38b77f09.PNG)

- app deploy yml (app/kubernetes/deployment.yml)  
    ![image](https://user-images.githubusercontent.com/18115456/122648105-7b443580-d162-11eb-9dd7-eec1529f1419.PNG)

- configmap 생성 후 조회

    ```sh
    kubectl create configmap paymenturl --from-literal=url=http://payment:8080 -n edu
    ```
    ![image]

- configmap 삭제 후, 에러 확인  

    ```sh
    kubectl delete configmap paymenturl
    ```
    ![image] 

***

## Autoscale (HPA)
***
## Circuit Breaker


***
## Zero-Downtime deploy (Readiness Probe)
- deployment.yml에 정상 적용되어 있는 readinessProbe  
```yml
readinessProbe:
  httpGet:
    path: '/actuator/health'
    port: 8080
  initialDelaySeconds: 10
  timeoutSeconds: 2
  periodSeconds: 5
  failureThreshold: 10
```

- deployment.yml에서 readiness 설정 제거 후, 배포중 siege 테스트 진행  
    ![image]

- 다시 readiness 정상 적용 후, Availability 100% 확인  
![image]

***
## Self-healing (Liveness Probe)

- deployment.yml에 정상 적용되어 있는 livenessProbe  

```yml
livenessProbe:
  httpGet:
    path: '/actuator/health'
    port: 8080
  initialDelaySeconds: 120
  timeoutSeconds: 2
  periodSeconds: 5
  failureThreshold: 5
```

- port 및 path 잘못된 값으로 설정해놓은 deploy로 다시 배포 후, retry 시도 확인 (in app 서비스)  
    - app deploy 재배포 (아래와 같이 잘못된 값으로 설정해놓음)   
        ![image](https://user-images.githubusercontent.com/18115456/120985806-ed0d9e00-c7b6-11eb-834f-ffd2c627ecf0.png)

    - retry 시도 확인  
        ![image]
