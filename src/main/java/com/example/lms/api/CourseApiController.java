package com.example.lms.api;

import com.example.lms.api.dto.ApiResponse;
import com.example.lms.api.dto.CourseResponse;
import com.example.lms.api.dto.PageResponse;
import com.example.lms.domain.course.Course;
import com.example.lms.service.course.CourseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseApiController {

    private final CourseService courseService;

    public CourseApiController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ApiResponse<PageResponse<CourseResponse>> list(@RequestParam(required = false) String keyword,
                                                           @RequestParam(required = false) String category,
                                                           @RequestParam(defaultValue = "1") int page,
                                                           @RequestParam(defaultValue = "10") int size) {
        CourseService.CoursePage coursePage = courseService.searchCourses(keyword, category, page, size);
        PageResponse<CourseResponse> response = new PageResponse<>(
                coursePage.items().stream().map(this::toResponse).toList(),
                coursePage.page(),
                coursePage.size(),
                coursePage.totalCount(),
                coursePage.totalPages()
        );
        return ApiResponse.ok(response);
    }

    private CourseResponse toResponse(Course c) {
        return new CourseResponse(c.getId(), c.getTitle(), c.getCategory(), c.getInstructorName(), c.getCapacity());
    }
}
