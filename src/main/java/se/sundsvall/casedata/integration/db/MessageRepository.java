package se.sundsvall.casedata.integration.db;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import se.sundsvall.casedata.integration.db.model.Message;

public interface MessageRepository extends JpaRepository<Message, String> {

	List<Message> findAllByErrandNumber(String errandNumber);

}
