package com.dietrecord.backend.modules.photo.service;

import com.dietrecord.backend.modules.photo.model.internal.PhotoAiRecognitionCandidate;
import com.dietrecord.backend.modules.photo.model.internal.ProcessedPhoto;

import java.util.List;

public interface PhotoAiRecognitionClient {

    List<PhotoAiRecognitionCandidate> recognize(ProcessedPhoto processedPhoto);
}
