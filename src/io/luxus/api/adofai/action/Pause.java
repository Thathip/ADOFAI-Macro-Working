package io.luxus.api.adofai.action;

import org.json.simple.JSONObject;

import io.luxus.api.adofai.converter.SafeDatatypeConverter;
import io.luxus.api.adofai.type.EventType;

public class Pause extends Action {

    private Double duration;          // 拍数（例: 7）
    private Long countdownTicks;      // カウントダウン表示（任意）
    private String angleCorrectionDir;// "Forward"/"Backward" 等（今回は動作に未使用）
    public Pause() {
        super(EventType.PAUSE);
    }

    @Override
    public void load(JSONObject json) {
        // eventType は親で扱う想定、ここではフィールドのみ
        Object d = json.get("duration");
        this.duration = SafeDatatypeConverter.toDouble(d == null ? 0 : d);
        Object ct = json.get("countdownTicks");
        this.countdownTicks = (ct instanceof Number) ? ((Number) ct).longValue() : null;
        Object acd = json.get("angleCorrectionDir");
        this.angleCorrectionDir = (acd != null) ? acd.toString() : null;
    }

    @Override
    public void save(StringBuilder sb, int floor) {
        sb.append("\t\t{\n");
        sb.append("\t\t\t\"floor\": ").append(floor).append(",\n");
        sb.append("\t\t\t\"eventType\": \"").append(EventType.PAUSE.getName()).append("\",\n");
        if (duration != null)       sb.append("\t\t\t\"duration\": ").append(formatDouble(duration)).append(",\n");
        if (countdownTicks != null) sb.append("\t\t\t\"countdownTicks\": ").append(countdownTicks).append(",\n");
        if (angleCorrectionDir != null) sb.append("\t\t\t\"angleCorrectionDir\": \"").append(angleCorrectionDir).append("\",\n");
        // 末尾の余分なカンマを消して閉じる（簡易）
        int lastComma = sb.lastIndexOf(",\n");
        if (lastComma > 0 && sb.substring(lastComma - 2).startsWith("\n")) {
            // do nothing
        }
        if (sb.charAt(sb.length()-2) == ',') sb.deleteCharAt(sb.length()-2);
        sb.append("\t\t},\n");
    }

    private String formatDouble(Double v) {
        double dv = v.doubleValue();
        long lv = (long) dv;
        return (dv == lv) ? Long.toString(lv) : String.format(java.util.Locale.US, "%.6f", dv);
    }

    // getters
    public Double getDuration() { return duration; }
    public Long getCountdownTicks() { return countdownTicks; }
    public String getAngleCorrectionDir() { return angleCorrectionDir; }
}
