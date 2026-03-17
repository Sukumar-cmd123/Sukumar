package com.bank.security;
import com.bank.entity.Customer;
import com.bank.entity.CustomerStatus;
import com.bank.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerUserDetailsService implements UserDetailsService {
	private final CustomerRepository customerRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Customer c = customerRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));

		boolean enabled = c.getStatus() == CustomerStatus.ACTIVE;
		return User.builder()
				.username(c.getUsername())
				.password(c.getPassword())
				.disabled(!enabled)
				.authorities(List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
				.build();
	}
}

