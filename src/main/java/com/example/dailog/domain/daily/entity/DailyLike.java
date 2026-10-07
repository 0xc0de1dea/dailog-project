package com.example.dailog.domain.daily.entity;

import com.example.dailog.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "daily_likes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_daily_like_user",
                        columnNames = {"daily_id", "user_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_daily_like_daily_id",
                        columnList = "daily_id"
                )
        }
)
public class DailyLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_id", nullable = false)
    private Daily daily;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public DailyLike(Daily daily, User user) {
        this.daily = daily;
        this.user = user;
    }
}
