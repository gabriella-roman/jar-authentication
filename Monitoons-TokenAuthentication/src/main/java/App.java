import Autenticação.Autenticacao;
import Autenticação.Token;
import Autenticação.Usuario;
import Conexao.Conexao;
import Conexao.ConexaoSQLServer;
import Monitoramento.MonitoramentoMsSQL;
import Monitoramento.MonitoramentoMySQL;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class App {
    public static void main(String[] args) throws IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        Conexao conexao = new Conexao();
        ConexaoSQLServer conexaoSQLServer = new ConexaoSQLServer();
        JdbcTemplate jdbcTemplate = conexaoSQLServer.getConexaoDoBanco();
        Autenticacao autenticacao = new Autenticacao(scanner, jdbcTemplate);

        System.out.println("Bem vindo ao Monitoons! \n");
        String plano = "";
        Usuario usuario;
        Boolean logou = false;
        do {
            System.out.println("Informe o seu email");
            String email = scanner.nextLine();

            System.out.println("Informe a sua senha");
            String senha = scanner.nextLine();

            usuario = new Usuario(email, senha);
            if (usuario.logar()) {
                Token token = new Token();
                System.out.println("Seja bem vindo!");
                autenticacao.concluirAutenticacao(email, usuario, token);
                logou = true;
            } else {
                System.out.println("Usuario ou senha incorretos!");
            }
        } while (!logou);
        if (logou) {
            Timer timer = new Timer();
            Usuario finalUsuario = usuario;
                MonitoramentoMySQL monitoramentoMySQL = new MonitoramentoMySQL();
                MonitoramentoMsSQL monitoramentoSQLServer = new MonitoramentoMsSQL();
                TimerTask tarefa = new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            monitoramentoMySQL.comecarMonitoramentoMySQL(finalUsuario);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            monitoramentoSQLServer.comecarMonitoramentoMsSQL(finalUsuario);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
                timer.scheduleAtFixedRate(tarefa, 0, 15000);

        }
    }
}
