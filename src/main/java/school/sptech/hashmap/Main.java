package school.sptech.hashmap;

public class Main {
    public static void main(String[] args) {
        Mapa<Integer, String> mapa = new Mapa<>(10);

        mapa.put(1, "Valor 1");
        mapa.put(2, "Valor 2");
        mapa.put(3, "Valor 3");
        System.out.println(mapa.get(1));
        System.out.println(mapa.get(2));
        System.out.println(mapa.get(3));

        mapa.put(1, "Atualizado");
        System.out.println(mapa.get(1));
        System.out.println(mapa.get(99));
        System.out.println(mapa.get(1));
    }
}
