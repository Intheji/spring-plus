# 🌱 Spring Plus

> 스프링 심화 학습 과제를 수행하며 `spring-plus` 프로젝트를 개선한 기록입니다.  
> 기능 구현부터 테스트, 보안, 쿼리 최적화, 대용량 데이터 처리, AWS 활용까지 단계적으로 적용했습니다.

<br/>

## 🧾 프로젝트 소개

`Spring Boot` 기반의 일정 관리 프로젝트입니다.  
유저, 할 일(Todo), 댓글(Comment), 담당자(Manager) 기능을 중심으로 동작하며,  
과제를 진행하면서 아래 내용을 직접 구현하고 개선했습니다.

- `@Transactional` 동작 방식 이해 및 수정
- JWT 기반 인증/인가 개선
- JPA / QueryDSL 기반 조회 최적화
- Controller 테스트 수정
- AOP 동작 수정
- Cascade 적용
- N+1 문제 해결
- Spring Security 도입
- 대용량 데이터 처리 및 검색 성능 개선
- AWS 기반 배포 환경 구성

<br/>

## 🛠 기술 스택

### Backend
- Java 17
- Spring Boot 3.3.3
- Spring Data JPA
- Spring Security
- QueryDSL
- JWT

### Database
- MySQL

### Test
- JUnit5
- Spring Boot Test
- Spring Security Test

### Infra
- AWS EC2
- AWS RDS
- AWS S3

<br/>

## 📁 프로젝트 구조

```text
src/main/java/org/example/expert
├─ aop
├─ config
├─ domain
│  ├─ auth
│  ├─ comment
│  ├─ common
│  ├─ log
│  ├─ manager
│  ├─ todo
│  └─ user
└─ ExpertApplication.java
```

<br/>

## 🚀 실행 방법

### 1. 프로젝트 클론

```bash
git clone {본인 레포지토리 주소}
cd spring-plus
```

### 2. DB 실행 및 설정

`application.yml` 기준 MySQL 설정 예시입니다.

```yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/plus
    username: {DB_USERNAME}
    password: {DB_PASSWORD}
```

### 3. 애플리케이션 실행

```bash
./gradlew bootRun
```

또는 IntelliJ에서 `ExpertApplication` 실행

<br/>

## ✅ 구현 내용

---

## 1. `@Transactional` 이해 및 수정 🧩

### 문제
`/todos` 저장 API 호출 시 아래와 같은 에러가 발생했습니다.

```text
Connection is read-only. Queries leading to data modification are not allowed
```

### 원인
서비스 계층에 `readOnly = true`가 적용된 상태에서 쓰기 작업이 수행되고 있었습니다.

### 해결
- 저장/수정이 필요한 메서드에는 별도로 `@Transactional`을 적용해 쓰기 트랜잭션으로 동작하도록 수정했습니다.

### 결과
- 할 일 저장 API 정상 동작

<br/>

---

## 2. JWT에 nickname 추가 🔐

### 요구사항
- `User` 테이블에 `nickname` 컬럼 추가
- JWT에서 유저 닉네임을 꺼내 화면에 표시 가능해야 함

### 적용 내용
- `User` 엔티티에 `nickname` 필드 추가
- 회원가입 / 로그인 시 닉네임 반영
- JWT 생성 시 `nickname` claim 추가
- 인증 객체 생성 시 nickname 사용 가능하도록 수정

### 결과
- 프론트엔드에서 JWT 기반으로 닉네임 활용 가능

<br/>

---

## 3. JPA 검색 조건 확장 ☁️

### 요구사항
- `weather` 조건 검색
- 수정일 기준 기간 검색
- JPQL 사용

### 적용 내용
- 선택 조건(`weather`, 시작일, 종료일)에 따라 검색 가능하도록 JPQL 기반 로직 구성
- 조건이 없을 수도 있는 케이스를 고려하여 유연하게 처리

### 결과
- 날씨 / 기간 조건을 포함한 일정 검색 가능

<br/>

---

## 4. 컨트롤러 테스트 수정 🧪

