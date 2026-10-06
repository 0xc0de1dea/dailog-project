package com.example.chapter03daily.common.interceptor;

import com.example.chapter03daily.common.exception.ErrorCode;
import com.example.chapter03daily.common.exception.ServiceException;
import com.example.chapter03daily.common.utils.JwtUtil;
import com.example.chapter03daily.domain.user.entity.User;
import com.example.chapter03daily.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.example.chapter03daily.common.constant.MagicNumber.SEVEN;

@Component
@RequiredArgsConstructor
public class StompAuthInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    private static final String AUTH_HEADER = "Authorization";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader(AUTH_HEADER)
                    .substring(SEVEN);

            String email = jwtUtil.extractEmail(token);

            Optional<User> isExistUser = userRepository.findUserByEmail(email);

            if (isExistUser.isEmpty()) {
                throw new ServiceException(ErrorCode.USER_NOT_FOUND);
            }

            accessor.setUser(new AuthenticatedUser(isExistUser.get()));
        }

        return message;
    }
}
