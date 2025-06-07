package http.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GsonUtils { // Создала отдельным классом, чтобы не дублировать во всех Handler'ах

    private GsonUtils() {}

    // Метод для получения настроенного Gson
    public static Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
        gsonBuilder.registerTypeAdapter(Duration.class, new DurationAdapter());
        gsonBuilder.setPrettyPrinting();
        return gsonBuilder.create();
    }

    public static class LocalDateTimeAdapter extends com.google.gson.TypeAdapter<LocalDateTime> {
        private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.format(formatter));
            }
        }

        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return LocalDateTime.parse(in.nextString(), formatter);
        }
    }

    public static class DurationAdapter extends com.google.gson.TypeAdapter<Duration> {
        @Override
        public void write(JsonWriter out, Duration value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.toMinutes());
            }
        }

        @Override
        public Duration read(JsonReader in) throws IOException {
            if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            com.google.gson.stream.JsonToken peek = in.peek();
            if (peek == com.google.gson.stream.JsonToken.NUMBER) {
                return Duration.ofMinutes(in.nextLong());
            } else if (peek == com.google.gson.stream.JsonToken.STRING) {
                try {
                    return Duration.ofMinutes(Long.parseLong(in.nextString()));
                } catch (NumberFormatException e) {
                    throw new IOException("Не удается выполнить parse Duration из string: " + e.getMessage());
                }
            } else {
                throw new IOException("Expected NUMBER or STRING for Duration, got " + peek);
            }
        }
    }
}
