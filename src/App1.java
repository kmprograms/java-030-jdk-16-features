import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class App1 {
    static List<Integer> findDivisors(int number) {
        return Stream.concat(
                IntStream
                        .range(1, number / 2 + 1)
                        .filter(element -> number % element == 0)
                        .boxed(),
                Stream.of(number))
                .toList();
    }

    public static void main(String[] args) {

        // 1. Zamiast uzywac Collectors.toList mozesz od teraz stosowac Stream.toList
        System.out.println("----------------------------------- 1 ---------------------------------");
        var numbers1 = Stream
                .of(10, 30, 50, 70)
                .map(x -> x / 10)
                // .collect(Collectors.toList())
                .toList();
        System.out.println(numbers1);

        // 2. Zamiast stosowac flatMap mozesz stosowac mapMulti

        // Na poczatek jak dziala flatMap?
        // Zasada dzialania: podajesz funkcje ktora mapuje kolejny element strumienia i dostajesz
        // strumien elementow po takim mapowaniu. To podejscie ma kilka wad:
        // -> klopot z przechodzeniem na stream
        // -> tworzenie duzej ilosci malych strumieni moze wplynac na wydajnosc przetwarzania

        // Rozwiazaniem tych problemow jest mapMulti
        // <R> Stream<R> mapMulti(BiConsumer<T, Consumer<R>> mapper)
        // Kazdy element jest mapowany na 0, 1 lub kilka elementow, ale dla kazdego elementu nie zwracasz strumienia
        // tylko na rzecz elementow wynikowych mapowania wywolujesz instancje Consumer-a podana jako drugi argument

        // Scenariusze uzycia

        // a. one to some (0..1) mapping
        System.out.println("----------------------------------- 2.1 -------------------------------");
        Stream
                .of("ABCD", "EFG", "HIJKLM")
                .mapMulti((expression, consumer) -> {
                    if (expression.length() >= 4) {
                        consumer.accept(expression.length());
                    }
                }).forEach(System.out::println);

        // b. one to one mapping
        System.out.println("----------------------------------- 2.2 -------------------------------");
        Stream
                .of("ABCD", "EFG", "HIJKLM")
                .mapMulti((expression, consumer) -> {
                        consumer.accept(expression.length());
                }).forEach(System.out::println);

        // c. one to many mapping
        System.out.println("----------------------------------- 2.3 -------------------------------");
        Stream
                .of("ABCD", "EFG", "HIJKLM")
                .mapMulti((expression, consumer) -> {
                    List.of(expression.split("")).forEach(consumer);
                }).forEach(System.out::println);

        // mapMulti pod spodem wykorzystuje jeden strumien zamiast tworzyc dla kazdego elementu osobny strumien.
        // kolejne otrzymywane w wyniku mapowania elementy za pomoca consumer-a sa propagowane do tego jednego
        // wewentrznego strumienia i potem ten strumien mozemy przetwarzac kolejnymi metodami
        // taki sposob zarzadzania elementami jest mozliwy dzieki zastosowaniu "pod spodem" SpinedBuffer

        /*
            One or more arrays are used to store elements. The use of a multiple arrays has better performance
            characteristics than a single array used by ArrayList, as when the capacity of the list
            needs to be increased no copying of elements is required. This is usually beneficial in the case
            where the results will be traversed a small number of times
        */

        // d. mozesz za pomoca mapMulti realizowac zachowania ktore lacza w sobie dzialania filter + map
        System.out.println("----------------------------------- 2.4 -------------------------------");

        int sum1 = Stream.of(1, 2.0, 3, 4L)
                .filter(number -> number instanceof Integer)
                .mapToInt(number -> (Integer) number)
                .sum();
        System.out.println("SUM1 = " + sum1);

        int sum2 = Stream.of(1, 2.0, 3, 4L)
                .mapMultiToInt((number, consumer) -> {
                    if (number instanceof Integer) {
                        consumer.accept((Integer) number);
                    }
                })
                .sum();
        System.out.println("SUM2 = " + sum2);

        // e. ciekawostka odnosnie Optional
        System.out.println("----------------------------------- 2.5 -------------------------------");
        Stream
                .of(Optional.of("A"), Optional.of("B"), Optional.empty())
                /*
                    Ponizszy zapis z uzyciem referencji do metody jest mozliwy bo pod spodem wywolanie wyglada
                    nastepujaco:
                    .mapMulti((Optional<String> element, Consumer<String> consumer)-> element.ifPresent(consumer))
                */
                .mapMulti(Optional::ifPresent)
                .forEach(System.out::println);

        System.out.println("----------------------------------- 2.6 -------------------------------");
        var numbers2 = Stream
                .of(3, 4, 6)
                .mapMulti((number, consumer) -> findDivisors(number).forEach(consumer))
                .toList();
        System.out.println(numbers2);

    }
}
