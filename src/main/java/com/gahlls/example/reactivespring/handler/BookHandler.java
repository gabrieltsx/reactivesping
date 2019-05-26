package com.gahlls.example.reactivespring.handler;

import com.gahlls.example.reactivespring.model.Book;
import com.gahlls.example.reactivespring.repository.BookReactiveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.BodyInserters.fromObject;

@Component
public class BookHandler {

    @Autowired
    private BookReactiveRepository bookReactiveRepository;

    private static Mono<ServerResponse> notFound = ServerResponse.notFound().build();

    public Mono<ServerResponse> getAll(ServerRequest serverRequest){
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(bookReactiveRepository.findAll(), Book.class);
    }

    public Mono<ServerResponse> getOne(ServerRequest serverRequest){

        String id = serverRequest.pathVariable("id");
        return bookReactiveRepository.findById(id).flatMap(item ->
                ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromObject(item)))
                .switchIfEmpty(notFound);
    }

    public Mono<ServerResponse> createBook(ServerRequest serverRequest){

        Mono<Book> itemMono = serverRequest.bodyToMono(Book.class);
        return itemMono.flatMap(item ->
                ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(bookReactiveRepository.save(item), Book.class));
    }

    public Mono<ServerResponse> deleteBook(ServerRequest serverRequest){

        String id = serverRequest.pathVariable("id");
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(bookReactiveRepository.deleteById(id), Void.class);
    }

    public Mono<ServerResponse> updateBook(ServerRequest serverRequest){

        String id = serverRequest.pathVariable("id");
        Mono<Book> updateBook = serverRequest.bodyToMono(Book.class).flatMap(book ->

                bookReactiveRepository.findById(id)
                        .flatMap(currentBook -> {
                            currentBook.setTitle(book.getTitle());
                            currentBook.setPrice(book.getPrice());
                            return bookReactiveRepository.save(currentBook);
                        })
        );

        return updateBook.flatMap(item ->
                ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromObject(item)))
                .switchIfEmpty(notFound);
    }

    public Mono<ServerResponse> itemsEx(ServerRequest serverRequest){

        throw new RuntimeException("RuntimeException Occurred");
    }
}
