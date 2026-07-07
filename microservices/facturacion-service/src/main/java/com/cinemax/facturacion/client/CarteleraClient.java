// client/CarteleraClient.java
@FeignClient(name = "cartelera-service")
public interface CarteleraClient {

    @GetMapping("/internal/showtimes/{idShowtime}")
    ShowtimeDTO getShowtime(@PathVariable("idShowtime") Integer idShowtime);
}