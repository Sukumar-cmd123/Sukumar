package com.bank.exception;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(AccountNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleAccountNotFound(AccountNotFoundException ex, HttpServletRequest req) {
		return build(HttpStatus.NOT_FOUND, ex.getMessage(), req.getRequestURI());
	}

	@ExceptionHandler(InsufficientBalanceException.class)
	public ResponseEntity<ApiErrorResponse> handleInsufficient(InsufficientBalanceException ex, HttpServletRequest req) {
		return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req.getRequestURI());
	}

	@ExceptionHandler(InvalidOtpException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidOtp(InvalidOtpException ex, HttpServletRequest req) {
		return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req.getRequestURI());
	}

	@ExceptionHandler(UnauthorizedAccessException.class)
	public ResponseEntity<ApiErrorResponse> handleUnauthorized(UnauthorizedAccessException ex, HttpServletRequest req) {
		return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), req.getRequestURI());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
		String msg = ex.getBindingResult()
				.getAllErrors()
				.stream()
				.map(err -> {
					if (err instanceof FieldError fe) {
						return fe.getField() + ": " + fe.getDefaultMessage();
					}
					return err.getDefaultMessage();
				})
				.collect(Collectors.joining(", "));
		return build(HttpStatus.BAD_REQUEST, msg, req.getRequestURI());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", req.getRequestURI());
	}

	private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message, String path) {
		ApiErrorResponse body = ApiErrorResponse.builder()
				.timestamp(Instant.now())
				.status(status.value())
				.error(status.getReasonPhrase())
				.message(message)
				.path(path)
				.build();
		return ResponseEntity.status(status).body(body);
	}
}

