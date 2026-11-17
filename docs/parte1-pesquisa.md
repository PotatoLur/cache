# Cache de Conteúdo — Pesquisa

**Aluno:** Guilherme Aoki
**Disciplina:** Backend

---

## 1. O que é cache e qual problema ele resolve?

Cache é uma camada de armazenamento temporário que guarda o resultado de uma operação cara (banco de dados, chamada de API, renderização de página) para que as próximas requisições iguais sejam respondidas quase instantaneamente, sem precisar executar a operação de novo. O problema que ele resolve é o custo de latência e de recursos: buscar um conteúdo direto na fonte a cada requisição desperdiça tempo do usuário e sobrecarrega o sistema de origem.

Exemplos do dia a dia:

- **Navegador:** quando você acessa um site, o navegador guarda imagens, CSS e JavaScript em cache. Na segunda visita, a página carrega muito mais rápido porque não precisa baixar tudo de novo.
- **CDN (Content Delivery Network):** serviços como Cloudflare guardam cópias de páginas e arquivos em servidores distribuídos pelo mundo, entregando o conteúdo a partir do ponto mais próximo do usuário.

Outros exemplos comuns: o banco de dados pode guardar em cache o resultado de consultas frequentes, e a própria aplicação pode manter objetos em memória (como o cache em `HashMap` feito neste exercício).

**Fontes:** MDN Web Docs — HTTP Caching (https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/Caching); AWS — O que é CDN (https://aws.amazon.com/pt/what-is/cdn/).

---

## 2. Cache hit, cache miss e hit ratio

- **Cache hit:** a requisição encontrou o dado no cache. É o cenário rápido — o conteúdo é retornado sem consultar a fonte.
- **Cache miss:** o dado não estava no cache (ou estava expirado). Nesse caso o sistema precisa buscar na fonte, o que é mais lento, e normalmente armazena o resultado no cache para os próximos acessos.
- **Hit ratio:** é a proporção de hits sobre o total de requisições (`hits / (hits + misses)`). Um hit ratio alto indica que o cache está sendo eficaz; um hit ratio baixo pode indicar TTL curto demais, limite pequeno ou padrão de acesso que não se repete.

**Fontes:** Redis — Patterns: Cache (https://redis.io/docs/latest/develop/use-cases/cache/); MDN Web Docs — HTTP Caching (https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/Caching).

---

## 3. O que é TTL e por que um dado em cache pode ficar "velho" (stale)?

TTL (Time To Live) é o tempo de vida configurado para um item em cache. Depois que esse tempo passa, o item é considerado expirado e a próxima requisição trata como *miss*, buscando o dado atualizado na fonte.

Um dado fica **stale** (velho) quando a fonte de dados muda mas o cache ainda guarda a versão antiga. Como o cache existe justamente para evitar consultas frequentes à fonte, ele nunca sabe sozinho que o dado mudou — ele só descobre quando o TTL expira ou quando alguém invalida a entrada manualmente. É por isso que TTL e invalidação são mecanismos complementares: o TTL limita o "tamanho" do problema, e a invalidação resolve os casos em que o dado mudou antes do TTL acabar.

**Fontes:** MDN Web Docs — HTTP Caching / Cache-Control (https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Cache-Control); Redis — Expiration (https://redis.io/docs/latest/develop/use/keyspace/#expiry).

---

## 4. Políticas de remoção: FIFO, LRU e LFU

Quando o cache está cheio e um novo item precisa entrar, a política de remoção (eviction) decide qual item sai:

- **FIFO (First In, First Out):** remove o item que entrou primeiro no cache, independente de ter sido usado ou não. É simples e justo em ordem de chegada, mas pode remover um item muito popular só porque ele entrou cedo.
- **LRU (Least Recently Used):** remove o item que foi **acessado há mais tempo**. Assume que quem não é usado há muito tempo provavelmente não será usado de novo. É a política mais comum e funciona bem quando o padrão de acesso tem repetição recente (ex.: o mesmo usuário navegando pelas mesmas páginas).
- **LFU (Least Frequently Used):** remove o item que foi **usado menos vezes no total**. Funciona melhor quando existe uma base de itens populares estáveis, mas é mais cara de implementar e pode "prender" itens que foram populares no passado e não são mais.

**Quando cada uma faz mais sentido:**

- **FIFO:** fluxos em que a ordem de chegada já é um bom indicador (ex.: fila de tarefas, cache de requisições em sequência).
- **LRU:** o caso mais geral de aplicações web — conteúdo acessado recentemente tende a ser acessado de novo. Foi a política escolhida neste exercício.
- **LFU:** workloads com poucos itens muito populares e muitos itens raramente acessados (ex.: cache de assets de um site com imagens "campeãs de acesso").

**Fontes:** Oracle Java Docs — LinkedHashMap (modo access-order implementa LRU) (https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/LinkedHashMap.html); Redis — Eviction policies (https://redis.io/docs/latest/develop/reference/eviction/).

---

## 5. O que é invalidação de cache? Por que é um dos problemas mais difíceis da computação?

Invalidação de cache é o processo de remover (ou atualizar) um item do cache quando o dado na fonte muda — por exemplo, quando um conteúdo é editado ou excluído. Sem invalidação, o cache continuaria entregando a versão antiga (stale) até o TTL expirar.

Diz-se que é um dos problemas mais difíceis da computação porque **não existe uma solução perfeita**: invalidar cedo demais desperdiça o cache (muitos misses), invalidar tarde demais entrega dados velhos. É a famosa frase de Phil Karlton: *"There are only two hard things in Computer Science: cache invalidation and naming things."* O problema fica ainda mais difícil em sistemas distribuídos, onde o dado pode mudar em uma instância enquanto o cache de outra ainda serve a versão antiga, e onde relógios e falhas de rede tornam a consistência entre os nós quase impossível de garantir.

**Fontes:** Martin Fowler — Two Hard Things (https://martinfowler.com/bliki/TwoHardThings.html); Redis — Cache Invalidation (https://redis.io/docs/latest/develop/use-cases/cache/).

---

## 6. Cache local vs. cache distribuído: quando o local deixa de ser suficiente?

- **Cache local (em memória da aplicação):** vive dentro do processo, como o `ConcurrentHashMap` usado neste exercício. É extremamente rápido (acesso direto à memória, sem rede), simples de implementar e não tem custo de infraestrutura. Porém cada instância da aplicação tem seu próprio cache — se você roda 3 réplicas, cada uma tem dados diferentes — e o cache some quando a aplicação reinicia.
- **Cache distribuído (ex.: Redis):** é um serviço separado, acessado pela rede, que pode ser compartilhado por todas as instâncias da aplicação. Garante que todas as réplicas vejam os mesmos dados, sobrevive a restarts da aplicação e permite políticas de expiração e persistência mais sofisticadas. O custo é a latência da rede e a complexidade de operar mais um serviço.

**Quando o local deixa de ser suficiente:**

1. **Múltiplas instâncias:** com mais de uma réplica, o cache local fica inconsistente — uma instância pode ter dado velho enquanto outra já atualizou. O Redis resolve isso centralizando o cache.
2. **Restart/deploy:** o cache local é zerado a cada deploy, causando uma onda de misses ("cache stampede") logo após a subida.
3. **Memória limitada por instância:** se o conjunto de dados é grande, dividir entre instâncias não escala; um cache dedicado com mais memória resolve.

**Fontes:** Redis — Use cases: Cache (https://redis.io/docs/latest/develop/use-cases/cache/); AWS — O que é Redis (https://aws.amazon.com/pt/elasticache/redis/).
