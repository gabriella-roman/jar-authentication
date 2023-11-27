package Componente;

import java.time.LocalDateTime;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

public class Alerta extends Registro{

    private Integer indexRegistro;
    private String grauAlerta;
    private String tipoComponente;
    private LocalDateTime dataHora;

    public Alerta(Integer fkCompHasComp, String tipo, Double valor, String valorFormatado, String unidade,Integer indexRegistro, String grauAlerta, String tipoComponente) {
        super(fkCompHasComp, tipo, valor, valorFormatado, unidade);
        this.indexRegistro = indexRegistro;
        this.grauAlerta = grauAlerta;
        this.tipoComponente = tipoComponente;
        this.dataHora = LocalDateTime.now();
    }

    public void enviarAlertaSlack(String nomeUsuario){
        System.out.println("Eu estou entrando aquiii");
        var client = Slack.getInstance().methods();
        var logger = LoggerFactory.getLogger("alertas-gerados-monitoons");
        try {
            // Call the chat.postMessage method using the built-in WebClient
            var result = client.chatPostMessage(r -> r
                    // The token you used to initialize your app
                    .token("xoxb-6248106097664-6225291978755-qIXSAA02lX1oVVpyX0FA2tSU")
                    .channel("#alertas")
                    .text("%s: O componente %s do computador do funcionário %s está com um alerta de grau %s. Recomendamos que você verifique a situação do computador".formatted(this.dataHora, this.tipoComponente, nomeUsuario, this.grauAlerta)));
            // You could also use a blocks[] array to send richer content

            // Print result, which includes information about the message (like TS)
            logger.info("result {}", result);
        } catch (IOException | SlackApiException e) {
            logger.error("error: {}", e.getMessage(), e);
            System.out.println("Deu erro" + e);
        }
    }

    public Integer getIndexRegistro() {
        return indexRegistro;
    }

    public void setIndexRegistro(Integer indexRegistro) {
        this.indexRegistro = indexRegistro;
    }

    public String getGrauAlerta() {
        return grauAlerta;
    }

    public void setGrauAlerta(String grauAlerta) {
        this.grauAlerta = grauAlerta;
    }

    public String getTipoComponente() {
        return tipoComponente;
    }

    public void setTipoComponente(String tipoComponente) {
        this.tipoComponente = tipoComponente;
    }

    @Override
    public String toString(){
        return """
                %s
                grauAlerta=%s,
                tipoComponente=%s,
                dataHora= %s
                """.formatted(super.toString(),
                this.grauAlerta,
                this.tipoComponente,
                this.dataHora
        );
    }
}


