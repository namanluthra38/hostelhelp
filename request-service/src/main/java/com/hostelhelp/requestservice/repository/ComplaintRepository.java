package com.hostelhelp.requestservice.repository;

import com.hostelhelp.requestservice.model.Complaint;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplaintRepository extends MongoRepository<Complaint, String> {
    Complaint findByHostelId(String hostelId);
    Complaint findByStudentId(String studentId);
}
