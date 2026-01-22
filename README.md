# sw_project_gov24api

공공데이터(Open API) 중 **Gov24(정부24) 관련 API**를 활용해 데이터를 조회/가공하고, 애플리케이션(또는 API 서버) 형태로 제공하는 프로젝트입니다.

> ✅ 목적: 공공데이터 API 연동 경험, 인증키 관리, 요청/응답 파싱, 예외처리, (선택) 서버 API화 및 배포 흐름 학습

---

## ✨ 주요 기능 (예시)
- Gov24(정부24) Open API 호출 (인증키 기반)
- 요청 파라미터(검색어/페이지/정렬 등) 구성 및 전송
- 응답(XML/JSON) 파싱 및 DTO 매핑
- 결과 리스트/상세 조회 기능
- 예외 처리(타임아웃/인증 실패/형식 오류) 및 로깅
- (선택) REST API로 래핑하여 프론트/외부에서 호출 가능

> 실제 구현 기능에 맞게 위 목록은 편집해서 사용하세요.

---

## 🧱 기술 스택 (필요한 것만 남기기)
- Language: **Java**
- Build: **Gradle**
- (선택) Framework: Spring Boot / Servlet / Console App
- (선택) HTTP Client: RestTemplate / WebClient / OkHttp / HttpURLConnection
- (선택) Parsing: Jackson(JSON) / JAXB, DOM, SAX(XML)
- (선택) Logging: SLF4J + Logback

---

## 📁 프로젝트 구조 (예시)
> 레포 구조에 맞게 디렉토리/패키지명을 조정하세요.

```bash
src/
 └─ main/
    ├─ java/
    │   ├─ .../config        # 환경설정, API Key 로딩
    │   ├─ .../client        # Gov24 API 호출 로직(HTTP Client)
    │   ├─ .../service       # 비즈니스 로직
    │   ├─ .../controller    # (선택) REST API 제공
    │   └─ .../dto           # 요청/응답 DTO
    └─ resources/
        └─ application.yml   # (선택) 설정 파일
