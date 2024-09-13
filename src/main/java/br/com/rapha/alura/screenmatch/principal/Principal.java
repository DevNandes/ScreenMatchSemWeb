package br.com.rapha.alura.screenmatch.principal;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

import br.com.rapha.alura.screenmatch.model.DadosSerie;
import br.com.rapha.alura.screenmatch.services.ConsumoApi;
import br.com.rapha.alura.screenmatch.services.ConverteDados;
import br.com.rapha.alura.screenmatch.model.DadosTemporada;


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

        // Busca dados Da Serie
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
		System.out.println(dados);

        List<DadosTemporada> temporadas = new ArrayList<>();

		for (int i = 1; i <=dados.totalTemporadas(); i++) {
			json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + TEMPORADA + i + API_KEY);
			DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
			temporadas.add(dadosTemporada);
		}
		temporadas.forEach(System.out::println);
    }
}
