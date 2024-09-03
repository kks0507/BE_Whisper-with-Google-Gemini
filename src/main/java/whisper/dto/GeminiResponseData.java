package whisper.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GeminiResponseData {
    private String emotion;
    private String emoji;
    private String message;
    private int score;
}







