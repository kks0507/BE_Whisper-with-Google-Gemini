package whisper.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import whisper.dto.GeminiRequest;
import whisper.dto.GeminiResponse;
import whisper.dto.GeminiResponseData;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private static final String FULL_PROMPT = """
    	    You are an AI that analyzes diary entries and generates a structured response including emotion, emoji, message, and a score. Follow the specified format strictly.

    	    ### Response Format ###
    	    {
    	        "Emotion": "Main emotion as a single word",
    	        "Emoji": "A single emoji that best represents the emotion",
    	        "Message": "A short poem or lyric based on analysis, including emotion and emoji",
    	        "Score": A score between 1 and 10 for the day
    	    }

    	    Steps to follow:
    	    1. Analyze the emotions expressed in the diary entries and identify the dominant emotion.
    	    2. Select an emoji that accurately represents this emotion.
    	    3. Rate the day on a scale of 1 to 10 based on sentiment analysis and overall tone.
    	       - **1-3:** Negative emotions
    	       - **4-6:** Neutral to somewhat positive
    	       - **7-10:** Positive emotions

    	    4. Generate a response according to the score:
    	       - **Option 1:** If the score is 7 or higher, create a short, bold, and witty poem (up to 200 characters) that captures the essence of the day, including the emoji.
    	         - Example: "Today was joyful! 😊 Dancing in sunshine with a heart full of light."

    	       - **Option 2:** If the score is between 4 and 6, generate a metaphorical phrase that reflects minor conflicts or daily challenges, incorporating the emoji.
    	         - Example: "A lion roared at my castle. 🦁😞"

    	       - **Option 3:** If the score is 3 or lower, offer empathy and comfort through a short lyric that provides reassurance and hope, including the emoji.
    	         - Example: "You're not alone, I'm here. 🌟 'Cause every little thing gonna be all right."

    		
    	    ### Analysis Guidelines ###
    	    - **1. Extremely Negative (1):** Severe depression, unable to perform daily life, suicidal thoughts.
    	    - **2. Very Negative (2):** Very severe depression, sad most of the time, impaired functioning.
    	    - **3. Negative (3):** Severe depression, feeling depressed most of the day, poor concentration.
    	    - **4. Somewhat Negative (4):** Moderate depression, frequent depression, decreased interest.
    	    - **5. Neutral (5):** Mild depression, occasional depression, decreased interest.
    	    - **6. Somewhat Positive (6):** Mild depression, occasional low mood.
    	    - **7. Positive (7):** Almost never, occasionally depressed but quickly recovered.
    	    - **8. Very Positive (8):** Rarely feeling depressed, rarely feeling tired.
    	    - **9. Extremely Positive (9):** Almost always feeling good, very rarely feeling depressed.
    	    - **10. Highest Positive State (10):** Not depressed at all, always in a good mood and positive.

    	    Always adhere to the response format and ensure safety ratings are included with each output.
    	    """;

    private final RestTemplate restTemplate;

    @Autowired
    public GeminiService(@Qualifier("geminiRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public GeminiResponseData analyzeDiary(String diaryText) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        // 요청 URL 생성
        String requestUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=" + geminiApiKey;

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 요청 본문 생성
        GeminiRequest requestPayload = new GeminiRequest(FULL_PROMPT + diaryText);

        // 요청 전송
        HttpEntity<GeminiRequest> request = new HttpEntity<>(requestPayload, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(requestUrl, request, String.class);

        // 응답 데이터 파싱
        GeminiResponse geminiResponse = objectMapper.readValue(response.getBody(), GeminiResponse.class);

        // 결과 추출 및 반환
        GeminiResponse.Part output = geminiResponse.getCandidates().get(0).getContent().getParts().get(0);
        String emotion = output.getText();
        String emoji = output.getText();
        String message = output.getText();
        int score = output.getScore();
       
        // 메시지 길이 제한 (예: 255자)
        if (message.length() > 240) {
            message = message.substring(0, 240);
        }

        return new GeminiResponseData(emotion, emoji, message, score);
    }
}





        
        
        
    










