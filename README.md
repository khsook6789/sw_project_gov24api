# sw_project_gov24api

공공데이터(Open API) 중 Gov24(정부24) 관련 API를 활용해 데이터를 조회/가공하고, 애플리케이션(또는 API 서버) 형태로 제공하는 프로젝트입니다.

> 목적: 공공데이터 API 연동 경험, 인증키 관리, 요청/응답 파싱, 예외처리

---

## ✨ 주요 기능
- Gov24(정부24) Open API 호출 (인증키 기반)
- 요청 파라미터(검색어/페이지/정렬 등) 구성 및 전송
- 응답(XML/JSON) 파싱 및 DTO 매핑
- 결과 리스트/상세 조회 기능
- 예외 처리(타임아웃/인증 실패/형식 오류) 및 로깅
- REST API로 래핑하여 프론트/외부에서 호출 가능

---

## 🧱 기술 스택
- Language: Java
- Build: Gradle
- Framework: Spring Boot
- Logging: SLF4J + Logback

---

## 📁 프로젝트 구조

```bash
src/
 └─ main/
    ├─ java/
    │   ├─ .../config        # 환경설정, API Key 로딩
    │   ├─ .../client        # Gov24 API 호출 로직(HTTP Client)
    │   ├─ .../service       # 비즈니스 로직
    │   ├─ .../controller    # REST API 제공
    │   └─ .../dto           # 요청/응답 DTO
    └─ resources/
        └─ application.properties
