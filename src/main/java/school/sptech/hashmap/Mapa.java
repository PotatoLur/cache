package school.sptech.hashmap;

import java.util.ArrayList;

class Mapa<K, V> {
    private ArrayList<Entrada<K, V>>[] tabela;
    private int capacidade;

    public Mapa(int capacidade) {
        this.capacidade = capacidade;
        this.tabela = new ArrayList[capacidade];
    }

    private void inicializar() {
        for (int i = 0; i < capacidade; i++) {
            tabela[i] = new ArrayList<>();
        }
    }

    private int hash(K chave) {
        return Math.abs(chave.hashCode() % capacidade);
    }

    public void put(K chave, V valor) {
        if (tabela[0] == null) {
            inicializar();
        }
        int indice = hash(chave);
        ArrayList<Entrada<K, V>> bucket = tabela[indice];

        for (Entrada<K, V> entrada : bucket) {
            if (entrada.chave.equals(chave)) {
                entrada.valor = valor;
                return;
            }
        }

        bucket.add(new Entrada<>(chave, valor));
    }

    public V get(K chave) {
        if (tabela[0] == null) {
            return null;
        }
        int indice = hash(chave);
        ArrayList<Entrada<K, V>> bucket = tabela[indice];

        for (Entrada<K, V> entrada : bucket) {
            if (entrada.chave.equals(chave)) {
                return entrada.valor;
            }
        }

        return null;
    }

    private static class Entrada<K, V> {
        K chave;
        V valor;

        Entrada(K chave, V valor) {
            this.chave = chave;
            this.valor = valor;
        }
    }
}