### 문제
실패하던 테스트:
- `todo_단건_조회_시_todo가_존재하지_않아_예외가_발생한다()`

### 해결
- 테스트가 실제 예외 응답 형식과 맞도록 수정
- 기대 상태 코드 및 응답 검증 로직 보완

### 결과
- 테스트 통과

<br/>

---

## 5. AOP 수정 📌

### 문제
`UserAdminController.changeUserRole()` 실행 전 동작해야 하는 AOP가 의도대로 적용되지 않았습니다.

### 해결
- 포인트컷 표현식을 점검하고, 정확한 메서드 실행 전에 동작하도록 수정했습니다.

### 결과
- 관리자 권한 변경 API 호출 전 정상 로깅 수행

<br/>

---

## 6. JPA Cascade 적용 🔗

### 요구사항
- 할 일을 생성한 유저는 담당자로 자동 등록되어야 함

### 해결
- Todo 저장 시 연관된 Manager 엔티티가 함께 저장될 수 있도록 Cascade 적용

### 결과
- 할 일 생성 시 담당자 자동 등록

<br/>

---

## 7. N+1 문제 해결 🚨

### 문제
`CommentController.getComments()` 호출 시 N+1 문제가 발생

### 해결
- 연관 엔티티 조회 시 fetch join 또는 적절한 조회 전략 적용
- 필요한 연관 데이터를 한 번에 가져오도록 수정

### 결과
- 불필요한 추가 쿼리 제거
- 댓글 조회 성능 개선

<br/>

---

## 8. QueryDSL 적용 🔍

### 요구사항
- JPQL로 작성된 `findByIdWithUser`를 QueryDSL로 변경
- N+1 문제가 발생하지 않도록 처리

### 해결
- QueryDSL로 조건식과 join을 명확하게 작성
- 사용자 정보가 함께 필요한 조회는 fetch join으로 처리


<br/>

---

## 9. Spring Security 도입 🛡️

### 요구사항
- 기존 Filter + Argument Resolver 구조를 Spring Security 기반으로 변경
- JWT 인증 방식 유지
- 권한 처리 유지

### 적용 내용
- Spring Security 설정 추가
- JWT 기반 인증 필터 적용
- 인증 객체를 Security Context에 저장하도록 변경
- 권한 처리를 Spring Security 방식으로 통합

### 결과
- 인증/인가 구조가 프레임워크 표준 방식으로 정리됨

<br/>

---

## 10. QueryDSL 검색 기능 구현 🧠

### 요구사항
- 새로운 검색 API 구현
- 제목 부분 검색
- 생성일 범위 검색
- 담당자 닉네임 부분 검색
- 최신순 정렬
- 담당자 수 / 댓글 수 포함
- 페이징 처리
- Projection 활용

### 적용 내용
- QueryDSL + Projection을 사용하여 필요한 필드만 반환
- 검색 조건을 조합 가능한 형태로 구성
- 검색 응답 DTO에 제목, 담당자 수, 댓글 수 포함


<br/>

---

## 11. Transaction 심화 🧾

### 요구사항
- 매니저 등록 요청 시 로그는 항상 남아야 함
- 매니저 등록은 실패할 수 있음
- 로그 저장은 독립적으로 처리되어야 함

### 해결
- 로그 저장 로직을 별도 트랜잭션으로 분리
- `@Transactional` 옵션을 활용하여 요청 로그는 항상 저장되도록 구성

### 결과
- 비즈니스 로직 실패 여부와 무관하게 로그 기록 보장

<br/>

---

## 12. AWS 활용 ☁️

### 12-1. EC2

#### 적용 내용
- EC2 인스턴스를 생성하고 애플리케이션을 배포
- Elastic IP를 연결하여 고정된 주소로 외부 접속이 가능하도록 구성
- Spring Boot Actuator를 활용하여 health check API를 외부에 공개

#### 배포 정보
- EC2 Public IP: `3.35.187.16`
- Health Check API: `http://3.35.187.16:8080/actuator/health`

#### 첨부
<details>
<summary>EC2 관련 이미지 보기</summary>

