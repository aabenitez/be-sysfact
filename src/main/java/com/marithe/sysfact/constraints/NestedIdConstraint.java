package com.marithe.sysfact.constraints;

import com.marithe.sysfact.validators.NestedIdValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = NestedIdValidator.class)
@Target({ METHOD, FIELD, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
public @interface NestedIdConstraint {
	String message() default "Invalid ID";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}