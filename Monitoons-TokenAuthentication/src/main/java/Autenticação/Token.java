package Autenticação;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public class Token {
    private String token;
    private Boolean isCreated = false;
    private LocalDateTime dataTokenGerado;
    private LocalDateTime tokenExpirado;

    public String gerarToken() {

        this.dataTokenGerado = LocalDateTime.now();
        this.tokenExpirado = this.dataTokenGerado.plusMinutes(2);

        int randomNumber1 = ThreadLocalRandom.current().nextInt(1,99);
        int randomNumber2 = ThreadLocalRandom.current().nextInt(1, 999);
        int randomNumber3 = ThreadLocalRandom.current().nextInt(1, 9999);
        int sumNumbers = randomNumber1 + randomNumber2 + randomNumber3;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String formattedDateTime = dataTokenGerado.format(formatter);
        String formattedDateTimeExpiration = tokenExpirado.format(formatter);

        String tokens = String.valueOf((randomNumber1 * randomNumber2 * randomNumber3 * randomNumber2) / sumNumbers);
        tokens = tokens
                .replace("2", "M")
                .replace("4", "O")
                .replace("1", "T")
                .replace("3", "S")
                .replace("-", "");
        this.token = tokens;

        System.out.printf("""
                *------------------------------Olá, Seu token foi gerado!------------------------------*
                Token: %s                                              
                Seu token foi criado as %s, e expira em %s                 
                *--------------------------------------------------------------------------------------*
                """.formatted(token, formattedDateTime, formattedDateTimeExpiration));
        setCreated(true);
        return token;
    }

    public Boolean isExpirado() {
        if(LocalDateTime.now().isAfter(this.getTokenExpirado())) {
            return true;
        } else {
            return false;
        }
    }
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getDataTokenGerado() {
        return dataTokenGerado;
    }

    public void setDataTokenGerado(LocalDateTime dataTokenGerado) {
        this.dataTokenGerado = dataTokenGerado;
    }

    public LocalDateTime getTokenExpirado() {
        return tokenExpirado;
    }

    public void setTokenExpirado(LocalDateTime tokenExpirado) {
        this.tokenExpirado = tokenExpirado;
    }

    public Boolean getCreated() {
        return isCreated;
    }

    public void setCreated(Boolean created) {
        isCreated = created;
    }

    @Override
    public String toString() {
        return "Token{" +
                "token='" + token + '\'' +
                ", isCreated=" + isCreated +
                ", dataTokenGerado=" + dataTokenGerado +
                ", tokenExpirado=" + tokenExpirado +
                '}';
    }
}