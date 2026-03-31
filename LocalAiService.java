import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class LocalAiService {
    private static final String OLLAMA_URL =
        System.getenv().getOrDefault("OLLAMA_URL", "http://localhost:11434/api/generate");
    private static final String OLLAMA_MODEL =
        System.getenv().getOrDefault("OLLAMA_MODEL", "llama3.2");

    private static final HttpClient CLIENT = HttpClient.newBuilder()
                                                 .connectTimeout(Duration.ofSeconds(8))
                                                 .build();

    public static String[] improveAchievementDraft(String title, String body)
        throws IOException, InterruptedException {
        String langRule = languageInstruction(safeText(title) + "\n" + safeText(body));
        String prompt = "You are an assistant that improves an achievement post. "
            + "Rewrite with clear, professional language while preserving meaning. "
            + langRule
            + "Return exactly in this format:\n"
            + "TITLE: <improved title>\n"
            + "BODY:\n<improved body>\n"
            + "Do not include any extra text.\n\n"
            + "Current title: " + safeText(title) + "\n"
            + "Current body:\n" + safeText(body);

        String response = callOllama(prompt);
        return parseTitleBody(response, title, body);
    }

    public static String[] improveNoticeDraft(String type, String title, String body)
        throws IOException, InterruptedException {
        String langRule = languageInstruction(safeText(title) + "\n" + safeText(body));
        String prompt = "You are an assistant that improves a community notice. "
            + "Type: " + safeText(type) + ". "
            + "Make it concise, professional, and easy to read. "
            + langRule
            + "Return exactly in this format:\n"
            + "TITLE: <improved title>\n"
            + "BODY:\n<improved body>\n"
            + "Do not include any extra text.\n\n"
            + "Current title: " + safeText(title) + "\n"
            + "Current body:\n" + safeText(body);

        String response = callOllama(prompt);
        return parseTitleBody(response, title, body);
    }

    public static String chatWithContext(String context, String userMessage)
        throws IOException, InterruptedException {
        String langRule = languageInstruction(safeText(userMessage));
        String prompt = "You are a helpful writing assistant. "
            + "Give clear and practical writing suggestions, and provide options when possible. "
            + langRule
            + "\n\nContext:\n" + safeText(context)
            + "\n\nUser message:\n" + safeText(userMessage)
            + "\n\nRespond directly to the user message.";
        return callOllama(prompt);
    }

    private static String callOllama(String prompt)
        throws IOException, InterruptedException {
        String payload = "{"
            + "\"model\":\"" + escapeJson(OLLAMA_MODEL) + "\","
            + "\"prompt\":\"" + escapeJson(prompt) + "\","
            + "\"stream\":false"
            + "}";

        HttpRequest request = HttpRequest.newBuilder()
                                  .uri(URI.create(OLLAMA_URL))
                                  .timeout(Duration.ofSeconds(45))
                                  .header("Content-Type", "application/json")
                                  .POST(HttpRequest.BodyPublishers.ofString(payload))
                                  .build();

        HttpResponse<String> response =
            CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Ollama HTTP " + response.statusCode());
        }

        String aiText = extractJsonStringField(response.body(), "response");
        if (aiText == null || aiText.trim().isEmpty()) {
            throw new IOException("Empty response from local AI.");
        }
        return aiText.trim();
    }

    private static String[] parseTitleBody(String text, String fallbackTitle,
        String fallbackBody) {
        String norm = text.replace("\r\n", "\n").trim();
        int titleIdx = norm.toUpperCase().indexOf("TITLE:");
        int bodyIdx = norm.toUpperCase().indexOf("BODY:");

        if (titleIdx >= 0 && bodyIdx > titleIdx) {
            String t = norm.substring(titleIdx + 6, bodyIdx).trim();
            String b = norm.substring(bodyIdx + 5).trim();
            if (!t.isEmpty() && !b.isEmpty()) {
                return new String[] {t, b};
            }
        }

        // Fallback: keep title, replace body with full response if useful.
        if (!norm.isEmpty()) {
            return new String[] {fallbackTitle, norm};
        }
        return new String[] {fallbackTitle, fallbackBody};
    }

    private static String extractJsonStringField(String json, String field) {
        String key = "\"" + field + "\"";
        int keyPos = json.indexOf(key);
        if (keyPos < 0)
            return null;

        int colonPos = json.indexOf(':', keyPos + key.length());
        if (colonPos < 0)
            return null;

        int q1 = json.indexOf('"', colonPos + 1);
        if (q1 < 0)
            return null;

        StringBuilder sb = new StringBuilder();
        boolean escape = false;
        for (int i = q1 + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escape) {
                switch (c) {
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case '"':
                        sb.append('"');
                        break;
                    case '\\':
                        sb.append('\\');
                        break;
                    default:
                        sb.append(c);
                        break;
                }
                escape = false;
                continue;
            }
            if (c == '\\') {
                escape = true;
                continue;
            }
            if (c == '"') {
                return sb.toString();
            }
            sb.append(c);
        }
        return null;
    }

    private static String escapeJson(String s) {
        if (s == null)
            return "";
        return s
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

    private static String languageInstruction(String text) {
        if (containsBangla(text)) {
            return "Write the output in Bangla language, natural and formal. ";
        }
        return "Write the output in English language. ";
    }

    private static boolean containsBangla(String text) {
        if (text == null || text.isEmpty())
            return false;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch >= '\u0980' && ch <= '\u09FF') {
                return true;
            }
        }
        return false;
    }

    private static String safeText(String s) { return s == null ? "" : s.trim(); }
}
