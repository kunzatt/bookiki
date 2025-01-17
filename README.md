# 📚 LoveYourShelf
> *스마트한 도서관 경험을 위한 무인 관리 시스템*

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=flat-square&logo=Spring%20Boot&logoColor=white)](/) 
[![React](https://img.shields.io/badge/React-61DAFB?style=flat-square&logo=React&logoColor=black)](/)
[![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=flat-square&logo=TypeScript&logoColor=white)](/)
[![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=Redis&logoColor=white)](/)
[![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=MySQL&logoColor=white)](/)
[![AWS](https://img.shields.io/badge/AWS-232F3E?style=flat-square&logo=Amazon%20AWS&logoColor=white)](/)

## 📋 목차
1. [프로젝트 소개](#-프로젝트-소개)
2. [시스템 아키텍처](#-시스템-아키텍처)
3. [기술 스택](#-기술-스택)
4. [주요 기능](#-주요-기능)
5. [팀원 소개](#-팀원-소개)
6. [프로젝트 현황](#-프로젝트-현황)

## 🎯 프로젝트 소개

### 개요
LoveYourShelf는 상주 사서 없이 운영되는 스마트 도서관 시스템입니다. QR코드와 IoT 기술을 결합하여 도서 관리를 자동화하고, 사용자 경험을 혁신적으로 개선합니다.

### 특징
| 기능 | 설명 |
|------|------|
| 📱 QR 기반 관리 | QR코드를 통한 간편한 도서 대출/반납 |
| 🎨 직관적 분류 | 장르별 컬러 코딩 시스템 |
| 📸 실시간 추적 | 라즈베리파이 카메라로 도서 위치 실시간 모니터링 |
| 💡 LED 안내 | LED 선을 통한 직관적인 도서 위치 안내 |
| ⚡ 자동화 시스템 | 완전 자동화된 대출/반납 프로세스 |
| 🔔 실시간 알림 | 즉각적인 오류 감지 및 관리자 알림 |




## 🔍 시스템 아키텍처
```mermaid
flowchart TD
    subgraph Client
        A[Web Application] --> B[QR Scanner]
    end
    subgraph IoT
        C[Raspberry Pi] --> D[Camera System]
        C --> E[LED Control]
    end
    subgraph Backend
        F[Spring Boot API] --> G[(MySQL)]
        F --> H[(Redis)]
    end
    A <--> F
    C <--> F
```

## 🛠 기술 스택

### Frontend
| 분류         | 기술                                                                                                             |
|--------------|------------------------------------------------------------------------------------------------------------------|
| 프레임워크   | [![React](https://img.shields.io/badge/React-61DAFB?style=flat-square&logo=React&logoColor=black)](/)           |
| 언어         | [![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=flat-square&logo=TypeScript&logoColor=white)](/) |
| 상태관리     | [![Redux](https://img.shields.io/badge/Redux-764ABC?style=flat-square&logo=Redux&logoColor=white)](/)           |
| 빌드 도구    | [![Vite](https://img.shields.io/badge/Vite-646CFF?style=flat-square&logo=Vite&logoColor=white)](/)             |

### Backend
| 분류         | 기술                                                                                                               |
|--------------|--------------------------------------------------------------------------------------------------------------------|
| 언어         | [![Java](https://img.shields.io/badge/Java-007396?style=flat-square&logo=Java&logoColor=white)](/)                 |
| 프레임워크   | [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=flat-square&logo=Spring%20Boot&logoColor=white)](/) |
| 데이터베이스 | [![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=MySQL&logoColor=white)](/)             |
| 캐시         | [![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=Redis&logoColor=white)](/)             |
| ORM          | [![JPA](https://img.shields.io/badge/JPA-6DB33F?style=flat-square&logo=Hibernate&logoColor=white)](/)             |
| 서버         | [![NGINX](https://img.shields.io/badge/NGINX-009639?style=flat-square&logo=NGINX&logoColor=white)](/)             |
| 인증         | [![JWT](https://img.shields.io/badge/JWT-000000?style=flat-square&logo=JSON%20Web%20Tokens&logoColor=white)](/)   |

### DevOps
| 분류         | 도구                                                                                                             |
|--------------|------------------------------------------------------------------------------------------------------------------|
| 버전관리     | [![GitLab](https://img.shields.io/badge/GitLab-FC6D26?style=flat-square&logo=GitLab&logoColor=white)](/)         |
| 프로젝트관리 | [![Jira](https://img.shields.io/badge/Jira-0052CC?style=flat-square&logo=Jira&logoColor=white)](/)              |
| 컨테이너     | [![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=Docker&logoColor=white)](/)         |
| CI/CD        | [![Jenkins](https://img.shields.io/badge/Jenkins-D24939?style=flat-square&logo=Jenkins&logoColor=white)](/)     |
| 클라우드     | [![AWS](https://img.shields.io/badge/AWS-232F3E?style=flat-square&logo=Amazon%20AWS&logoColor=white)](/)        |



## 💫 주요 기능

### 회원 관리
```mermaid
graph LR
    A[회원가입] --> B{계정 유형}
    B -->|이메일| C[이메일 인증]
    B -->|SNS| D[OAuth 인증]
    C --> E[계정 생성]
    D --> E
```

### 도서 관리 프로세스
```mermaid
graph TD
    A[도서 등록] -->|ISBN 스캔| B[도서 정보 입력]
    B --> C[QR코드 생성]
    C --> D[위치 배정]
    D --> E[시스템 등록 완료]
```

## 👥 팀원 소개

| 이름 | 역할 | 담당 |
|------|------|------|
| 강수진 | Developer | Backend / Frontend |
| 김용명 | Developer | Backend / Frontend |
| 이동욱 | Developer | Backend / Frontend |
| 차윤영 | Developer | Backend / Frontend |
| 배남석석 | EM | Engineering Manager / AI |

## 📈 프로젝트 현황

### 진행상황
☑️ 팀 미팅 완료  
☑️ 기능 명세서 작성  
☑️ API 명세서 작성  
☑️ DB 설계  
☑️ BE 컨벤션 작성  
☑️ UX/UI 프로토타입 제작  
☑️ CI/CD 구축


---
© 2025 LoveYourShelf. All Rights Reserved.