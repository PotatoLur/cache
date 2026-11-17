package school.sptech.cache;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cache")
public class CacheController {

    private final ConteudoService service;

    public CacheController(ConteudoService service) {
        this.service = service;
    }

    @GetMapping("/estatisticas")
    public EstatisticasCache estatisticas() {
        return service.estatisticas();
    }

    @DeleteMapping
    public ResponseEntity<Void> limpar() {
        service.limparCache();
        return ResponseEntity.noContent().build();
    }
}
