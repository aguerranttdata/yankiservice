package com.group7.yanki.repository;

import com.group7.yanki.model.Account;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface AccountRepository extends ReactiveMongoRepository<Account,String> {
    Flux<Account> findAccountByPhone(Long phone);
}
