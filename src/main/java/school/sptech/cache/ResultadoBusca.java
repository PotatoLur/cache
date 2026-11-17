package school.sptech.cache;

public class ResultadoBusca {

    private final Conteudo conteudo;
    private final boolean hit;

    public ResultadoBusca(Conteudo conteudo, boolean hit) {
        this.conteudo = conteudo;
        this.hit = hit;
    }

    public Conteudo getConteudo() {
        return conteudo;
    }

    public boolean isHit() {
        return hit;
    }
}
