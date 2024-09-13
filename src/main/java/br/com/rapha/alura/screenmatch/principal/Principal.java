package br.com.rapha.alura.screenmatch.principal;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.time.LocalDate;
import java.util.Optional;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import br.com.rapha.alura.screenmatch.model.DadosEpisodio;
import br.com.rapha.alura.screenmatch.model.DadosSerie;
import br.com.rapha.alura.screenmatch.services.ConsumoApi;
import br.com.rapha.alura.screenmatch.services.ConverteDados;
import br.com.rapha.alura.screenmatch.model.DadosTemporada;
import br.com.rapha.alura.screenmatch.model.Episodio;


public class Principal {

    private Scanner input = new Scanner(System.in);
    private ConsumoApi consumo =  new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();

    // O final serve para identificar variaveis que nunca serao alteradas e tambem usa-se o nome em completo maiusculo para essas variaveis deste tipo
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=31681602";
    private final String TEMPORADA = "&Season=";

    
    public void exibeMenu(){
        System.out.println("Digite o nome da serie para busca:");
        var nomeSerie = input.nextLine();
		var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);

        // Busca dados da Serie
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
		System.out.println(dados);

        // Busca dados da Temporada
        List<DadosTemporada> temporadas = new ArrayList<>();

		for (int i = 1; i <=dados.totalTemporadas(); i++) {
			json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + TEMPORADA + i + API_KEY);
			DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
			temporadas.add(dadosTemporada);
		}
		temporadas.forEach(System.out::println);

        // for (int i = 0; i < dados.totalTemporadas(); i++){
        //     List<DadosEpisodio> episodiosTemporada = temporadas.get(i).episodios();
        //     for (int j = 0; j < episodiosTemporada.size(); j++){
        //         System.out.println(episodiosTemporada.get(j).titulo());
        //     }
        // }

        // Dois lupings usando variaveis arbitrarias( -> )(lambda)
        temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));

        // Exemplo de streams(Fluxo)
        // List<String> nomes = Arrays.asList("rapha", "matheus", "Josuel", "Alison");

        // nomes.stream()
        //     .sorted()
        //     .limit(3)
        //     .map(n -> n.toLowerCase())
        //     .filter(n -> n.startsWith("a"))
        //     .forEach(System.out::println);

        List<DadosEpisodio> dadosEpisodios = temporadas.stream()
            .flatMap(t -> t.episodios().stream())
            // Gera uma lista mutavel, ou seja e possivel adicionar mais episodios e afins
            .collect(Collectors.toList());
            // Geraria uma lista imutavel
            // .toList()

        // Lista o top 5 episodios
        System.out.println("Top 10 episodios: ");
        dadosEpisodios.stream()
            .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
            .peek(e -> System.out.println("Primeiro FIltro(N/A): " + e))
            .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
            .peek(e -> System.out.println("Ordenacao: " + e))
            .limit(10)
            .peek(e -> System.out.println("Limit:  " + e))
            .map(e -> e.titulo().toUpperCase())
            .peek(e -> System.out.println("Map: " + e))
            .forEach(System.out::println);

        List<Episodio> episodios = temporadas.stream()
            .flatMap(t -> t.episodios().stream()
                .map(d -> new Episodio(t.numero(), d))
            ).collect(Collectors.toList());

        episodios.forEach(System.out::println);

        Integer i = 1;

        while (i != 2) {

            System.out.println("Digite um trecho do titulo que voce quer buscar: ");
    
            var trechoTitulo = input.nextLine();
    
            // Usa optional pra se nao achar ele nao ter uma referencia nula
            Optional<Episodio> episodioBuscado = episodios.stream()
                .filter(e-> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase()))
                .findFirst();
    
            if (episodioBuscado.isPresent()){
                System.out.println("Episodio Encontrado: " + episodioBuscado.get());
                i = 2;
            } else {
                System.out.println("Episodio nao encontrado!");
                System.out.println("=================================");
                System.out.println("Deseja tentar novamente?\n1 - Sim\n2 - Nao");

                i = input.nextInt();
                input.nextLine();
            }
        }


        System.out.println("A partir de que ano voce quer ver os episodios?");
        var ano = input.nextInt();
        input.nextLine();

        LocalDate dataBusca = LocalDate.of(ano, 1, 1);

        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        episodios.stream()
            .filter(e ->e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
            .forEach(e -> System.out.println(
                "Temporada: " + e.getTemporada() +
                    " | Episodio: " + e.getTitulo() +
                    " | Data de Lancamento: " + e.getDataLancamento().format(formatador)
            ));

        Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
            .filter(e -> e.getAvaliacao() > 0.0)
            .collect(Collectors.groupingBy(Episodio::getTemporada, Collectors.averagingDouble(Episodio::getAvaliacao)));

        System.out.println(avaliacoesPorTemporada);
        // Sem filtro
        //{1=6.176, 2=7.708333333333333, 3=7.060869565217391, 4=5.542857142857143, 5=7.0058823529411764, 6=7.433333333333334}
        // Com filtro
        //{1=8.577777777777778, 2=8.409090909090908, 3=8.547368421052632, 4=8.622222222222222, 5=8.507142857142856, 6=8.3625}
    }
}
