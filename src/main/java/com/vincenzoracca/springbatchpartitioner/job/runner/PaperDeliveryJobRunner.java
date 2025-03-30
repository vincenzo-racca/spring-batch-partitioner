package com.vincenzoracca.springbatchpartitioner.job.runner;

import com.vincenzoracca.springbatchpartitioner.middleware.mock.ExternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class PaperDeliveryJobRunner {

    private final Job job;

    private final JobLauncher jobLauncher;

    @Scheduled(cron = "${paper-delivery-cron}")
    public void run() throws Exception {
        var pks = ExternalService.buildPks();
        JobParameters jobParameters = new JobParametersBuilder()
                .addJobParameter("pks", pks, String.class)
                .addJobParameter("createAt", Instant.now().toString(), String.class)
                .toJobParameters();

        jobLauncher.run(job, jobParameters);
    }

}
