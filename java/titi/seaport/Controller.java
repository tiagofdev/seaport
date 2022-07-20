package titi.seaport;

import titi.seaport.Implementation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping( "/")
@RequiredArgsConstructor
public class Controller {
    // Controller also usually called resource in webapp development
    private final Implementation algorithm;


    @GetMapping("/load")
    public ResponseEntity<Response> loadFile() throws IOException {
        System.out.println("load called");
        return ResponseEntity.ok(
                Response.builder()
                        .timeStamp(now())
                        .status(OK)
                        .statusCode(OK.value())
                        .message("Result retrieved from server.")
                        .data(Map.of("map",algorithm.loadFile()))
                        .build()
        );
    }

    @GetMapping("/status")
    public ResponseEntity<Response> getStatus() {
        return ResponseEntity.ok(
                Response.builder()
                        .timeStamp(now())
                        .status(OK)
                        .statusCode(OK.value())
                        .message("Result retrieved from server.")
                        .data(Map.of("ships",algorithm.getStatus()))
                        .build()
        );
    }

    @GetMapping("/start")
    public ResponseEntity<Response> start() {
        return ResponseEntity.ok(
                Response.builder()
                        .timeStamp(now())
                        .status(OK)
                        .statusCode(OK.value())
                        .message("Result retrieved from server.")
                        .data(Map.of("result",algorithm.start()))
                        .build()
        );
    }

    @GetMapping("/cancelall")
    public ResponseEntity<Response> cancelAll() {
        return ResponseEntity.ok(
                Response.builder()
                        .timeStamp(now())
                        .status(OK)
                        .statusCode(OK.value())
                        .message("Result retrieved from server.")
                        .data(Map.of("result",algorithm.cancelAll()))
                        .build()
        );
    }

    @GetMapping("/pauseall")
    public ResponseEntity<Response> pauseAll() {
        return ResponseEntity.ok(
                Response.builder()
                        .timeStamp(now())
                        .status(OK)
                        .statusCode(OK.value())
                        .message("Result retrieved from server.")
                        .data(Map.of("result",algorithm.pauseAll()))
                        .build()
        );
    }

    @GetMapping("/cancel/{job}")
    public ResponseEntity<Response> cancel(@PathVariable("job") int job) {
        return ResponseEntity.ok(
                Response.builder()
                        .timeStamp(now())
                        .status(OK)
                        .statusCode(OK.value())
                        .message("Result retrieved from server.")
                        .data(Map.of("result",algorithm.cancel(job)))
                        .build()
        );
    }

    @GetMapping("/pause/{job}")
    public ResponseEntity<Response> pause(@PathVariable("job") int job) {
        return ResponseEntity.ok(
                Response.builder()
                        .timeStamp(now())
                        .status(OK)
                        .statusCode(OK.value())
                        .message("Result retrieved from server.")
                        .data(Map.of("result",algorithm.pause(job)))
                        .build()
        );
    }

    @GetMapping("/download")
    public ResponseEntity<Response> download() throws IOException {
        System.out.println("Was I called?");
        return ResponseEntity.ok(
                Response.builder()
                        .timeStamp(now())
                        .status(OK)
                        .statusCode(OK.value())
                        .message("Result retrieved from server.")
                        .data(Map.of("result",algorithm.download()))
                        .build()
        );
    }
}
