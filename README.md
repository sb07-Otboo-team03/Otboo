# Otboo
옷장을 부탁해
개인화 의상 및 아이템 추천 SaaS

코드잇 3팀 고급 프로젝트

## 작품 소개
사용자의 보유 의상, 날씨, 취향을 기반으로 의상 조합을 추천해주고,
OOTD 피드와 팔로우 등의 소셜 기능을 갖춘 서비스입니다.

## 기술 스택
**Backend**
- Spring Boot
- Spring Security (JWT)
- Spring Data JPA
- QueryDSL

**Database**
- PostgreSQL
- H2

**Infra & DevOps**
- AWS ECS
- Redis Cache
- Docker / Docker Compose
- GitHub Actions (CI/CD)
- Spring Batch
- Elasticsearch
- API & Docs
- springdoc-openapi (Swagger)

##  팀원 구성
| 이름 | 역할 | 담당 |
|------|------|------|
| [이정훈](https://github.com/mij9929) | Backend | 사용자 및 프로필 관리, 인증/인가, Redis Cache |
| [최지혜](https://github.com/ChoiJiHye950) | Backend | 의상 관리, 파일 관리 |
| [조성만](https://github.com/BetterCodings) | Backend | 날씨 관리, Spring Batch |
| [김유미](https://github.com/yuuum0214) | Backend | 의상 속성 정의, 의상 추천 |
| [박재완](https://github.com/gnara0719) | Backend | 피드 관리, 좋아요 및 댓글 관리 |
| [장미연](https://github.com/spring7th) | Backend | 팔로우 및 DM, 알림 기능 |

## ⭐ 주요 기능
### 날씨 기반 의상 추천
- 사용자 위치 기반 날씨 데이터를 수집

### 개인화 의상 관리 및 추천
- 사용자가 보유한 의상을 등록 및 관리
- 날씨, 취향을 반영한 맞춤형 추천 알고리즘 제공

### OOTD 피드
- 추천 코디를 피드로 공유
- 좋아요 및 댓글을 통한 사용자 간 상호작용

### 팔로우 및 실시간 DM
- 사용자 간 팔로우 기능
- WebSocket 기반 실시간 메시징

### 실시간 알림 시스템
- SSE 기반 알림 제공
- 좋아요, 댓글, 팔로우, DM, 날씨 변화 이벤트 알림

## 프로젝트 구조
```
otboo/
├── domain/
│   ├── binarycontent/
│   ├── clothes/
│   ├── comment/
│   ├── directMessage/
│   ├── feed/
│   ├── follow/
│   ├── kafka/
│   ├── like/
│   ├── notification/
│   ├── profile/
│   ├── sse/
│   ├── user/
│   ├── weather/
│   ├── websocket/
│   ├── notification/
│   ├── BaseEntity
│   └── BaseUpdatableEntity
├── global/
│   ├── aop/
│   ├── async/
│   ├── config/
│   ├── event/
│   ├── exception/
│   ├── filter/
│   ├── init/
│   ├── properties/
│   ├── security/
│   ├── slice/
│   └── util/
```

