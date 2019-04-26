package com.dan.consumer;

import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.transport.netty.client.TcpClientTransport;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.stereotype.Controller;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@SpringBootApplication
public class ConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConsumerApplication.class, args);
	}


	@Bean
	RSocket rSocket () {
		return RSocketFactory
				.connect()
				.dataMimeType(MimeTypeUtils.APPLICATION_JSON_VALUE)
				.frameDecoder(PayloadDecoder.ZERO_COPY)
				.transport(TcpClientTransport.create(7000))
				.start()
				.block();
	}

	@Bean
	RSocketRequester requester(RSocketStrategies rSocketStrategies) {
		return RSocketRequester.create(this.rSocket(),
				MimeTypeUtils.APPLICATION_JSON, rSocketStrategies);
	}
}

@RestController
class GreetingController {

	private final RSocketRequester requester;

	GreetingController(RSocketRequester requester) {
		this.requester = requester;

	}

	@GetMapping("/error")
	Flux<GreetingsResponse> error() {
		return this.requester
				.route("error")
				.data(Mono.empty())
				.retrieveFlux(GreetingsResponse.class);
	}


	@GetMapping(value = "/greet/sse/{name}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	Flux<GreetingsResponse> greetStream (@PathVariable String name) {
		return  this.requester
				.route("greet-stream")
				.data(new GreetingRequest(name))
				.retrieveFlux(GreetingsResponse.class);


	}

	@GetMapping("/greet/{name}")
	Mono<GreetingsResponse> greet(@PathVariable String name){
		return this.requester
				.route("greet")
				.data(new GreetingRequest(name))
				.retrieveMono(GreetingsResponse.class);

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

	GreetingsResponse() {

	}

	GreetingsResponse(String name) {
		this.withGreetings("Hello" + name + " @ " + Instant.now());
	}

	GreetingsResponse withGreetings(String msg) {
		this.greetings = msg;
		return this;
	}
}

