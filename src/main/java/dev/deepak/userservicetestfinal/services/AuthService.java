package dev.deepak.userservicetestfinal.services;

import dev.deepak.userservicetestfinal.dtos.UserDto;
import dev.deepak.userservicetestfinal.models.SessionStatus;
import dev.deepak.userservicetestfinal.models.User;
import dev.deepak.userservicetestfinal.models.Session;
import dev.deepak.userservicetestfinal.repositories.SessionRepository;
import dev.deepak.userservicetestfinal.repositories.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.MacAlgorithm;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMapAdapter;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class AuthService {
    private UserRepository userRepository;
    private SessionRepository sessionRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public AuthService(UserRepository userRepository, SessionRepository sessionRepository,BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.bCryptPasswordEncoder=bCryptPasswordEncoder;
    }

    public ResponseEntity<UserDto> login(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            throw new RuntimeException("Email is wrong");
        }

        User user = userOptional.get();

        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Password is wrong for "+email);
        }
        long userId=user.getId();
        Optional<List<Session>> optionalSessions=sessionRepository.findByUser_IdAndSessionStatus(userId,SessionStatus.ACTIVE);
        if(optionalSessions.isPresent()&&optionalSessions.get().size()==2){
            throw new RuntimeException("Already 2 sessions Exist");
        }

       // String token = RandomStringUtils.randomAlphanumeric(30);
        MacAlgorithm alg = Jwts.SIG.HS256; //or HS384 or HS256
        SecretKey key = alg.key().build();

       // String message = "Muheed Madarchod!";
        Map<String,Object> jsonMap=new HashMap<>();
        jsonMap.put("email",user.getEmail());
        jsonMap.put("roles",List.of(user.getRoles()));
        jsonMap.put("createdAt",new Date());
        jsonMap.put("expiryAt", DateUtils.addDays(new Date(),30));
        //byte[] content = message.getBytes(StandardCharsets.UTF_8);

        // Create the compact JWS:
       // String jws = Jwts.builder().content(content, "text/plain").signWith(key, alg).compact();
        String jws=Jwts.builder()
                .claims(jsonMap)
                .signWith(key,alg)
                .compact();

        // Parse the compact JWS:
        //content = Jwts.parser().verifyWith(key).build().parseSignedContent(jws).getPayload();

        //assert message.equals(new String(content, StandardCharsets.UTF_8));

        Session session = new Session();
        session.setSessionStatus(SessionStatus.ACTIVE);
        session.setToken(jws);
        session.setUser(user);
        //session.setExpiringAt(new Date());
        sessionRepository.save(session);

        UserDto userDto = new UserDto();
        userDto.setEmail(email);

//        Map<String, String> headers = new HashMap<>();
//        headers.put(HttpHeaders.SET_COOKIE, token);

        MultiValueMapAdapter<String, String> headers = new MultiValueMapAdapter<>(new HashMap<>());
        headers.add(HttpHeaders.SET_COOKIE, "auth-token:" + jws);



        ResponseEntity<UserDto> response = new ResponseEntity<>(userDto, headers, HttpStatus.OK);
//        response.getHeaders().add(HttpHeaders.SET_COOKIE, token);

        return response;
    }

    public ResponseEntity<Void> logout(String token, Long userId) {
        Optional<Session> sessionOptional = sessionRepository.findByTokenAndUser_Id(token, userId);

        if (sessionOptional.isEmpty()) {
            return null;
        }

        Session session = sessionOptional.get();

        session.setSessionStatus(SessionStatus.ENDED);

        sessionRepository.save(session);

        return ResponseEntity.ok().build();
    }

    public UserDto signUp(String email, String password) throws RuntimeException{
        Optional<User> optionalUser=userRepository.findByEmail(email);
        if(!optionalUser.isEmpty()){
           throw new RuntimeException("User already exist");
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(bCryptPasswordEncoder.encode(password));
        
        User savedUser = userRepository.save(user);

        return UserDto.from(savedUser);
    }

    public SessionStatus validate(String token, Long userId) {
        Optional<Session> sessionOptional = sessionRepository.findByTokenAndUser_Id(token, userId);

        if (sessionOptional.isEmpty()) {
            return null;
        }

        return SessionStatus.ACTIVE;
    }

}
