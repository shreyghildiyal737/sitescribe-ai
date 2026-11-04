package com.sitescribe.ai;

import com.sitescribe.upload.Upload;

public interface TranscriptionService {
    TranscriptResult transcribe(Upload upload, String manualTranscript);
}
