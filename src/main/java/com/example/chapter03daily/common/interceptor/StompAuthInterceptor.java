//package com.example.chapter03daily.common.interceptor;
//
//import com.example.chapter03daily.common.exception.ErrorCode;
//import com.example.chapter03daily.common.exception.ServiceException;
//import com.example.chapter03daily.common.utils.JwtUtil;
//import com.example.chapter03daily.domain.user.entity.User;
//import com.example.chapter03daily.domain.user.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.MessageChannel;
//import org.springframework.messaging.simp.stomp.StompCommand;
//import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
//import org.springframework.messaging.support.ChannelInterceptor;
//import org.springframework.messaging.support.MessageHeaderAccessor;
//import org.springframework.stereotype.Component;
//
//import java.util.Optional;
//
//import static com.example.chapter03daily.common.constant.MagicNumber.SEVEN;
//
//@Component
//@RequiredArgsConstructor
//public class StompAuthInterceptor implements ChannelInterceptor {
//
//    private final JwtUtil jwtUtil;
//    private final UserRepository userRepository;
//
//    private static final String AUTH_HEADER = "Authorization";
//
//    @Override
//    public Message<?> preSend(Message<?> message, MessageChannel channel) {
//        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
//
//        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
//            String token = accessor.getFirstNativeHeader(AUTH_HEADER)
//                    .substring(SEVEN);
//
//            String email = jwtUtil.extractEmail(token);
//
//            Optional<User> isExistUser = userRepository.findUserByEmail(email);
//
//            if (isExistUser.isEmpty()) {
//                throw new ServiceException(ErrorCode.USER_NOT_FOUND);
//            }
//
//            accessor.setUser(new AuthenticatedUser(isExistUser.get()));
//        }
//
//        return message;
//    }
//}

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

@Component
@RequiredArgsConstructor
public class StompAuthInterceptor implements ChannelInterceptor {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel
    ) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(
                        message,
                        StompHeaderAccessor.class
                );

        if (accessor == null) {
            return message;
        }

        if (!StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        String authorization =
                accessor.getFirstNativeHeader(AUTH_HEADER);

        System.out.println(
                "[STOMP] Authorization = " +
                        authorization
        );

        if (authorization == null ||
                authorization.isBlank()) {

            System.out.println(
                    "[STOMP] Authorization 없음"
            );

            throw new ServiceException(
                    ErrorCode.USER_NOT_FOUND
            );
        }

        String token =
                authorization.trim();

        /*
         * Bearer 제거
         */
        if (token.startsWith(BEARER_PREFIX)) {
            token =
                    token.substring(
                            BEARER_PREFIX.length()
                    ).trim();
        }

        if (token.isBlank()) {

            System.out.println(
                    "[STOMP] JWT 없음"
            );

            throw new ServiceException(
                    ErrorCode.USER_NOT_FOUND
            );
        }

        System.out.println(
                "[STOMP] JWT 추출 완료"
        );

        String email =
                jwtUtil.extractEmail(token);

        System.out.println(
                "[STOMP] JWT email = " + email
        );

        User user =
                userRepository
                        .findUserByEmail(email)
                        .orElseThrow(
                                () -> new ServiceException(
                                        ErrorCode.USER_NOT_FOUND
                                )
                        );

        accessor.setUser(
                new AuthenticatedUser(user)
        );

        System.out.println(
                "[STOMP] 인증 성공 = " +
                        user.getName()
        );

        return message;
    }
}