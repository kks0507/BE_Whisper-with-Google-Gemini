package whisper.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import whisper.dto.AddDiaryRequest;
import whisper.dto.DiaryResponse;
import whisper.dto.GeminiResponseData;
import whisper.dto.UpdateDiaryRequest;
import whisper.dto.GeminiRequest;
import whisper.entity.Diary;
import whisper.entity.UserEntity;
import whisper.repository.DiaryRepository;
import whisper.repository.UserRepository;
import whisper.util.FileUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiaryService {

    private static final String UPLOADED_FOLDER = "c:\\DiaryFileUpload\\"; // 실제 파일 저장 경로 설정

    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;
    private final FileUtils fileUtils;
    

    public DiaryResponse getDiaryById(Long id, String email) { // getDiaryById 메서드 수정
        log.info("Fetching diary with id: {} for user: {}", id, email);
        Long userId = userRepository.findByEmail(email).getId();
        Diary diary = diaryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Diary not found"));
        return new DiaryResponse(diary.getId(), diary.getDate().toString(), diary.getContent(),
                diary.getAttachment(), diary.getEmotion(), diary.getEmoji(), diary.getMessage(), diary.getScore());
    }

    @Transactional
    public void deleteDiary(Long id, String email) {
        log.info("Deleting diary with id: {} for user: {}", id, email);
        Long userId = userRepository.findByEmail(email).getId();
        Diary diary = diaryRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new RuntimeException("Diary not found"));

        diaryRepository.delete(diary);
    }

    public List<DiaryResponse> getAllDiaries(String email) {
        log.info("Fetching all diaries for user: {}", email);
        Long userId = userRepository.findByEmail(email).getId();
        return diaryRepository.findByUserId(userId).stream()
                .map(diary -> new DiaryResponse(diary.getId(), diary.getDate().toString(), diary.getContent(),
                        diary.getAttachment(), diary.getEmotion(), diary.getEmoji(), diary.getMessage(), diary.getScore()))
                .collect(Collectors.toList());
    }

  
    @Transactional
    public DiaryResponse createDiary(AddDiaryRequest addDiaryRequest, MultipartFile file, String email) throws IOException {
        log.info("Creating new diary for user: {}", email);
        Long userId = userRepository.findByEmail(email).getId();
        Diary diary = new Diary();
        diary.setContent(addDiaryRequest.getContent());
        diary.setDate(addDiaryRequest.getDate());

        String diaryContent = addDiaryRequest.getContent();
        if (file != null && !file.isEmpty()) {
            try {
                diary.setAttachment(fileUtils.saveFile(file)); // 파일 저장 경로 지정
                diaryContent = analyzeDiaryWithAttachment(diaryContent, file);
            } catch (IOException e) {
                throw new IOException("Failed to save file", e); // IOException으로 변경
            }
        }

        GeminiResponseData responseData = geminiService.analyzeDiary(diaryContent);
        diary.setEmotion(responseData.getEmotion());
        diary.setEmoji(responseData.getEmoji());
        diary.setMessage(responseData.getMessage());
        diary.setScore(responseData.getScore());

        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        diary.setUser(user);
        diaryRepository.save(diary);

        return new DiaryResponse(diary.getId(), diary.getDate().toString(), diary.getContent(), diary.getAttachment(),
                diary.getEmotion(), diary.getEmoji(), diary.getMessage(), diary.getScore());
    }

    private String analyzeDiaryWithAttachment(String diaryContent, MultipartFile attachment) throws IOException {
        String attachmentContent = new String(attachment.getBytes());
        return diaryContent + "\n\n**Attachment:**\n" + attachmentContent;
    }
    
    
    @Transactional
    public DiaryResponse updateDiary(Long id, UpdateDiaryRequest updateDiaryRequest, MultipartFile file, String email) throws IOException {
        log.info("Updating diary with id: {} for user: {}", id, email);
        Long userId = userRepository.findByEmail(email).getId();
        Diary diary = diaryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Diary not found"));

        if (updateDiaryRequest.isRemoveAttachment()) {
            fileUtils.deleteFile(diary.getAttachment());
            diary.setAttachment(null);
        } else if (file != null && !file.isEmpty()) {
            fileUtils.deleteFile(diary.getAttachment());
            diary.setAttachment(fileUtils.saveFile(file));
        }

        diary.setContent(updateDiaryRequest.getContent());
        String updatedContent = diary.getContent();
        if (diary.getAttachment() != null) {
            updatedContent = analyzeDiaryWithAttachment(updatedContent, file);
        }
        GeminiResponseData responseData = geminiService.analyzeDiary(updatedContent);
        diary.setEmotion(responseData.getEmotion());
        diary.setEmoji(responseData.getEmoji());
        diary.setMessage(responseData.getMessage());
        diary.setScore(responseData.getScore());

        diaryRepository.save(diary);

        return new DiaryResponse(diary.getId(), diary.getDate().toString(), diary.getContent(), diary.getAttachment(),
                diary.getEmotion(), diary.getEmoji(), diary.getMessage(), diary.getScore());
    }
    private String analyzeDiaryWithAttachment1(String diaryContent, MultipartFile attachment) throws IOException {
        String attachmentContent = new String(attachment.getBytes());
        return diaryContent + "\n\n**Attachment:**\n" + attachmentContent;
    }

}













