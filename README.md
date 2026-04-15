# 👕 Otboo

<p align="center">
  <img width="300" src="https://github.com/sb07-Otboo-team03/Otboo/blob/dev/api/src/main/resources/static/assets/Logo-wa1Pp3bf.svg" />
</p>

<p align="center">
  <b>개인화 의상 및 아이템 추천 SaaS</b>
</p>

<p align="center">
  코드잇 3팀 고급 프로젝트<br/>
  <sub>2026.03.10 ~ 2026.04.17</sub>
</p>

<hr/>

## 작품 소개
> 💡 이 프로젝트는 날씨 + 개인화 추천을 결합한 서비스입니다.  
'옷장을 부탁해'는 사용자의 보유 의상, 날씨, 취향을 기반으로 의상 조합을 추천해주고,  
OOTD 피드와 팔로우 등의 소셜 기능을 제공합니다

<hr/>

## 🛠 기술 스택

### 🚀 Backend
![Spring Boot](https://img.shields.io/badge/SpringBoot-6DB33F?style=flat&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Security-6DB33F?style=flat&logo=springsecurity&logoColor=white)
![JPA](https://img.shields.io/badge/JPA-59666C?style=flat)
![QueryDSL](https://img.shields.io/badge/QueryDSL-000000?style=flat)

### 🗄 Database
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?logo=postgresql&logoColor=white)
![H2](https://img.shields.io/badge/H2-09476B?style=flat)

### ⚙️ Infra & DevOps
![AWS ECS](https://img.shields.io/badge/AWS_ECS-FF9900?logo=amazonaws&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?logo=githubactions&logoColor=white)
![Elasticsearch](https://img.shields.io/badge/Elasticsearch-005571?logo=elasticsearch)

<hr/>

##  팀원 구성
| 이름 | 역할 | 담당 |
|------|------|------|
| [이정훈](https://github.com/mij9929) | Backend | 사용자 및 프로필 관리, 인증/인가, Redis Cache |
| [최지혜](https://github.com/ChoiJiHye950) | Backend | 의상 관리, 파일 관리 |
| [조성만](https://github.com/BetterCodings) | Backend | 날씨 관리, Spring Batch |
| [김유미](https://github.com/yuuum0214) | Backend | 의상 속성 정의, 의상 추천 |
| [박재완](https://github.com/gnara0719) | Backend | 피드 관리, 좋아요 및 댓글 관리 |
| [장미연](https://github.com/spring7th) | Backend | 팔로우 및 DM, 알림 기능 |

<hr/>

## ⭐ 주요 기능

### 🌦 날씨 기반 의상 추천
- 사용자 위치 기반 날씨 데이터 수집

### 👗 개인화 의상 관리
- 의상 등록 및 관리
- 취향 기반 추천 알고리즘 제공

### 📸 OOTD 피드
- 코디 공유
- 좋아요 / 댓글 인터랙션

### 💬 실시간 DM
- WebSocket 기반 채팅

### 🔔 실시간 알림
- SSE 기반 이벤트 알림

<hr/>

## 🗂 프로젝트 구조

<details>
<summary>📁 전체 구조 보기</summary>
  
```
otboo/
├── api/
│   ├── domain/
│   │   ├── binarycontent/     # 파일/이미지 관리
│   │   ├── clothes/           # 의류
│   │   ├── comment/           # 댓글
│   │   ├── directMessage/     # 개인메시지
│   │   ├── feed/              # 피드
│   │   ├── follow/            # 팔로우
│   │   ├── kafka/             # Kafka
│   │   ├── like/              # 좋아요
│   │   ├── notification/      # 알림
│   │   ├── profile/           # 프로필
│   │   ├── sse/               # SSE 통신
│   │   ├── user/              # 사용자
│   │   ├── weather/           # 날씨
│   │   └── websocket/         # WebSocket 통신
│   │
│   ├── global/
│   │   ├── aop/               # AOP 처리
│   │   ├── async/             # 비동기 처리
│   │   ├── config/            # 설정
│   │   ├── event/             # 이벤트 처리
│   │   ├── exception/         # 예외 처리
│   │   ├── filter/            # 필터
│   │   ├── init/              # 초기 데이터 세팅
│   │   ├── properties/        # 설정 프로퍼티
│   │   ├── security/          # 인증/인가
│   │   ├── slice/             # 페이지네이션
│   │   └── util/              # 유틸리티
│
├── batch/
│   └── weather/               # 날씨 배치 작업
│
├── common/
│   ├── notification/          # 공통 알림 모듈
│   └── weather/               # 공통 날씨 모듈

```
</details>
<hr/>

## 🎞 DEMO
- 서비스 배포 링크: [otbooteam03.cloud](https://otbooteam03.cloud/#/auth/login)
- 서비스 시연 영상:
