package com.ks.tocho5.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "game")
public class GameStatusModel{
	@Id
	private Integer game_id;
	private String status;
	
	
}