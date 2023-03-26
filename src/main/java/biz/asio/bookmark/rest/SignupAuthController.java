package biz.asio.bookmark.rest;

import biz.asio.bookmark.model.User;
import biz.asio.bookmark.repository.UserRepository;
import biz.asio.bookmark.security.JwtUtil;
import biz.asio.bookmark.security.model.LoginDTO;
import biz.asio.bookmark.security.model.Message;
import biz.asio.bookmark.security.model.Token;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@AllArgsConstructor
@RestController
@RequestMapping("/auth")
public class SignupAuthController {

	AuthenticationManager authenticationManager;
	UserRepository userRepository;
	PasswordEncoder encoder;
	JwtUtil jwtUtil;

	@PostMapping("/login")
	public ResponseEntity<?> loginUser(@RequestBody LoginDTO loginDTO) {

		Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getUserName(), loginDTO.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtUtil.generateToken(authentication);

		// return token only
		// token object needed so we can have it wrapped inside regular JSON object
		return ResponseEntity.ok(new Token(jwt));
	}

	@PostMapping("/register")
	public ResponseEntity<?> registerUser(@RequestBody User registerDTO) {
		if (userRepository.existsByUserName(registerDTO.getUserName())) {
			return ResponseEntity
					.badRequest()
					.body(new Message("User already exists."));
		}

		if (userRepository.existsByEmail(registerDTO.getEmail())) {
			return ResponseEntity
					.badRequest()
					.body(new Message("Email already exists."));
		}

		// encode password before saving
		registerDTO.setPassword(encoder.encode(registerDTO.getPassword()));
		userRepository.save(registerDTO);

		return ResponseEntity.ok(new Message("User registered successfully."));
	}
}
