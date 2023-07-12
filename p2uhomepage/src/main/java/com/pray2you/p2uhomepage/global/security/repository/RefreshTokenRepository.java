package com.pray2you.p2uhomepage.global.security.repository;

import com.pray2you.p2uhomepage.global.security.refreshtoken.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}
