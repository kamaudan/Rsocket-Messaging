package com.dan.Producer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;
import java.util.stream.Stream;

@SpringBootApplication
public class ProducerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProducerApplication.class, args);
	}

}

@Controller
class GreetingsRsocketController {

	@MessageMapping("error")
	Flux<GreetingsResponse> error() {
		return Flux.error(new IllegalArgumentException());
	}

	@MessageExceptionHandler
	Flux<GreetingsResponse> errorHandler(IllegalArgumentException iae ) {
		return Flux.just(new GreetingsResponse()
		.withGreetings("OH NO NO!"));
	}


	@MessageMapping("greet-stream")
	Flux<GreetingsResponse> greetStream(GreetingRequest request) {
		return Flux.fromStream(Stream.generate(() -> new GreetingsResponse(request.getName()
		))).delayElements(Duration.ofSeconds(1));




	}


	@MessageMapping("greet")
	GreetingsResponse greet( GreetingRequest request) {
		return  new GreetingsResponse(request.getName());
	}

}



@Data
@AllArgsConstructor
@NoArgsConstructor
class GreetingRequest {

	private String name;

}

@Data
class GreetingsResponse {

	private String greetings;

	GreetingsResponse(){

	}

	GreetingsResponse(String name) {
		this.withGreetings("Hello" +   name + " @ " + Instant.now());
	}

	GreetingsResponse withGreetings( String msg) {
		this.greetings = msg;
		return  this;
	}
}




