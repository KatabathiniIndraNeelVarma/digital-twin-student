package com.example.digitaltwin.security;
import com.example.digitaltwin.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private final UserRepository users;
    public DatabaseUserDetailsService(UserRepository users){this.users=users;}
    @Override public UserDetails loadUserByUsername(String email){return users.findByEmailIgnoreCase(email).orElseThrow(()->new UsernameNotFoundException("Account not found"));}
}
