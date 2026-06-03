package com.example.linuxlearning.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TerminalTranscriptService {

    private final Map<Long, List<String>> transcripts = new ConcurrentHashMap<>();

    public void append(Long sessionId, String line) {
        transcripts.computeIfAbsent(sessionId, key -> new ArrayList<>()).add(line);
    }

    public String summarize(Long sessionId) {
        List<String> lines = transcripts.getOrDefault(sessionId, List.of());
        return String.join("\n", lines);
    }

    public void clear(Long sessionId) {
        transcripts.remove(sessionId);
    }
}
