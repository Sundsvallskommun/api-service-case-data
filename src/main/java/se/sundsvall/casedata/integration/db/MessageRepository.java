package se.sundsvall.casedata.integration.db;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import se.sundsvall.casedata.integration.db.model.Message;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;


@CircuitBreaker(name = "messageRepository")
public interface MessageRepository extends JpaRepository<Message, String> {

	List<Message> findAllByErrandNumber(String errandNumber);

}
