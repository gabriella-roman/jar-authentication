package Componente;

import java.time.LocalDateTime;

public class Alerta {
    private String grauAlerta;
    private String tipoComponente;
    private LocalDateTime dataHora;

    public Alerta(String grauAlerta, String tipoComponente) {
        this.grauAlerta = grauAlerta;
        this.tipoComponente = tipoComponente;
        this.dataHora = LocalDateTime.now();
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

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    @Override
    public String toString(){
        return """
                grauAlerta=%s,
                tipoComponente=%s,
                dataHora=%s
                """.formatted(
                this.grauAlerta,
                this.tipoComponente,
                this.dataHora
        );
    }
}

