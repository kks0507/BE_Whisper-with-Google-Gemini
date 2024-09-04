# Whisper (나에게만 말해줘. 너의 속마음을) 🤫

## 프로젝트 설명

**Whisper**는 사용자의 일기를 분석하여 감정 상태를 파악하고, 이를 바탕으로 위로와 격려를 제공하는 일기 앱입니다. 

* 텍스트 뿐만 아니라 텍스트 파일도 첨부 가능하여, 사용자가 자신의 감정과 경험을 더욱 풍부하게 표현할 수 있습니다. 
* 제미나이(Gemini) AI를 활용하여 일기 내용을 분석하고, 감정에 맞는 이모티콘, 짧은 시, 그리고 하루 점수를 생성합니다. 
* 분석 결과는 자동 저장되어, 메인 페이지(캘린더 형식)에서 상세조회 페이지로 접근해서 확인할 수 있습니다.

<br>

## 설치 및 실행 방법

### 1. 프로젝트 클론

```bash
git clone https://github.com/kks0507/Whisper_BE-with-Google-Gemini-.git
```

### 2. MySQL 데이터베이스 설정

* 로컬 환경에 MySQL 서버를 설치하고 실행합니다.
* `application.properties` 파일에서 데이터베이스 연결 정보를 설정합니다.


### 3. API 엔드포인트

다음 API 엔드포인트를 통해 Whisper 기능을 사용할 수 있습니다.

* `/api/auth/register`: 사용자 등록
* `/api/auth/login`: 사용자 로그인
* `/api/diaries`: 새로운 일기 작성
* `/api/diaries/{id}`: 특정 일기 조회, 수정, 삭제

<br>

## 주요 기능 및 코드 설명

### 1. 일기 분석 및 감정 분석 (`DiaryService`)

* `createDiary`: 새로운 일기를 생성하고, 텍스트 및 첨부파일을 분석하여 감정 분석 결과를 생성합니다.
* `analyzeDiaryWithAttachment`: 첨부파일이 있는 경우, 첨부파일 내용을 텍스트와 함께 분석합니다.
* `updateDiary`: 기존 일기를 수정하고, 변경된 내용을 바탕으로 감정 분석 결과를 업데이트합니다.
* `getDiaryById`, `getAllDiaries`, `deleteDiary`: 일기 조회, 전체 일기 목록 조회, 일기 삭제 기능을 제공합니다.

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class DiaryService {

    // ...

    @Transactional
    public DiaryResponse createDiary(AddDiaryRequest addDiaryRequest, MultipartFile file, String email) throws IOException {
        // ...
        String diaryContent = addDiaryRequest.getContent();
        if (file != null && !file.isEmpty()) {
            try {
                diary.setAttachment(fileUtils.saveFile(file)); 
                diaryContent = analyzeDiaryWithAttachment(diaryContent, file);
            } catch (IOException e) {
                throw new IOException("Failed to save file", e); 
            }
        }

        GeminiResponseData responseData = geminiService.analyzeDiary(diaryContent);
        // ...
    }

    // ...
}
```

### 2. 사용자 인증 및 권한 관리 (`SecurityConfig`)

* Spring Security를 사용하여 사용자 인증 및 권한 관리 기능을 구현합니다.
* JWT 토큰을 사용하여 사용자 세션을 관리합니다.
* `JwtRequestFilter`를 통해 JWT 토큰을 검증하고 인증 정보를 설정합니다.

```java
@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfiguration {

    // ...

    @SuppressWarnings("removal")
	@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // ...
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // ...
}
```

### 3. 제미나이(Gemini) API 연동 (`GeminiService`)

* `analyzeDiary`: 제미나이 API를 호출하여 일기 내용을 분석하고 감정, 이모티콘, 메시지, 점수를 생성합니다.
* `FULL_PROMPT`: 제미나이 API에게 제공할 프롬프트를 정의합니다.

```java
@Service
public class GeminiService {

    // ...

    public GeminiResponseData analyzeDiary(String diaryText) throws JsonProcessingException {
        // ...
        GeminiRequest requestPayload = new GeminiRequest(FULL_PROMPT + diaryText);
        // ...
    }
}
```

## 프로젝트 참여자

* [kks0507](https://github.com/kks0507)
