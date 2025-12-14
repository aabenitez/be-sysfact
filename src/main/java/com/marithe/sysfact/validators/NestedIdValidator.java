package com.marithe.sysfact.validators;

import com.marithe.sysfact.constraints.NestedIdConstraint;
import com.marithe.sysfact.model.BaseEntity;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NestedIdValidator implements ConstraintValidator<NestedIdConstraint, BaseEntity> {

	@Override
	public void initialize(NestedIdConstraint nestedId) {
	}

	@Override
	public boolean isValid(BaseEntity entity, ConstraintValidatorContext cxt) {
		return entity != null && entity.getId() != null;
	}
}