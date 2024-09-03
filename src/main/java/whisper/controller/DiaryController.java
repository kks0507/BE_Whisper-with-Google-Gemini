package whisper.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import whisper.dto.AddDiaryRequest;
import whisper.dto.DiaryResponse;
import whisper.dto.UpdateDiaryRequest;
import whisper.dto.GeminiResponseData;
import whisper.service.DiaryService;
import whisper.service.GeminiService;
import whisper.util.ApiResponse;
import whisper.util.FileUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/diaries")
@Slf4j
public class DiaryController {

    private static final String UPLOADED_FOLDER = "c:\\DiaryFileUpload\\"; // 파일 업로드 폴더 경로

    @Autowired
    private DiaryService diaryService;

    @Autowired
    private FileUtils fileUtils;

    @Autowired
    private GeminiService geminiService;

    @Operation(summary = "새 일기 작성", description = "새로운 일기를 작성합니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DiaryResponse>> createDiary(
        @RequestPart(value = "diary") String diaryJson,
        @RequestPart(value = "file", required = false) MultipartFile file,
        Authentication authentication) throws IOException {

        // JSON 데이터를 객체로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        AddDiaryRequest addDiaryRequest = objectMapper.readValue(diaryJson, AddDiaryRequest.class);

        // 파일 저장 로직 (구현 필요)
        String fileName = fileUtils.saveFile(file);

        // 일기 생성 및 Gemini API 분석
        DiaryResponse response = diaryService.createDiary(addDiaryRequest, file, authentication.getName());
        GeminiResponseData geminiResponseData = geminiService.analyzeDiary(addDiaryRequest.getContent());
        response.setGeminiResponseData(geminiResponseData);

        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Diary created successfully", response));
    }

    @Operation(summary = "일기 전체 조회", description = "현재 사용자에 대한 모든 일기를 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<DiaryResponse>>> getAllDiaries(Authentication authentication) {
        log.info("Fetching all diaries for user: {}", authentication.getName());
        List<DiaryResponse> diaries = diaryService.getAllDiaries(authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "Diaries fetched successfully", diaries));
    }

    @Operation(summary = "일기 상세 조회", description = "ID로 특정 일기를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DiaryResponse>> getDiaryById(@PathVariable("id") Long id, Authentication authentication) {
        log.info("Fetching diary with id: {} for user: {}", id, authentication.getName());
        try {
            DiaryResponse diary = diaryService.getDiaryById(id, authentication.getName());
            return ResponseEntity.ok(new ApiResponse<DiaryResponse>(true, "Diary fetched successfully", diary));
        } catch (RuntimeException e) {
            log.error("Failed to fetch diary: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<DiaryResponse>(false, e.getMessage(), null));
        }
    }

    @Operation(summary = "일기 수정", description = "기존 일기를 수정합니다.")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DiaryResponse>> updateDiary(
        @PathVariable("id") Long id,
        @RequestPart(value = "diary") String diaryJson,
        @RequestPart(value = "file", required = false) MultipartFile file,
        Authentication authentication) throws IOException {

        // JSON 데이터를 객체로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        UpdateDiaryRequest updateDiaryRequest = objectMapper.readValue(diaryJson, UpdateDiaryRequest.class);

        // 파일 저장 로직 (구현 필요)
        String fileName = null;
        if (file != null && !file.isEmpty()) {
            fileName = fileUtils.saveFile(file);
        }

        // 일기 수정 및 Gemini API 분석
        DiaryResponse response = diaryService.updateDiary(id, updateDiaryRequest, file, authentication.getName());
        GeminiResponseData geminiResponseData = geminiService.analyzeDiary(updateDiaryRequest.getContent());
        response.setGeminiResponseData(geminiResponseData);

        return ResponseEntity.ok(new ApiResponse<>(true, "Diary updated successfully", response));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDiary(@PathVariable("id") Long id, Authentication authentication) throws IOException {
        log.info("Deleting diary with id: {} for user: {}", id, authentication.getName());
        diaryService.deleteDiary(id, authentication.getName()); // authentication.getName() 전달
        return ResponseEntity.noContent().build();
    }

}