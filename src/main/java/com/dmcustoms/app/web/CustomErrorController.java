package com.dmcustoms.app.web;

import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dmcustoms.app.data.dto.ErrorDTO;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(path = "/error", produces = "application/json")
public class CustomErrorController implements ErrorController {

	@GetMapping
	public ResponseEntity<ErrorDTO> getError(HttpServletRequest request) {
		Integer status = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
		String message = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
		String uri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
		
		return ResponseEntity.status(status).body(new ErrorDTO(status, message, uri));
	}
	
}
