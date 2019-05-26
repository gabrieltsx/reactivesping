package com.gahlls.example.reactivespring.repository;

import com.gahlls.example.reactivespring.model.Book;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface BookReactiveRepository extends ReactiveMongoRepository<Book, String> {

    Mono<Book> findByTitle(String title);
}
