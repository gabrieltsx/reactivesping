package com.gahlls.example.reactivespring.router;

import com.gahlls.example.reactivespring.handler.BookHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static com.gahlls.example.reactivespring.constants.BookConstants.BOOK_FUN_END_POINT;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class BookRouter {

    @Bean
    public RouterFunction<ServerResponse> bookRoute(BookHandler bookHandler){
        return RouterFunctions
                .route(GET(BOOK_FUN_END_POINT).and(accept(MediaType.APPLICATION_JSON))
                        ,bookHandler::getAll)
                .andRoute(GET(BOOK_FUN_END_POINT.concat("/{id}")).and(accept(MediaType.APPLICATION_JSON))
                        ,bookHandler::getOne)
                .andRoute(POST(BOOK_FUN_END_POINT).and(accept(MediaType.APPLICATION_JSON))
                        ,bookHandler::createBook)
                .andRoute(DELETE(BOOK_FUN_END_POINT.concat("/{id}")).and(accept(MediaType.APPLICATION_JSON))
                        ,bookHandler::deleteBook)
                .andRoute(PUT(BOOK_FUN_END_POINT.concat("/{id}")).and(accept(MediaType.APPLICATION_JSON))
                        ,bookHandler::updateBook);
    }
}
