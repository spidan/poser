package de.dfki.asr.poser.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PoserControllers {

	@GetMapping("/isAlive")
	public String testService()
	{
		return "ok";
	}

	@PostMapping(path = "/rdfToJson",
				consumes = "text/turtle",
				produces = "application/json")
	public ResponseEntity<?> getJsonFromRDF() {
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
