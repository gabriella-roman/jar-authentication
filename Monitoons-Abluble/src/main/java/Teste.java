import Autenticação.Autenticacao;
import Autenticação.Token;

import java.util.Scanner;

public class Teste {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        Token token = new Token();
        Autenticacao autenticacao = new Autenticacao();
        Integer opcao;

        do {
            System.out.println("""
                    *-----------------------------Olá, bem vindo a Monitoons!------------------------------*
                    1 - Cadastro
                    2 - Login
                    3 - Gerar token
                    0 - Sair
                    *--------------------------------------------------------------------------------------*
                    """);
            opcao = input.nextInt();
            input.nextLine();

            switch (opcao) {
                case 0:
                    System.out.println("Até logo :)");
                    break;
                case 1 :
                    autenticacao.adicionarPergunta(token);
                    token.gerarToken();
                    break;
                case 2:
                    autenticacao.login(token);
                    break;
                case 3:
                    autenticacao.gerarNovoToken();
                    break;
                default:
                    System.out.println("Opção inválida");
                    break;
            }
        } while (opcao != 0);
    }
}
