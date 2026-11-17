package school.sptech.cache;

public class ConteudoMapper {

    private ConteudoMapper() {
    }

    public static ConteudoResponse toResponse(Conteudo conteudo) {
        return new ConteudoResponse(
                conteudo.getId(),
                conteudo.getTitulo(),
                conteudo.getCorpo(),
                conteudo.getAutor(),
                conteudo.getAtualizadoEm()
        );
    }

    public static Conteudo toEntity(ConteudoRequest request) {
        Conteudo conteudo = new Conteudo();
        conteudo.setTitulo(request.getTitulo());
        conteudo.setCorpo(request.getCorpo());
        conteudo.setAutor(request.getAutor());
        return conteudo;
    }
}
