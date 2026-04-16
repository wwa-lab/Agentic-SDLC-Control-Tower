package com.sdlctower.domain.requirement;

import com.sdlctower.domain.requirement.dto.AiAnalysisDto;
import com.sdlctower.domain.requirement.dto.CreateRequirementRequestDto;
import com.sdlctower.domain.requirement.dto.GenerateSpecRequestDto;
import com.sdlctower.domain.requirement.dto.GenerationResultDto;
import com.sdlctower.domain.requirement.dto.InvokeSkillRequestDto;
import com.sdlctower.domain.requirement.dto.RawRequirementInputDto;
import com.sdlctower.domain.requirement.dto.RequirementDetailDto;
import com.sdlctower.domain.requirement.dto.RequirementDraftDto;
import com.sdlctower.domain.requirement.dto.RequirementImportStatusDto;
import com.sdlctower.domain.requirement.dto.RequirementListDto;
import com.sdlctower.domain.requirement.dto.RequirementListItemDto;
import com.sdlctower.domain.requirement.dto.RequirementNormalizeRequestDto;
import com.sdlctower.domain.requirement.dto.SdlcChainDto;
import com.sdlctower.domain.requirement.dto.SkillExecutionResultDto;
import com.sdlctower.shared.ApiConstants;
import com.sdlctower.shared.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
public class RequirementController {

    private final RequirementService requirementService;
    private final RequirementImportService requirementImportService;

    public RequirementController(RequirementService requirementService, RequirementImportService requirementImportService) {
        this.requirementService = requirementService;
        this.requirementImportService = requirementImportService;
    }

    @GetMapping(ApiConstants.REQUIREMENTS)
    public ApiResponse<RequirementListDto> listRequirements(
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection
    ) {
        return ApiResponse.ok(requirementService.getRequirementList(
                priority,
                status,
                category,
                search,
                sortBy,
                sortDirection));
    }

    @GetMapping(ApiConstants.REQUIREMENT_DETAIL)
    public ApiResponse<RequirementDetailDto> getRequirementDetail(@PathVariable String requirementId) {
        return ApiResponse.ok(requirementService.getRequirementDetail(requirementId));
    }

    @GetMapping(ApiConstants.REQUIREMENT_CHAIN)
    public ApiResponse<SdlcChainDto> getRequirementChain(@PathVariable String requirementId) {
        return ApiResponse.ok(requirementService.getRequirementChain(requirementId));
    }

    @GetMapping(ApiConstants.REQUIREMENT_ANALYSIS)
    public ApiResponse<AiAnalysisDto> getRequirementAnalysis(@PathVariable String requirementId) {
        return ApiResponse.ok(requirementService.getRequirementAnalysis(requirementId));
    }

    @PostMapping(ApiConstants.REQUIREMENT_GENERATE_STORIES)
    public ResponseEntity<ApiResponse<GenerationResultDto>> generateStories(@PathVariable String requirementId) {
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(ApiResponse.ok(requirementService.generateStories(requirementId)));
    }

    @PostMapping(ApiConstants.REQUIREMENT_GENERATE_SPEC)
    public ResponseEntity<ApiResponse<GenerationResultDto>> generateSpec(
            @PathVariable String requirementId,
            @RequestBody(required = false) GenerateSpecRequestDto body
    ) {
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(ApiResponse.ok(requirementService.generateSpec(requirementId, body)));
    }

    @PostMapping(ApiConstants.REQUIREMENT_STORY_GENERATE_SPEC)
    public ResponseEntity<ApiResponse<GenerationResultDto>> generateSpecFromStory(@PathVariable String storyId) {
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(ApiResponse.ok(requirementService.generateSpec(storyId)));
    }

    @PostMapping(ApiConstants.REQUIREMENT_ANALYZE)
    public ResponseEntity<ApiResponse<GenerationResultDto>> analyzeRequirement(@PathVariable String requirementId) {
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(ApiResponse.ok(requirementService.runAnalysis(requirementId)));
    }

    @PostMapping(ApiConstants.REQUIREMENT_INVOKE_SKILL)
    public ResponseEntity<ApiResponse<SkillExecutionResultDto>> invokeSkill(
            @PathVariable String requirementId,
            @RequestBody InvokeSkillRequestDto body
    ) {
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(ApiResponse.ok(requirementService.invokeSkill(requirementId, body)));
    }

    @PostMapping(value = ApiConstants.REQUIREMENT_NORMALIZE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<RequirementDraftDto>> normalizeRequirement(
            @RequestBody RequirementNormalizeRequestDto body
    ) {
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(ApiResponse.ok(requirementService.normalizeRequirement(body)));
    }

    @PostMapping(value = ApiConstants.REQUIREMENT_NORMALIZE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<RequirementDraftDto>> normalizeUploadedRequirement(
            @RequestPart("file") List<MultipartFile> files,
            @RequestParam(value = "kb_name", required = false) String kbName,
            @RequestParam(required = false) String profileId
    ) {
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(ApiResponse.ok(requirementService.normalizeUploadedRequirement(kbName, profileId, files)));
    }

    @PostMapping(value = ApiConstants.REQUIREMENT_IMPORTS, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<RequirementImportStatusDto>> startImport(
            @RequestPart("file") List<MultipartFile> files,
            @RequestParam(value = "kb_name", required = false) String kbName,
            @RequestParam(required = false) String profileId
    ) {
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(ApiResponse.ok(requirementImportService.startImport(kbName, profileId, files)));
    }

    @GetMapping(ApiConstants.REQUIREMENT_IMPORT_DETAIL)
    public ApiResponse<RequirementImportStatusDto> getImportStatus(@PathVariable String importId) {
        return ApiResponse.ok(requirementImportService.getImportStatus(importId));
    }

    @PostMapping(ApiConstants.REQUIREMENTS)
    public ResponseEntity<ApiResponse<RequirementListItemDto>> createRequirement(
            @RequestBody CreateRequirementRequestDto body
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(requirementService.createRequirement(body)));
    }
}
