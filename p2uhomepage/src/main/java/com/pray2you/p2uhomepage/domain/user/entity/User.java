package com.pray2you.p2uhomepage.domain.user.entity;

import com.pray2you.p2uhomepage.domain.model.BaseTimeEntity;
import com.pray2you.p2uhomepage.domain.model.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Getter
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private long id;

    @Column(nullable = false)
    private String githubId;

    @Column(nullable = false)
    private String username;

    @Column
    private String profileImgUrl;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean deleted = false;

    @Builder
    public User(String githubId, String username, String profileImgUrl, String phoneNumber, String email, Role role) {
        this.githubId = githubId;
        this.username = username;
        this.profileImgUrl = profileImgUrl;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.role = role;
    }

    public User update(User updateUser) {
        this.githubId = updateUser.githubId;
        this.username = updateUser.username;
        this.profileImgUrl = updateUser.profileImgUrl;
        this.phoneNumber = updateUser.phoneNumber;
        this.email = updateUser.email;
        this.role = updateUser.role;
        this.deleted = updateUser.deleted;

        return this;
    }
}
