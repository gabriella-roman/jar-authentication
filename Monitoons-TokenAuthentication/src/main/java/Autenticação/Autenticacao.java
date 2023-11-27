package Autenticação;

import com.google.gson.JsonObject;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.util.Scanner;

public class Autenticacao {
    private final Scanner scanner;
    private final JdbcTemplate jdbcTemplate;
    JsonObject json = new JsonObject();

    public Autenticacao(Scanner scanner, JdbcTemplate jdbcTemplate) {
        this.scanner = scanner;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void realizarAutenticacao(Usuario usuario) throws IOException, InterruptedException {
        String email = obterEmail();

        while (!usuario.emailExiste(email)) {
            System.out.println("Seu email não está cadastrado em nosso sistema. Cheque as credenciais!");
            email = obterEmail();
        }

        realizarAutenticacaoSenha(email, usuario);
    }

    private String obterEmail() {
        System.out.println("Insira seu email: ");
        return scanner.nextLine();
    }

    private void realizarAutenticacaoSenha(String email, Usuario usuario) throws IOException, InterruptedException {
        Boolean senhaCorreta = false;
        Token token = new Token();

        while (!senhaCorreta) {
            System.out.println("Insira sua senha: ");
            String senha = scanner.nextLine();
            senhaCorreta = usuario.senhaCorreta(email, senha);

            if (!senhaCorreta) {
                System.out.println("Senha incorreta! Tente novamente.");
            } else {
                concluirAutenticacao(email, usuario, token);
            }
        }
    }

    private void concluirAutenticacao(String email, Usuario usuario, Token token) throws IOException, InterruptedException {
        Integer idUsuario = jdbcTemplate.queryForObject("SELECT idUsuario FROM usuario WHERE email = ?", Integer.class, email);
        usuario.setIdUsuario(idUsuario);
        String nomeUsuario = jdbcTemplate.queryForObject("SELECT nomeUsuario FROM usuario WHERE email = ?", String.class, email);
        System.out.println("Bem vindo(a), " + nomeUsuario + "!");
        enviarTokenAutenticacao(nomeUsuario, usuario, token);
    }

    private void enviarTokenAutenticacao(String nomeUsuario, Usuario usuario, Token token) throws IOException, InterruptedException {

        token.gerarToken(usuario);

        JsonObject json = new JsonObject();
        json.addProperty("text", "Olá, " + nomeUsuario + "! Seu token de autenticação é: " + token.getToken() + "." + "\n" + "O token expira em 5 minutos.");
        Slack.enviarMensagem(json);

        System.out.println("No canal token-autenticacao foi enviado um token para você. Insira-o no console para continuar: ");

        validarToken(usuario, token);
    }

    private void validarToken(Usuario usuario, Token token) throws IOException, InterruptedException {
        String tokenDoBanco = jdbcTemplate.queryForObject(
                "SELECT token FROM token WHERE fkUsuario = ? ORDER BY STR_TO_DATE(dataHoraCriado, '%d-%m-%Y %H:%i') DESC LIMIT 1",
                String.class,
                usuario.getIdUsuario()
        );

        String tokenUsuario;

        boolean tokenValido = false;

        while (!tokenValido) {
            System.out.println("Insira o token: ");
            tokenUsuario = scanner.nextLine();

            if (tokenDoBanco.equals(tokenUsuario)) {
                if (!token.isExpired()) {
                    System.out.println("O monitoramento foi iniciado!");
                    tokenValido = true;
                } else {
                    System.out.println("Token expirado! Iremos gerar um novo token para você.");
                    token.gerarToken(usuario);
                    json.addProperty("text", "Olá, " + usuario.getNome() + "! Seu token de autenticação é: " + token.getToken() + "." + "\n" + "O token expira em 5 minutos.");
                    Slack.enviarMensagem(json);
                    System.out.println("Foi enviado um novo token para você.");
                }
            } else {
                System.out.println("Token inválido! Tente novamente.");
            }
        }
    }

    public String getEmail(Usuario usuario) {
        return usuario.getEmail();
    }
}
