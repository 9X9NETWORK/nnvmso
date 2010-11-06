package com.nnvmso.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.nnvmso.form.UserSignupForm;

@Component
public class NnUserSignupValidator implements Validator {

	@Override
	public boolean supports(Class clazz) {
		return UserSignupForm.class.isAssignableFrom(clazz); 
	}

	@Override
	public void validate(Object target, Errors errors) {		
		ValidationUtils.rejectIfEmpty(errors, "mso.email", "required.mso", "Mso account is required");
		ValidationUtils.rejectIfEmpty(errors, "user.email", "required.email", "Email is required");				
		ValidationUtils.rejectIfEmpty(errors, "user.name", "required.name",  "Name is required");
		ValidationUtils.rejectIfEmpty(errors, "user.password", "required.password", "Password is required");
	}
	
}
