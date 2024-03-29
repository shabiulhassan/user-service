package dev.deepak.userservicetestfinal.repositories;

import dev.deepak.userservicetestfinal.models.Session;
import dev.deepak.userservicetestfinal.models.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findByTokenAndUser_Id(String token, Long userId);
    Optional<List<Session>> findByUser_IdAndSessionStatus(Long userId,SessionStatus sessionStatus);
}
