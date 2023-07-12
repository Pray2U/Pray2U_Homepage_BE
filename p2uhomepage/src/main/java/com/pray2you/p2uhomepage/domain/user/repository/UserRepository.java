package com.pray2you.p2uhomepage.domain.user.repository;

import com.pray2you.p2uhomepage.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByGithubId(String githubId);
}
