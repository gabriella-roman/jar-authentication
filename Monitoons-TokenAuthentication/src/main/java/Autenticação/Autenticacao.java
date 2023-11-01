package Autenticação;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Autenticacao {
    private List<String> perguntas;
    private List<String> respostas;
    public void login(Token token) {
        String tokenGerado = token.getToken();
        if (!token.getCreated()) {
            System.out.println("Token não foi criado!");
        } else {
            Scanner input = new Scanner(System.in);
            System.out.println("Informe o token");
            String resposta = input.nextLine();
            if (!resposta.equals(tokenGerado)) {
                System.out.println("Token inválido");
            } else if (token.isExpirado()) {
                System.out.println("Token expirado");
            } else {
                System.out.println("Bem-vindo(a)");

            }
        }
    }

    public void adicionarPergunta (Token token) {
        Scanner input = new Scanner(System.in);

        if(!(getPerguntas() == null || getRespostas() == null || getPerguntas().isEmpty() || getRespostas().isEmpty())){
            System.out.println("Você já tem perguntas cadastradas!");
        } else {
            this.perguntas = new ArrayList<>();
            this.respostas = new ArrayList<>();
            System.out.println("Essas perguntas servem para gerar um novo token!");
            System.out.println("Digite sua pergunta!");
            String pergunta = input.nextLine();
            System.out.println("Agora, digite a resposta!");
            String resposta = input.nextLine();

            perguntas.add(pergunta);
            respostas.add(resposta);

            System.out.println("Sua pergunta foi inserida com sucesso!");
            System.out.println("""
                Pergunta: %s
                Resposta: %s
                """.formatted(pergunta, resposta));
            System.out.println("Para gerar um novo token será necessário responder essas perguntas com êxito.");
            token.gerarToken();
        }
    }

    public void gerarNovoToken(Token token) {
        String resposta;
        Scanner input = new Scanner(System.in);

        if (getPerguntas() == null || getRespostas() == null || getPerguntas().isEmpty() || getRespostas().isEmpty()) {
            System.out.println("Nenhum token foi gerado.");
        } else {
            if(token.getCreated().equals(true) && !token.isExpirado()) {
                System.out.println("Token antigo ainda pode ser utilizado!");
            } else {
                if (token.isExpirado()) {
                    System.out.println("""
                            Token expirado. Responda para obter um novo token: 
                            %s
                            """.formatted(getPerguntas().get(0)));
                    resposta = input.nextLine();
                    if (!resposta.equals(getRespostas().get(0))) {
                        System.out.println("Resposta incorreta");
                    } else {
                        System.out.println("Seu token será gerado!");
                        token.gerarToken();
                    }
                }
            }
        }
    }

    public List<String> getPerguntas() {
        return perguntas;
    }

    public void setPerguntas(List<String> perguntas) {
        this.perguntas = perguntas;
    }

    public List<String> getRespostas() {
        return respostas;
    }

    public void setRespostas(List<String> respostas) {
        this.respostas = respostas;
    }

    @Override
    public String toString() {
        return "Autenticacao{" +
                "perguntas=" + perguntas +
                ", respostas=" + respostas +
                '}';
    }
}