#### 1. EC2 인스턴스 생성 화면
<img width="1796" height="995" alt="image" src="https://github.com/user-attachments/assets/f37e6cca-e098-4d67-b3a6-e4b1d031ffd4" />

#### 2. EC2 보안 그룹 설정 화면
<img width="1882" height="853" alt="image" src="https://github.com/user-attachments/assets/1023cfae-a7cc-4555-a600-be7cdc1df56f" />

#### 3. Elastic IP 연결 화면
<img width="1882" height="621" alt="image" src="https://github.com/user-attachments/assets/849bf96d-4431-44fe-b448-299e108869f0" />

#### 4. Health Check 응답 화면
<img width="653" height="259" alt="image" src="https://github.com/user-attachments/assets/81bc97c4-1f27-449b-a6de-a15374bcac44" />

</details>

### 12-2. RDS

#### 적용 내용
- MySQL 기반 RDS 인스턴스를 생성
- EC2와 동일한 VPC 내에서 연결되도록 구성
- RDS 보안 그룹에서 EC2 보안 그룹만 접근할 수 있도록 설정하여 DB 접근 대상을 제한
- 애플리케이션의 datasource를 RDS 엔드포인트 기반으로 변경하여 실제 DB와 연동

#### 배포 정보
- DB 엔드포인트: `spring-plus.cfku2cs2woku.ap-northeast-2.rds.amazonaws.com`
- DB 종류: `MySQL`
- DB 이름: `plus`

#### 첨부
<details>
<summary>RDS 관련 이미지 보기</summary>

#### 1. RDS 인스턴스 생성 화면
<img width="1767" height="307" alt="image" src="https://github.com/user-attachments/assets/11dc1a68-bab5-4327-90a1-c4172c403ac9" />

#### 2. RDS 보안 그룹 설정 화면
<img width="2456" height="809" alt="image" src="https://github.com/user-attachments/assets/eee8f264-703e-4559-858a-dc1fb3d3a619" />

#### 3. EC2-RDS 연결 정보 화면
<img width="1764" height="776" alt="image" src="https://github.com/user-attachments/assets/3653fc17-8a27-47cf-8124-10345706db85" />

</details>

### 12-3. S3

#### 적용 내용
- S3 버킷을 생성하여 프로필 이미지 업로드 기능을 구현
- 업로드된 파일은 `profile/UUID.확장자` 형식의 key로 저장되도록 구성
- 버킷을 퍼블릭으로 공개하지 않고 Presigned URL을 발급하여 일정 시간 동안만 파일 접근이 가능하도록 구현
- Presigned URL 만료 시간은 5일로 설정

#### API 정보
- 업로드 API: `POST /users/profile-image`
- 인증 방식: JWT Bearer Token 필요

#### 버킷 정보
- Bucket Name: `spring-plus-profile-155326049459-ap-northeast-2-an`

