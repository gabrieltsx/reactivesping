package com.gahlls.example.reactivespring.handler;

import com.gahlls.example.reactivespring.model.Book;
import com.gahlls.example.reactivespring.repository.BookReactiveRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

import static com.gahlls.example.reactivespring.constants.BookConstants.BOOK_FUN_END_POINT;

@SpringBootTest
@RunWith(SpringRunner.class)
@DirtiesContext
@AutoConfigureWebTestClient
@ActiveProfiles("test")
public class BookHandlerTest {

    @Autowired
    private BookReactiveRepository bookReactiveRepository;

    @Autowired
    private WebTestClient webTestClient;

    public List<Book> data(){
        return Arrays.asList(new Book(null, "Brave New World", 50.00),
                new Book(null, "Nineteen Eighty Four", 45.00),
                new Book(null, "Fahrenheit 451", 40.00),
                new Book("ABC", "Animal Farm", 20.00),
                new Book("DEF", "Blade Runner", 30.00));
    }

    @Before
    public void setUp(){

        bookReactiveRepository.deleteAll()
                .thenMany(Flux.fromIterable(data()))
                .flatMap(bookReactiveRepository::save)
                .doOnNext((book ->{
                    System.out.println("Inserted book is : " + book);
                }))
                .blockLast();
    }

    @Test
    public void getAllBooks(){
        webTestClient.get().uri(BOOK_FUN_END_POINT)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Book.class)
                .hasSize(5);
    }

    @Test
    public void getOneBook(){
        webTestClient.get().uri(BOOK_FUN_END_POINT.concat("/{id}"), "ABC")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title", "Animal Farm");

    }

    @Test
    public void getOneBook_notFound(){
        webTestClient.get().uri(BOOK_FUN_END_POINT.concat("/{id}"), "XYZ")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void createBook(){
        Book newBook = new Book(null, "The Lord of the Rings", 38.00);

        webTestClient.post().uri(BOOK_FUN_END_POINT)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(newBook), Book.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.title").isEqualTo("The Lord of the Rings")
                .jsonPath("$.price").isEqualTo(38.00);
    }

    @Test
    public void deleteItem(){

        webTestClient.delete().uri(BOOK_FUN_END_POINT.concat("/{id}"), "ABC")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class);
    }

    @Test
    public void updateItem(){
        double newPrice = 70.99;
        Book book = new Book(null, "A Song Of Ice And Fire", newPrice);

        webTestClient.put().uri(BOOK_FUN_END_POINT.concat("/{id}"), "ABC")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(book), Book.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.price", newPrice);

    }

    @Test
    public void updateItem_notFound(){
        double newPrice = 41.99;
        Book book = new Book(null, "The Silmarillion", newPrice);

        webTestClient.put().uri(BOOK_FUN_END_POINT.concat("/{book}"), "XYZ")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(book), Book.class)
                .exchange()
                .expectStatus().isNotFound();
    }
}
