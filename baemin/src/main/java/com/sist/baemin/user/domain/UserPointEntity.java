package com.sist.baemin.user.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class UserPointEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long pointId;
	
	@ManyToOne
	@JoinColumn(name="userId")
	private UserEntity userid;
	private Long currentPoint;
	private LocalDateTime lastUpdated;
}
