package com.sitescribe.upload;

import com.sitescribe.project.ProjectRepository;
import com.sitescribe.storage.StorageService;
import com.sitescribe.user.UserRepository;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

class UploadServiceTest {

    private final StorageService storage = mock(StorageService.class);
    private final UploadService service = new UploadService(
        mock(UploadRepository.class), mock(ProjectRepository.class), mock(UserRepository.class), storage);

    @Test
    void forgetsThePathOnceTheAudioIsGone() throws IOException {
        Upload upload = uploadAt("uploads/a.webm");

        service.deleteAudioFile(upload);

        verify(storage).delete("uploads/a.webm");
        verify(upload).clearFilePath();
    }

    @Test
    void keepsThePathWhenDeletionFailsSoTheFileIsNotOrphaned() throws IOException {
        Upload upload = uploadAt("uploads/a.webm");
        doThrow(new IOException("disk said no")).when(storage).delete("uploads/a.webm");

        // The report is already saved by this point, so a failed delete must not fail the request.
        assertThatCode(() -> service.deleteAudioFile(upload)).doesNotThrowAnyException();
        verify(upload, never()).clearFilePath();
    }

    private Upload uploadAt(String path) {
        Upload upload = mock(Upload.class);
        when(upload.getFilePath()).thenReturn(path);
        return upload;
    }
}
