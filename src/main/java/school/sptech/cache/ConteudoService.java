package school.sptech.cache;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class ConteudoService {

    private final ConteudoRepository repository;
    private final ConteudoCache cache;

    public ConteudoService(ConteudoRepository repository, ConteudoCache cache) {
        this.repository = repository;
        this.cache = cache;
    }

    public ResultadoBusca buscarPorId(Long id) {
        return cache.obterOuBuscar(id, () -> buscarNaFonte(id));
    }

    public Conteudo criar(Conteudo conteudo) {
        conteudo.setAtualizadoEm(LocalDateTime.now());
        return repository.save(conteudo);
    }

    public Conteudo atualizar(Long id, Conteudo dados) {
        Conteudo existente = buscarOuFalhar(id);
        existente.setTitulo(dados.getTitulo());
        existente.setCorpo(dados.getCorpo());
        existente.setAutor(dados.getAutor());
        existente.setAtualizadoEm(LocalDateTime.now());
        Conteudo salvo = repository.save(existente);
        cache.invalidar(id);
        return salvo;
    }

    public void excluir(Long id) {
        buscarOuFalhar(id);
        repository.deleteById(id);
        cache.invalidar(id);
    }

    public EstatisticasCache estatisticas() {
        return cache.estatisticas();
    }

    public void limparCache() {
        cache.limpar();
    }

    private Conteudo buscarNaFonte(Long id) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return buscarOuFalhar(id);
    }

    private Conteudo buscarOuFalhar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conteúdo não encontrado"));
    }
}
