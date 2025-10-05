package com.hostelhelp.studentservice;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.sql.DataSource;
import java.sql.SQLException;

@SpringBootApplication
public class StudentServiceApplication {
    @Autowired
    private DataSource dataSource;
    public static void main(String[] args) {
        SpringApplication.run(StudentServiceApplication.class, args);
    }
    @PostConstruct
    public void checkDbUrl() throws SQLException {
        System.out.println("🔍 Actual DB URL used by app: " + dataSource.getConnection().getMetaData().getURL());
    }
    @PostConstruct
    public void printJvmZone() {
        System.out.println("🕒 JVM Default Zone: " + java.time.ZoneId.systemDefault());
    }
}
