package com.github.kzhunmax.jobsearch.service;

import com.github.kzhunmax.jobsearch.dto.request.JobRequestDTO;
import com.github.kzhunmax.jobsearch.dto.response.JobResponseDTO;
import com.github.kzhunmax.jobsearch.exception.JobNotFoundException;
import com.github.kzhunmax.jobsearch.model.Job;
import com.github.kzhunmax.jobsearch.model.User;
import com.github.kzhunmax.jobsearch.repository.JobRepository;
import com.github.kzhunmax.jobsearch.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobService {
    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public JobResponseDTO createJob(JobRequestDTO dto, String username) {
        User user = findUserByUsername(username);
        Job job = buildJobFromDTO(dto, user);
        Job savedJob = jobRepository.save(job);
        return toJobResponseDTO(savedJob);
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }


    private Job buildJobFromDTO(JobRequestDTO dto, User user) {
        return Job.builder()
                .title(dto.title())
                .description(dto.description())
                .company(dto.company())
                .location(dto.location())
                .salary(dto.salary())
                .active(true)
                .postedBy(user)
                .build();
    }

    private JobResponseDTO toJobResponseDTO(Job job) {
        return new JobResponseDTO(
                job.getId(),
                job.getTitle(),
                job.getDescription(),
                job.getCompany(),
                job.getLocation(),
                job.getSalary(),
                job.isActive(),
                job.getPostedBy().getUsername()
        );
    }

    @Transactional
    public JobResponseDTO getJobById(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException(jobId));
        return toJobResponseDTO(job);
    }

    @Transactional
    public JobResponseDTO updateJob(Long jobId, JobRequestDTO dto) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException(jobId));

        job.setTitle(dto.title());
        job.setDescription(dto.description());
        job.setCompany(dto.company());
        job.setLocation(dto.location());
        job.setSalary(dto.salary());

        Job updatedJob = jobRepository.save(job);
        return toJobResponseDTO(updatedJob);
    }

    @Transactional
    public void deleteJob(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException(jobId));
        job.setActive(false);
        jobRepository.save(job);
    }

    @Transactional
    public Page<JobResponseDTO> getAllActiveJobs(Pageable pageable) {
        return jobRepository.findByActiveTrue(pageable)
                .map(this::toJobResponseDTO);
    }

    @Transactional
    public Page<JobResponseDTO> getJobsByRecruiter(String username, Pageable pageable) {
        User recruiter = findUserByUsername(username);
        return jobRepository.findByPostedBy(recruiter, pageable)
                .map(this::toJobResponseDTO);
    }
}
