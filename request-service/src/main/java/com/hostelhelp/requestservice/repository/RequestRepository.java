package com.hostelhelp.requestservice.repository;

import com.hostelhelp.requestservice.model.Request;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RequestRepository extends MongoRepository<Request, String> {

    List<Request> findByStudentId(String studentId);

    List<Request> findByStatus(Request.Status status);
}
