package ru.senya.sampleserv.models;

import lombok.Data;

@Data
public class ResponseModel {

    private Result result;

    @Data
    public static class Result {
        private String num_tokens;
        private Message message;

        @Data
        public static class Message {
            private String role, text;
        }
    }
}

