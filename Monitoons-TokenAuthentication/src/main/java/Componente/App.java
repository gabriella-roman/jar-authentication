package Componente;

import Autenticação.Autenticacao;
import Autenticação.Usuario;
import Conexao.Conexao;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.util.Scanner;

public class App {
    public static void main(String[] args) throws IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        Conexao conexao = new Conexao();
        JdbcTemplate jdbcTemplate = conexao.getConexaoDoBanco();
        Usuario usuario = new Usuario();
        Autenticacao autenticacao = new Autenticacao(scanner, jdbcTemplate);
        autenticacao.realizarAutenticacao(usuario);
        Monitoramento monitoramento = new Monitoramento();
        monitoramento.comecarMonitoramento(usuario);
    }
}
