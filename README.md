# cache

## HashMap

### O que é um HashMap e em quais situações ele é útil?
HashMap é uma estrutura de dados que permite armazenar pares de chave-valor, onde cada chave é única e está associada a um valor específico. É útil para buscar valores rapidamente usando uma chave, como cache, dicionários, ou contagem de ocorrências.

### Como as chaves e os valores são armazenados? Para que serve a função hash?
As chaves e valores são armazenados em buckets organizados em um array. A função hash transforma a chave em um número inteiro que representa o índice. Permitindo buscar rapidamente um dado.

### Qual é o papel de hashCode() e equals() nesse processo?
O hashCode() gera o código hash da chave, que é usado pela função hash para calcular o índice do bucket. O equals() é usado para comparar chaves dentro do bucket, porque diferentes chaves podem cair no mesmo bucket. 

### O que são colisões e quais são as formas de tratá-las?
Colisão acontece quando duas chaves diferentes geram o mesmo índice após a função hash. Para tratar: chaining com listas ligadas, open addressing com probing linear/quadrático, ou redimensionamento da tabela. 

### Qual estratégia foi escolhida e por quê?
Chaining com ArrayList porque é mais simples de implementar. Quando há colisão, apenas adicionamos a nova entrada na lista do bucket. Para buscar, percorremos a lista até encontrar a chave. 

### Complexidade de tempo (Big O)

**put() - Inserção/Atualização:**
- Melhor caso: O(1) - quando o bucket está vazio ou a chave já está na primeira posição
- Pior caso: O(n) - quando todas as chaves colidem no mesmo bucket e precisamos percorrer a lista inteira

**get() - Busca:**
- Melhor caso: O(1) - quando a chave está na primeira posição do bucket
- Pior caso: O(n) - quando todas as chaves colidem no mesmo bucket e precisamos percorrer a lista inteira

O pior caso acontece quando a função hash não distribui bem as chaves, fazendo com que tudo caia no mesmo bucket. Na prática, com uma boa função hash e capacidade adequada, os casos O(1) são predominantes.

## Cache de Conteúdo (Spring Boot)

API que serve conteúdos usando um cache próprio construído com `ConcurrentHashMap`, simulando uma fonte lenta (`Thread.sleep(2000)` na busca).

### Como rodar

```bash
mvn spring-boot:run
```

A aplicação sobe em `http://localhost:8080` com banco H2 em memória.

### Configuração

Em `src/main/resources/application.properties`:

```properties
app.cache.max-itens=5
app.cache.ttl-segundos=60
```

### Como o cache funciona

- `ConteudoCache` guarda os conteúdos em um `Map<Long, EntradaCache>`. Cada `EntradaCache` armazena o conteúdo, o momento em que foi guardado (`armazenadoEm`) e o último acesso (`ultimoAcesso`).
- **HIT**: o conteúdo está no cache e não expirou. O `ultimoAcesso` é atualizado e a resposta volta com o header `X-Cache: HIT`.
- **MISS**: o conteúdo não está no cache ou expirou (TTL). Busca na fonte (com atraso de 2s), armazena no cache e responde com `X-Cache: MISS`.
- **Limite (LRU)**: ao atingir `max-itens`, o item com o `ultimoAcesso` mais antigo é removido e contabilizado como eviction.
- **Invalidação**: `PUT` e `DELETE /conteudos/{id}` removem a entrada correspondente do cache.
- **Estatísticas**: hits, misses, evictions e hit ratio são expostos em `GET /cache/estatisticas`. `DELETE /cache` limpa tudo.

### Endpoints

| Método | Rota | Descrição |
|---|---|---|
| GET | `/conteudos/{id}` | Busca no cache; se MISS, busca na fonte (lenta) e armazena. Header `X-Cache: HIT/MISS` |
| POST | `/conteudos` | Cadastra um conteúdo |
| PUT | `/conteudos/{id}` | Atualiza e invalida o cache |
| DELETE | `/conteudos/{id}` | Exclui e invalida o cache |
| GET | `/cache/estatisticas` | Tamanho, limite, hits, misses, evictions e hit ratio |
| DELETE | `/cache` | Limpa todo o cache |

### Exemplo de requisição

```bash
curl -X POST http://localhost:8080/conteudos -H "Content-Type: application/json" -d '{"titulo":"Artigo 1","corpo":"Corpo","autor":"Ana"}'
curl http://localhost:8080/conteudos/1
curl http://localhost:8080/cache/estatisticas
```
