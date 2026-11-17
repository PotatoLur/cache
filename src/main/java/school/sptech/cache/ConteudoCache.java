package school.sptech.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

@Component
public class ConteudoCache {

    private static final Logger log = LoggerFactory.getLogger(ConteudoCache.class);

    private final Map<Long, EntradaCache> cache = new ConcurrentHashMap<>();
    private final int maxItens;
    private final long ttlMillis;
    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong misses = new AtomicLong();
    private final AtomicLong evictions = new AtomicLong();

    public ConteudoCache(@Value("${app.cache.max-itens:5}") int maxItens,
                         @Value("${app.cache.ttl-segundos:60}") long ttlSegundos) {
        this.maxItens = maxItens;
        this.ttlMillis = ttlSegundos * 1000;
    }

    public ResultadoBusca obterOuBuscar(Long id, Supplier<Conteudo> fonte) {
        long inicio = System.currentTimeMillis();
        EntradaCache entrada = cache.get(id);

        if (entrada != null && !entrada.expirado(ttlMillis)) {
            entrada.atualizarAcesso();
            hits.incrementAndGet();
            log.info("Cache HIT  - id={} - {} ms", id, System.currentTimeMillis() - inicio);
            return new ResultadoBusca(entrada.getConteudo(), true);
        }

        if (entrada != null) {
            cache.remove(id, entrada);
        }

        misses.incrementAndGet();
        Conteudo conteudo = fonte.get();
        adicionar(id, conteudo);
        log.info("Cache MISS - id={} - {} ms", id, System.currentTimeMillis() - inicio);
        return new ResultadoBusca(conteudo, false);
    }

    public void invalidar(Long id) {
        cache.remove(id);
    }

    public void limpar() {
        cache.clear();
    }

    public EstatisticasCache estatisticas() {
        return new EstatisticasCache(cache.size(), maxItens, hits.get(), misses.get(), evictions.get());
    }

    private synchronized void adicionar(Long id, Conteudo conteudo) {
        if (cache.containsKey(id)) {
            cache.put(id, new EntradaCache(conteudo));
            return;
        }
        if (cache.size() >= maxItens) {
            removerLru();
        }
        cache.put(id, new EntradaCache(conteudo));
    }

    private void removerLru() {
        Long chaveRemover = null;
        long acessoMaisAntigo = Long.MAX_VALUE;

        for (Map.Entry<Long, EntradaCache> item : cache.entrySet()) {
            long acesso = item.getValue().getUltimoAcesso();
            if (acesso < acessoMaisAntigo) {
                acessoMaisAntigo = acesso;
                chaveRemover = item.getKey();
            }
        }

        if (chaveRemover != null) {
            cache.remove(chaveRemover);
            evictions.incrementAndGet();
        }
    }
}
