package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class MyGdxGame extends ApplicationAdapter {
	//Variáveis de textura
	private SpriteBatch batch;
	private Texture[] passaros;
	private Texture fundo;
	private Texture canoBaixo;
	private Texture canoTopo;
	private Texture gameOver;
	private Texture logo;
	private Texture moeda1;
	private Texture moeda2;
	private Texture moedaAtual;

	// Variáveis de colisores
	private ShapeRenderer shapeRenderer;
	private Circle circuloPassaro;
	private Rectangle retanguloCanoCima;
	private Rectangle retanguloCanoBaixo;
	private Circle circuloMoeda;

	//Variáveis responsáveis por informações como valores de ponto, gravidade e etc.
	private float larguraDispositivo;
	private float alturaDipositivo;
	private float variacao = 0;
	private float gravidade = 2;
	private float posicaoInicialVerticalPassaro = 0;
	private float posicaoCanoHorizontal;
	private float posicaoCanoVertical;
	private float espacoEntreCanos;
	private Random random;
	private int pontos = 0;
	private int pontuacaoMaxima = 0;
	private boolean passouCano = false;
	private int estadoJogo = 0;
	private float posicaoHorizontalPassaro = 50;
	private float escalaPassaro = .5f;
	private float escalaMoeda = .25f;
	private float posicaoMoedaX;
	private float posicaoMoedaY;
	private float valorMoeda1 = 5;
	private float valorMoeda2 = 10;

	private float dificuldade = 200;

	//Variáveis que armazena informaçoes de texto, como score e recorde.
	private BitmapFont textoPontuacao;
	private BitmapFont textoReiniciar;
	private BitmapFont textoMelhorPontuacao;

	//Sons do jogo
	private Sound somVoando;
	private Sound somColisao;
	private Sound somPontuacao;
	private Sound somMoeda;

	//Variável preferencias, que guarda informações das localizações no android
	private Preferences preferencias;

	//Variável que guarda informações da resolução do jogo e tela do celular
	private OrthographicCamera camera;
	private Viewport viewport;
	private final float VIRTUAL_WIDTH = 720;
	private final float VIRTUAL_HEIGHT = 1280;

	//Método responsável por chamar outro metodo que contem texturas
	@Override
	public void create() {
		inicializarTexturas();
		inicializaObjetos();
	}

	//Método responsável por renderizar as texturas na tela e chama outros métodos
	@Override
	public void render() {
		//Um sistema de buffer que controla a cor dos pixels na tela, responsavel pelas cores dos assets do jogo
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		//Chamando métodos para renderizar tudo na tela, após o check do buffer
		verificarEstadoJogo();
		validarPontos();
		desenharCena();
		desenharTexturas();
		detectarColisoes();
	}

	//Método responsável por carregar as texturas usadas no jogo
	private void inicializarTexturas() {
		passaros = new Texture[3];
		passaros[0] = new Texture("angry1.png");
		passaros[1] = new Texture("angry2.png");
		passaros[2] = new Texture("angry3.png");

		fundo = new Texture("fundo.png");
		logo = new Texture("logo.png");
		canoBaixo = new Texture("cano_baixo_maior.png");
		canoTopo = new Texture("cano_topo_maior.png");
		gameOver = new Texture("game_over.png");
		moeda1 = new Texture("moedaprata.png");
		moeda2 = new Texture("moedaouro.png");

		moedaAtual = moeda2;
	}

	//Método responsável por organizar as coisas na tela de acordo com a resolução
	private void inicializaObjetos() {
		//inicia os batchs que sao sprites que nao tem interação
		batch = new SpriteBatch();
		random = new Random();

		//Inicia os objetos de acordo com a resolução do aparelho
		larguraDispositivo = VIRTUAL_WIDTH;
		alturaDipositivo = VIRTUAL_HEIGHT;
		posicaoInicialVerticalPassaro = alturaDipositivo / 2;
		posicaoCanoHorizontal = larguraDispositivo;
		espacoEntreCanos = 350;
		posicaoMoedaY = alturaDipositivo / 2;
		posicaoMoedaX = posicaoCanoHorizontal + larguraDispositivo / 2;

		//Renderiza o texto da pontuacao, define cor branca e deixa no tamanho definido
		textoPontuacao = new BitmapFont();
		textoPontuacao.setColor(Color.WHITE);
		textoPontuacao.getData().setScale(10);

		//Renderiza o texto de reinicia na cor verde e deixa do tamanho definido
		textoReiniciar = new BitmapFont();
		textoReiniciar.setColor(Color.GREEN);
		textoReiniciar.getData().setScale(2);

		//Renderiza o texto de recorde em vermelho e deixa do tamanho definido
		textoMelhorPontuacao = new BitmapFont();
		textoMelhorPontuacao.setColor(Color.RED);
		textoMelhorPontuacao.getData().setScale(2);

		//Cria o colisor dos objetos do jogo
		shapeRenderer = new ShapeRenderer();
		circuloPassaro = new Circle();
		retanguloCanoBaixo = new Rectangle();
		retanguloCanoCima = new Rectangle();
		circuloMoeda = new Circle();

		//Prepara os sons na cena para reproduzir quando for chamado
		somVoando = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
		somColisao = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
		somPontuacao = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));
		somMoeda = Gdx.audio.newSound(Gdx.files.internal("coinsound.wav"));

		//Chama as preferencias de outro codigo e puxa o recorde armazenado
		preferencias = Gdx.app.getPreferences("flappybird");
		pontuacaoMaxima = preferencias.getInteger("pontuacaoMaxima", 0);

		//Posiciona a camera na tela para que nao renderize as coisas fora do lugar
		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
		viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
	}

	//Método responsável por definir o modo de jogo, como iniciar, começar jogo e game over
	private void verificarEstadoJogo() {
		//Boolean que chequa se o usuario tocou na tela
		boolean toqueTela = Gdx.input.justTouched();

		//Se tocou na tela, muda de iniciar para começar jogo
		if (estadoJogo == 0) {
			if (toqueTela) {
				gravidade = -15;
				estadoJogo = 1;
				somVoando.play();
			}
		}
		//Se não tocou na tela, ou deu game over, chama outro if
		else if (estadoJogo == 1) {
			if (toqueTela) {
				gravidade = -15;
				somVoando.play();
			}

			//Responsável pela velocidade dos assets na tela
			posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * dificuldade;
			posicaoMoedaX -= Gdx.graphics.getDeltaTime() * dificuldade;

			//Checando colisão do cano ao player
			if (posicaoCanoHorizontal < -canoTopo.getWidth()) {
				posicaoCanoHorizontal = larguraDispositivo;
				posicaoCanoVertical = random.nextInt(400) - 200;
				passouCano = false;
			}

			//Se a moeda nao for coletada, e ela sai da tela, ela é respawnada
			if (posicaoMoedaX < -moedaAtual.getWidth() / 2 * escalaMoeda) {
				resetaMoeda();
			}

			if (posicaoInicialVerticalPassaro < 0 || posicaoInicialVerticalPassaro > alturaDipositivo) {
				estadoJogo = 2;
			}

			//Responsável pelo movimento do passaro, desligando gravidade e adicionando novamente
			if (posicaoInicialVerticalPassaro > 0 || toqueTela) {
				posicaoInicialVerticalPassaro = posicaoInicialVerticalPassaro - gravidade;
				gravidade++;

				if (pontos > 20) {
					dificuldade = 500;
				}
			}

		}

		//Se gamer over, guarda as informações de score e checa se é recorde novo ou nao
		else if (estadoJogo == 2) {
			if (pontos > pontuacaoMaxima) {
				pontuacaoMaxima = pontos;
				preferencias.putInteger("pontuacaoMaxima", pontuacaoMaxima);
				preferencias.flush();
			}

			posicaoHorizontalPassaro -= Gdx.graphics.getDeltaTime() * 500;

			//Se game over e o player tocar na tela, recomeça o jogo para a tela inical
			if (toqueTela) {
				estadoJogo = 0;
				pontos = 0;
				gravidade = 0;
				posicaoHorizontalPassaro = 50;
				posicaoInicialVerticalPassaro = alturaDipositivo / 2;
				posicaoCanoHorizontal = larguraDispositivo;
				resetaMoeda();
			}
		}
	}

	//Método responsável por detectar colisões
	private void detectarColisoes() {
		//Colisor do passaro, centralizado ao asset
		circuloPassaro.set
				(
						posicaoHorizontalPassaro + passaros[0].getWidth() * escalaPassaro / 2,
						posicaoInicialVerticalPassaro + passaros[0].getHeight() * escalaPassaro / 2,
						(passaros[0].getHeight() * escalaPassaro) / 2
				);

		//Colisor da moeda
		circuloMoeda.set
				(
						posicaoMoedaX - ((moedaAtual.getWidth() * escalaMoeda) / 2),
						posicaoMoedaY - ((moedaAtual.getHeight() * escalaMoeda) / 2),
						(moedaAtual.getWidth() * escalaMoeda) / 2
				);

		//Colisor do cano de baixo, ajustado com o asset
		retanguloCanoBaixo.set
				(
						posicaoCanoHorizontal,
						alturaDipositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoVertical,
						canoBaixo.getWidth(),
						canoBaixo.getHeight()
				);

		retanguloCanoCima.set
				(
						posicaoCanoHorizontal,
						alturaDipositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical,
						canoTopo.getWidth(),
						canoTopo.getHeight()
				);

		//Se houver colisão, ativa as boleanas de gameover
		boolean colidiuCanoCima = Intersector.overlaps(circuloPassaro, retanguloCanoCima);
		boolean colidiuCanoBaixo = Intersector.overlaps(circuloPassaro, retanguloCanoBaixo);
		boolean colidiuMoeda = Intersector.overlaps(circuloPassaro, circuloMoeda);

		//Se colidiu com a moeda for verdadeira, soma pontos e checa se é moeda dourada ou prata
		if (colidiuMoeda == true) {
			if (moedaAtual == moeda1) pontos += valorMoeda1;
			else pontos += valorMoeda2;
			posicaoMoedaY = alturaDipositivo * 2;
			somMoeda.play();
		}

		//Se colidiu com cano baixo ou cano de cima, ativa o modo gameover
		if (colidiuCanoBaixo || colidiuCanoCima) {
			if (estadoJogo == 1) {
				somColisao.play();
				estadoJogo = 2;
			}
		}
	}

	//Método responsável por posicionar background usando batchs
	private Screen desenharCena() {
		//batch combinando com a camera
		batch.setProjectionMatrix(camera.combined);

		//iniciar o batch que desenha todos os assets na tela
		batch.begin();

		batch.draw(fundo, 0, 0, larguraDispositivo, alturaDipositivo);
		batch.draw
				(
						passaros[(int) variacao],
						posicaoHorizontalPassaro,
						posicaoInicialVerticalPassaro,
						passaros[0].getWidth() * escalaPassaro,
						passaros[0].getHeight() * escalaPassaro
				);

		if (estadoJogo != 0) {
			//batch que desenha o primeiro cano na tela inicial
			batch.draw
					(
							canoBaixo,
							posicaoCanoHorizontal,
							alturaDipositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoVertical
					);

			//batch que desenha o primeiro cano na tela inicial
			batch.draw
					(
							canoTopo,
							posicaoCanoHorizontal,
							alturaDipositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical
					);

			//batch que desenha a primeira moeda na tela inicial
			batch.draw
					(
							moedaAtual,
							posicaoMoedaX - (moedaAtual.getWidth() * escalaMoeda),
							posicaoMoedaY - (moedaAtual.getWidth() * escalaMoeda),
							moedaAtual.getWidth() * escalaMoeda,
							moedaAtual.getHeight() * escalaMoeda
					);
		}
		return null;
	}

	//Método responsável por chamar os batchs que desenham os pontos
	private void desenharTexturas()
	{
		batch.setProjectionMatrix(camera.combined);
		//batch.begin();

		//desenha a pontuacao na posicao definida
		textoPontuacao.draw
				(
						batch,
						String.valueOf(pontos),
						larguraDispositivo/ 2,
						alturaDipositivo - 110
				);

		//Switch que troca os batchs na tela conforme o estado do jogo
		switch (estadoJogo)
		{
			case 0:
			{
				//desenha a logo na tela inicial
				batch.draw
						(
								logo,
								larguraDispositivo/2 - logo.getWidth()/2/4,
								alturaDipositivo / 3,
								logo.getWidth()/4,
								logo.getHeight()/4
						);
			}break;

			case 1: {} break;

			case 2:
			{
				//se for game over, desenha a logo game over
				batch.draw
						(
								gameOver,
								larguraDispositivo/2 - gameOver.getWidth()/2,
								alturaDipositivo / 2
						);

				//Desenha o texto reiniciar
				textoReiniciar.draw
						(
								batch,
								"Toque para reiniciar!",
								larguraDispositivo/2 -140,
								alturaDipositivo/2 - gameOver.getHeight()/2
						);

				//Desenha o recorde na tela
				textoMelhorPontuacao.draw
						(
								batch,
								"Seu Record é: " + pontuacaoMaxima + " pontos",
								larguraDispositivo/2 -140,
								alturaDipositivo/2 - gameOver.getHeight()
						);
			}break;
		}
		batch.end();
	}

	//Método responsável pelos pontos ao passar entre os canos
	public void validarPontos()
	{
		if (posicaoCanoHorizontal < posicaoHorizontalPassaro)
		{
			if(!passouCano)
			{
				pontos++;
				passouCano = true;
				somPontuacao.play();
			}
		}

		variacao += Gdx.graphics.getDeltaTime() * 10;

		if (variacao > 3)
		{
			variacao = 0;
		}
	}

	//Método responsável por respawnar a moeda caso nao colete ela, ou cria uma nova ao coletar
	private void resetaMoeda()
	{
		posicaoMoedaX = posicaoCanoHorizontal + canoBaixo.getWidth() + moedaAtual.getWidth() + random.nextInt((int) (larguraDispositivo - (moedaAtual.getWidth() * escalaMoeda)));
		posicaoMoedaY = moedaAtual.getHeight() / 2 + random.nextInt((int) alturaDipositivo - moedaAtual.getHeight() / 2);

		int randomMoedaNova = random.nextInt(100);
		if(randomMoedaNova < 30)
		{
			moedaAtual = moeda2;
		}
		else
		{
			moedaAtual = moeda1;
		}
	}

	//Método responsável por checar a resolucao da tela a toda momenta(DILMA)
	@Override
	public void resize(int width, int height)
	{
		viewport.update(width, height);
	}

	//Limpa e libera memória do aparelho
	@Override
	public void dispose () {}
}