#### 업로드 결과 예시
- 업로드 후 Presigned URL을 응답으로 반환
- 예시 응답: [Presigned URL 바로가기](https://spring-plus-profile-155326049459-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/profile/9c7810b4-1b79-4f46-9270-605d092cb9c1.webp?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20260405T165844Z&X-Amz-SignedHeaders=host&X-Amz-Credential=AKIASIKRHNSZ22WNON5M%2F20260405%2Fap-northeast-2%2Fs3%2Faws4_request&X-Amz-Expires=432000&X-Amz-Signature=97714fd504911a0db31c1505906e09650a238252bc2946d8670512f5ac9765e3)


<details>
<summary>S3 관련 이미지 보기</summary>

#### 1. S3 버킷 생성 화면
<img width="1672" height="469" alt="image" src="https://github.com/user-attachments/assets/a5128d21-6c38-4df0-8430-2259ec9e1bbf" />

#### 2. S3 권한 설정 화면
<img width="2450" height="709" alt="image" src="https://github.com/user-attachments/assets/dabf6774-52f8-49e7-a6c3-b197aaeec701" />

#### 3. 업로드 API 성공 응답 화면
<img width="1199" height="582" alt="image" src="https://github.com/user-attachments/assets/41a33d3e-efe1-4ee6-9612-289d8004e193" />

#### 4. S3 객체 저장 화면
<img width="2455" height="570" alt="image" src="https://github.com/user-attachments/assets/7ec57cb3-ba18-46c3-bc5d-3f5bb020a58e" />
<img width="1451" height="1359" alt="image" src="https://github.com/user-attachments/assets/d1ddd294-6f3b-45ad-8bb3-7a495c753e82" />



</details>

<br/>

---

## 13. 대용량 데이터 처리 🚀

### 13-1. 500만 건 유저 데이터 생성

#### 요구사항
- 테스트 코드로 유저 데이터 500만 건 생성
- JDBC Bulk Insert 사용
- 닉네임 랜덤 생성
- 중복 최소화

#### 적용 내용
- `JdbcTemplate` 기반 batch insert 구현
- 고정 prefix + 랜덤 값 + 시퀀스 조합으로 닉네임 생성
- batch size를 설정하여 대량 데이터 삽입

#### 예시 닉네임
```text
user_0000_1axge
user_0001_2dlwt
user_0002_hckt
```

#### 결과
- 500만 건 유저 데이터 생성 완료~

<br/>

### 13-2. 닉네임 정확 일치 검색 API 구현

#### 요구사항
- 닉네임 조건으로 유저 목록 검색
- 정확히 일치해야 검색 가능

#### 적용 내용
- `/users`
- `nickname` 파라미터를 사용해 정확히 일치하는 유저 검색
- 페이지네이션 적용

#### 예시 요청
```http
GET /users?nickname=user_0000_1axge&page=1&size=10
```

<br/>

### 13-3. 검색 성능 개선 과정

#### 1) 인덱스 적용 전/후 비교

| 구분 | 측정 결과(ms) | 평균(ms) | 비고 |
|---|---:|---:|---|
| 인덱스 적용 전 | 1968 | 1968.0 | `nickname` 인덱스 없음 |
| 인덱스 적용 후 | 5, 5, 2, 5, 5 | 4.4 | 서로 다른 닉네임 5건 기준 |

#### 2) `Page` 와 `Slice` 비교

| 방식 | 측정 결과(ms) | 평균(ms) | 비고 |
|---|---:|---:|---|
| Page | 5, 5, 2, 5, 5 | 4.4 | 전체 개수 포함 |
| Slice | 2, 2, 2, 2, 2 | 2.0 | 다음 페이지 여부만 확인 |

#### 3) 엔티티 조회와 Projection 조회 비교

| 방식 | 측정 결과(ms) | 평균(ms) | 비고 |
|---|---:|---:|---|
| 기존 Page + 엔티티 조회 | 4, 5, 2, 5, 2 | 3.6 | 엔티티 전체 조회 후 DTO 변환 |
| Page + Projection 조회 | 11, 1, 1, 1, 1 | 3.0 | 필요한 컬럼만 조회 |

#### SQL 비교

기존 방식은 `User` 엔티티 전체 컬럼을 조회했습니다.

```sql
select id, created_at, email, modified_at, nickname, password, user_role
from users
where nickname = ?
limit ?
```

Projection 적용 후에는 응답에 필요한 컬럼만 조회하도록 변경했습니다.

```sql
select id, email
from users
where nickname = ?
limit ?
```

#### 성능 개선 정리
- 가장 큰 개선 효과는 `nickname` 인덱스 추가에서 확인
- 이후 `Slice`, `Projection` 방식으로 추가 최적화를 진행
- 이미 인덱스 적용 후 충분히 빠른 상태였기 때문에 후속 개선은 그냥 미세한 최적화에 가까웠음

<br/>

## 📊 주요 성능 비교 요약

| 항목 | 평균(ms) |
|---|---:|
| 인덱스 적용 전 | 1968.0 |
| 인덱스 적용 후(Page) | 4.4 |
| Slice 적용 후 | 2.0 |
| Projection 적용 후(Page) | 3.0 |






<br/>

## 👩‍💻 블로그

- https://ggoongdeng.tistory.com/256
