package com.sist.baemin.user.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "userPoint")
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
