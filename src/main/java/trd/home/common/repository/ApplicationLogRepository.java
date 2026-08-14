package trd.home.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import trd.home.common.dao.ApplicationLog;

public interface ApplicationLogRepository extends JpaRepository<ApplicationLog, String> {}
