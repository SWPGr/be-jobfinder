package com.example.jobfinder.repository;

import com.example.jobfinder.model.Report;
import com.example.jobfinder.model.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportTypeRepository extends BaseNameRepository<ReportType, Long> {
}
