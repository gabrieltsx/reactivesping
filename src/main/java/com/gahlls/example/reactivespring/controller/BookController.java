package com.gahlls.example.reactivespring.controller;

import com.gahlls.example.reactivespring.constants.BookConstants;
import com.gahlls.example.reactivespring.model.Book;
import com.gahlls.example.reactivespring.repository.BookReactiveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(BookConstants.BOOK_END_POINT)
public class BookController {

    @Autowired
    private BookReactiveRepository bookReactiveRepository;


    @GetMapping
    public Flux<Book> getAll(){
        return bookReactiveRepository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Book> createBook(@RequestBody Book book){
        return bookReactiveRepository.insert(book);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Book>> getOne(@PathVariable String id){
        return bookReactiveRepository.findById(id)
                .map((book) -> new ResponseEntity<>(book, HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteBook(@PathVariable String id){
        return bookReactiveRepository.deleteById(id);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Book>> updateBook(@PathVariable String id, @RequestBody Book book){
        return bookReactiveRepository.findById(id)
                .flatMap((bookCurrent) ->{

                   bookCurrent.setPrice(book.getPrice());
                   bookCurrent.setTitle(book.getTitle());
                   return bookReactiveRepository.save(bookCurrent);
                })
                .map((updatedBook) -> new ResponseEntity<>(updatedBook, HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/runtimeException")
    public Flux<Book> runtimeException(){
        return bookReactiveRepository.findAll()
                .concatWith(Mono.error(new RuntimeException("RuntimeException Occurred.")));
    }
}
