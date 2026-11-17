package school.sptech.cache;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/conteudos")
public class ConteudoController {

    private final ConteudoService service;

    public ConteudoController(ConteudoService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConteudoResponse> buscar(@PathVariable Long id) {
        ResultadoBusca resultado = service.buscarPorId(id);
        String cacheStatus = resultado.isHit() ? "HIT" : "MISS";
        return ResponseEntity.ok()
                .header("X-Cache", cacheStatus)
                .body(ConteudoMapper.toResponse(resultado.getConteudo()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConteudoResponse criar(@RequestBody ConteudoRequest request) {
        return ConteudoMapper.toResponse(service.criar(ConteudoMapper.toEntity(request)));
    }

    @PutMapping("/{id}")
    public ConteudoResponse atualizar(@PathVariable Long id, @RequestBody ConteudoRequest request) {
        return ConteudoMapper.toResponse(service.atualizar(id, ConteudoMapper.toEntity(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
