package Autenticação;

import Conexao.Conexao;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public class Token {
    private String token;
    private LocalDateTime dataHoraCriado;
    private LocalDateTime dataHoraExpira;

    Conexao con = new Conexao();
    public String gerarToken(Usuario usuario) {

        this.dataHoraCriado = LocalDateTime.now();
        this.dataHoraExpira = this.dataHoraCriado.plusMinutes(5);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String formattedDateTime = dataHoraCriado.format(formatter);
        String formattedDateTimeExpiration = dataHoraExpira.format(formatter);

        int randomNumber1 = ThreadLocalRandom.current().nextInt(1,99);
        int randomNumber2 = ThreadLocalRandom.current().nextInt(1, 999);
        int randomNumber3 = ThreadLocalRandom.current().nextInt(1, 9999);
        int sumNumbers = randomNumber1 + randomNumber2 + randomNumber3;

        String tokens = String.valueOf((randomNumber1 * randomNumber2 * randomNumber3 * randomNumber2) / sumNumbers);

        tokens = tokens
                .replace("2", "M")
                .replace("4", "O")
                .replace("1", "T")
                .replace("3", "S")
                .replace("-", "");
        this.token = tokens;

        try {
            con.getConexaoDoBanco().update("INSERT INTO token (token, dataHoraCriado, dataHoraExpira, fkUsuario) VALUES (?, ?, ?, ?)", this.token, formattedDateTime, formattedDateTimeExpiration, usuario.getIdUsuario());
        } catch (Exception e) {
            System.out.println("Erro ao inserir token!");
        }
        return token;
    }

    public Boolean isExpired() {
        LocalDateTime dataHoraAtual = LocalDateTime.now();
        return dataHoraAtual.isAfter(this.dataHoraExpira);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
