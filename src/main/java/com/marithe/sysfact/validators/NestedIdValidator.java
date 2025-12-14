package py.com.ventasjdbc.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import py.com.ventasjdbc.constraints.NestedIdConstraint;
import py.com.ventasjdbc.model.BaseEntity;

public class NestedIdValidator implements ConstraintValidator<NestedIdConstraint, BaseEntity> {

	@Override
	public void initialize(NestedIdConstraint nestedId) {
	}

	@Override
	public boolean isValid(BaseEntity entity, ConstraintValidatorContext cxt) {
		return entity != null && entity.getId() != null;
	}
}