package whisper.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DiaryResponse {
    private Long id;
    private String date;
    private String content;
    private String attachment;
    private String emotion;
    private String emoji;
    private String message;
    private int score;
    
    
    public void setGeminiResponseData(GeminiResponseData geminiResponseData) {
        this.emotion = geminiResponseData.getEmotion();
        this.emoji = geminiResponseData.getEmoji();
        this.message = geminiResponseData.getMessage();
        this.score = geminiResponseData.getScore();
    }
}




