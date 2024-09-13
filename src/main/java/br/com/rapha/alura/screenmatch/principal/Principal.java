package br.com.rapha.alura.screenmatch.principal;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;
// import java.util.Arrays;

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
        System.out.println("Top 5 episodios: ");
        dadosEpisodios.stream()
            .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
            .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
            .limit(5)
            .forEach(System.out::println);

        List<Episodio> episodios = temporadas.stream()
            .flatMap(t -> t.episodios().stream()
                .map(d -> new Episodio(t.numero(), d))
            ).collect(Collectors.toList());

        episodios.forEach(System.out::println);

    }
}
