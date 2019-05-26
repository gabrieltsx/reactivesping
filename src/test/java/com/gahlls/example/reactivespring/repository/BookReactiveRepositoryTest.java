package com.gahlls.example.reactivespring.repository;

import com.gahlls.example.reactivespring.model.Book;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

@DataMongoTest
@RunWith(SpringRunner.class)
@DirtiesContext
public class BookReactiveRepositoryTest {

    @Autowired
    private BookReactiveRepository bookReactiveRepository;

    List<Book> books = Arrays.asList(new Book(null, "Brave New World", 50.00),
                                     new Book(null, "Nineteen Eighty Four", 45.00),
                                     new Book(null, "Fahrenheit 451", 40.00),
                                     new Book("ABC", "Animal Farm", 20.00),
                                     new Book("DEF", "Blade Runner", 30.00));


    @Before
    public void setUp(){
        bookReactiveRepository.deleteAll()
                .thenMany(Flux.fromIterable(books))
                .flatMap(bookReactiveRepository::save)
                .doOnNext((book -> {
                    System.out.println("Inserted Book is :"+ book);
                }))
                .blockLast();
    }

    @Test
    public void getAllBooks(){
        StepVerifier.create(bookReactiveRepository.findAll())
                .expectSubscription()
                .expectNextCount(5)
                .verifyComplete();
    }

    @Test
    public void getBookById(){
        StepVerifier.create(bookReactiveRepository.findById("ABC"))
                .expectSubscription()
                .expectNextMatches(book -> book.getTitle().equals("Animal Farm"))
                .verifyComplete();
    }

    @Test
    public void getBookByTitle(){
        StepVerifier.create(bookReactiveRepository.findByTitle("Nineteen Eighty Four"))
                .expectSubscription()
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void saveBook(){

        Book book = new Book(null, "Foundation", 60.00);
        Mono<Book> savedUser = bookReactiveRepository.save(book);
        StepVerifier.create(savedUser.log("savedBook :"))
                .expectSubscription()
                .expectNextMatches(book1 -> book1.getId()!=null && book1.getTitle().equals("Foundation"))
                .verifyComplete();
    }

    @Test
    public void updateBook(){

        String newTitle = "Duna";
        Mono<Book> updateBook = bookReactiveRepository.findById("DEF")
                .map(book -> {
                    book.setTitle(newTitle);
                    return book;
                })
                .flatMap(book -> {
                    return bookReactiveRepository.save(book);
                });

        StepVerifier.create(updateBook.log("updateBook :"))
                .expectSubscription()
                .expectNextMatches(user -> user.getTitle().equals("Duna"))
                .verifyComplete();
    }

    @Test
    public void deleteBook(){

        Mono<Void> deletedBook = bookReactiveRepository.findByTitle("Fahrenheit 451")
                .flatMap((book) -> {
                    return bookReactiveRepository.delete(book);
                });

        StepVerifier.create(deletedBook.log("deleteBook :"))
                .expectSubscription()
                .verifyComplete();

        StepVerifier.create(bookReactiveRepository.findAll().log("The New Book List"))
                .expectSubscription()
                .expectNextCount(4)
                .verifyComplete();
    }
}
