package com.bank.audit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.util.Arrays;

@Aspect
@Component
public class AuditLoggingAspect {
	private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

	@Around("@annotation(com.bank.audit.Auditable)")
	public Object around(ProceedingJoinPoint pjp) throws Throwable {
		MethodSignature sig = (MethodSignature) pjp.getSignature();
		Auditable auditable = sig.getMethod().getAnnotation(Auditable.class);

		String principal = resolvePrincipal();
		String method = sig.getDeclaringType().getSimpleName() + "." + sig.getName();
		String args = safeArgs(pjp.getArgs());

		long start = System.currentTimeMillis();
		auditLog.info("action={} principal={} method={} args={}", auditable.action(), principal, method, args);
		try {
			Object result = pjp.proceed();
			auditLog.info("action={} principal={} method={} status=SUCCESS durationMs={}",
					auditable.action(), principal, method, System.currentTimeMillis() - start);
			return result;
		} catch (Exception ex) {
			auditLog.info("action={} principal={} method={} status=FAILED durationMs={} error={}",
					auditable.action(), principal, method, System.currentTimeMillis() - start, ex.getClass().getSimpleName());
			throw ex;
		}
	}

	private String resolvePrincipal() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated()) return "anonymous";
		return auth.getName();
	}

	private String safeArgs(Object[] args) {
		if (args == null) return "[]";
		return Arrays.toString(Arrays.stream(args).map(a -> {
			if (a == null) return null;
			String s = a.toString();
			if (s.toLowerCase().contains("password")) return "[REDACTED]";
			return s;
		}).toArray());
	}
}
