package com.marithe.sysfact.model;

import javax.validation.constraints.NotNull;

public class BaseEntity {

	@NotNull(message = "El id no puede estar nulo")
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
