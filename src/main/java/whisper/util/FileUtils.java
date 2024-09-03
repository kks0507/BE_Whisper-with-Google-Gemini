package whisper.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class FileUtils {

    private static final String UPLOADED_FOLDER = "C:\\DiaryFileUpload"; // 파일 업로드 경로

    public String saveFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // 디렉토리 생성 확인
        Path directoryPath = Paths.get(UPLOADED_FOLDER);
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }

        // 파일 저장
        Path filePath = directoryPath.resolve(file.getOriginalFilename());
        Files.write(filePath, file.getBytes());

        return file.getOriginalFilename();
    }

    public void deleteFile(String filename) throws IOException {
        if (filename == null) {
            return;
        }

        Path filePath = Paths.get(UPLOADED_FOLDER, filename);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
    }
}


