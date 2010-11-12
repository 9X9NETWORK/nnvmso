package com.nnvmso.validator;

import java.util.Set;

import javax.validation.ConstraintViolation;

import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.nnvmso.form.*;
import com.nnvmso.model.NnUser;

@Component
public class NnUserSignupValidator implements Validator {
		
	@Override
	public boolean supports(Class clazz) {
		return UserSignupForm.class.isAssignableFrom(clazz); 
	}

	@Override
	public void validate(Object target, Errors errors) {
		UserSignupForm form = (UserSignupForm) target;		
		//http://www.jarvana.com/jarvana/view/org/springframework/spring-context/3.0.2.RELEASE/spring-context-3.0.2.RELEASE-sources.jar!/org/springframework/validation/beanvalidation/SpringValidatorAdapter.java?format=ok
		//http://stackoverflow.com/questions/4007541/spring-framework-form-validation-disallowed-field-validation
		//Set<ConstraintViolation<Object>> result = this
		ValidationUtils.rejectIfEmpty(errors, "mso.email", "required.mso", "Mso account is required");
		ValidationUtils.rejectIfEmpty(errors, "user.email", "required.email", "Email is required");				
		ValidationUtils.rejectIfEmpty(errors, "user.name", "required.name",  "Name is required");
		ValidationUtils.rejectIfEmpty(errors, "user.password", "required.password", "Password is required");
	}
	
}
