package com.lordkay.dispatchhub.dispatch;

import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Spring Data JDBC aggregate root for outbox claim SQL only.
 * Domain reads/writes of delivery jobs stay on the JPA {@code DeliveryJob} entity.
 */
@Table("delivery_job")
public class OutboxJob {

	@Id
	private UUID id;

	protected OutboxJob() {
	}

	public OutboxJob(UUID id) {
		this.id = id;
	}

	public UUID getId() {
		return id;
	}
}
