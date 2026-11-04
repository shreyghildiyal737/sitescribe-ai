package com.sitescribe.upload;
import org.springframework.data.jpa.repository.JpaRepository;import java.util.*;
public interface UploadRepository extends JpaRepository<Upload,UUID>{List<Upload> findByProjectId(UUID projectId);}
