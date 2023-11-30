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

    public void concluirAutenticacao(String email, Usuario usuario, Token token) throws IOException, InterruptedException {
        if(usuario.logar()){
            enviarTokenAutenticacao(usuario.getNome(), usuario, token);
        }

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
                "SELECT TOP 1 token\n" +
                        "FROM token\n" +
                        "WHERE fkUsuario = ?\n" +
                        "ORDER BY CONVERT(datetime, dataHoraCriado, 103) DESC",
                String.class,
                usuario.getIdUsuarioSQLServer()
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
