package school.sptech.cache;

public class EntradaCache {

    private final Conteudo conteudo;
    private final long armazenadoEm;
    private long ultimoAcesso;

    public EntradaCache(Conteudo conteudo) {
        this.conteudo = conteudo;
        long agora = System.currentTimeMillis();
        this.armazenadoEm = agora;
        this.ultimoAcesso = agora;
    }

    public Conteudo getConteudo() {
        return conteudo;
    }

    public long getUltimoAcesso() {
        return ultimoAcesso;
    }

    public void atualizarAcesso() {
        this.ultimoAcesso = System.currentTimeMillis();
    }

    public boolean expirado(long ttlMillis) {
        return System.currentTimeMillis() - armazenadoEm > ttlMillis;
    }
}
