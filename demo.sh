#!/bin/bash
# Demo do cache - rode com a aplicacao no ar: mvn spring-boot:run
# Para o teste de TTL rapido, suba com: mvn spring-boot:run -Dspring-boot.run.arguments="--app.cache.ttl-segundos=15"

BASE=http://localhost:8080

echo "===== 0. Limpando cache e banco de testes ====="
curl -s -X DELETE $BASE/cache -o /dev/null -w "DELETE /cache -> %{http_code}\n"

echo ""
echo "===== 1. Cadastrando 7 conteudos ====="
for i in 1 2 3 4 5 6 7; do
  curl -s -X POST $BASE/conteudos -H "Content-Type: application/json" \
    -d "{\"titulo\":\"Artigo $i\",\"corpo\":\"Corpo do artigo $i\",\"autor\":\"Autor $i\"}" \
    -w "  -> HTTP %{http_code}\n"
done

echo ""
echo "===== 2. MISS depois HIT (mesmo id=1) ====="
curl -s -D - -o /dev/null -w "tempo total: %{time_total}s\n" $BASE/conteudos/1 | grep -E "X-Cache|tempo"
curl -s -D - -o /dev/null -w "tempo total: %{time_total}s\n" $BASE/conteudos/1 | grep -E "X-Cache|tempo"

echo ""
echo "===== 3. Enchendo o cache (ids 2 a 5) - agora tem 5 itens ====="
for i in 2 3 4 5; do
  curl -s -o /dev/null -w "GET /conteudos/$i -> %{http_code} em %{time_total}s\n" $BASE/conteudos/$i
done

echo ""
echo "===== 4. Acessando id=1 de novo (HIT - renova o acesso dele) ====="
curl -s -o /dev/null -w "GET /conteudos/1 -> %{http_code} em %{time_total}s\n" $BASE/conteudos/1

echo ""
echo "===== 5. Cache cheio: GET id=6 deve remover o LRU (id=2) ====="
curl -s -D - -o /dev/null -w "tempo total: %{time_total}s\n" $BASE/conteudos/6 | grep -E "X-Cache|tempo"
curl -s http://localhost:8080/cache/estatisticas; echo

echo ""
echo "===== 6. GET id=7 remove o proximo LRU (id=3) ====="
curl -s -o /dev/null -w "GET /conteudos/7 -> %{http_code} em %{time_total}s\n" $BASE/conteudos/7
curl -s $BASE/cache/estatisticas; echo

echo ""
echo "===== 7. Prova da remocao: GET id=2 de novo (foi evictado -> MISS) ====="
curl -s -D - -o /dev/null -w "tempo total: %{time_total}s\n" $BASE/conteudos/2 | grep -E "X-Cache|tempo"

echo ""
echo "===== 8. TTL: GET id=4, esperar expirar, GET de novo ====="
curl -s -o /dev/null -w "GET /conteudos/4 -> %{http_code}\n" $BASE/conteudos/4
echo "aguardando expiracao do TTL..."
sleep 16
curl -s -D - -o /dev/null -w "tempo total: %{time_total}s\n" $BASE/conteudos/4 | grep -E "X-Cache|tempo"

echo ""
echo "===== 9. Invalidacao: PUT em id=5 e GET depois ====="
curl -s -o /dev/null -w "PUT /conteudos/5 -> %{http_code}\n" -X PUT $BASE/conteudos/5 -H "Content-Type: application/json" -d '{"titulo":"Artigo 5 v2","corpo":"Corpo atualizado","autor":"Autor 5"}'
curl -s -D - -o /dev/null -w "tempo total: %{time_total}s\n" $BASE/conteudos/5 | grep -E "X-Cache|tempo"

echo ""
echo "===== 10. Estatisticas finais ====="
curl -s $BASE/cache/estatisticas; echo
