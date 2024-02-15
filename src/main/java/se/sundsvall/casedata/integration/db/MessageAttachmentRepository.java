package se.sundsvall.casedata.integration.db;

import org.springframework.data.jpa.repository.JpaRepository;

import se.sundsvall.casedata.integration.db.model.MessageAttachment;

public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment, String> {

}
